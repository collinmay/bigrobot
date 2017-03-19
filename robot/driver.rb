# Author: Collin May

require 'stringio'
require 'thread'

class Driver
  def initialize(robot, socket)
    @robot = robot
    @socket = socket
    
    @log = $log.sub("[" + @socket.remote_address.ip_address + "] ")
    @netlog = NetLogger.new(self)
    $log.add_target @netlog
    
    @input_thread = Thread.new do
      begin
        run_input
      rescue => e
        @log.log_exception e
      ensure
        $log.remove_target @netlog
        @log.log "lost connection"
        unbind_subsystems
        @socket.close
      end
    end

    @subsystems = []
  end

  def unbind_subsystems
    @subsystems.each do |sub|
      sub.unbind
    end
  end

  def safe_mode
    @subsystems.each do |sub|
      sub.safe_mode
    end
  end
  
  def recv_str
    len = @socket.read(2).unpack("S>")[0]
    return @socket.read(len)
  end

  def pack_str(str)
    [str.length].pack("S>") + str
  end
  
  def write_packet(type, payload)
    packet = [payload.length, type].pack("s>C") + payload
    @socket.write(packet)
    @socket.flush
  end
  
  def send_keepalive(timestamp)
    write_packet(0x01, timestamp)
  end

  def send_subsystems
    reports = @robot.subsystems.collect do |sub|
      driver_name = sub.driver == nil ? "" : sub.driver.name
      header = [sub.numeric_type].pack("C") + pack_str(sub.name) + pack_str(driver_name)

      next header + sub.pack_metadata
    end
    write_packet(0x02, [reports.length].pack("S>") + reports.join(String.new))
  end

  def send_sensors
    reports = @robot.sensors.collect do |sensor|
      header = [sensor.numeric_type].pack("l>") + pack_str(sensor.name)
      next header + sensor.pack_metadata
    end
    write_packet(0x07, [reports.length].pack("S>") + reports.join(String.new))
  end

  def send_subsystem_bind_success(sub)
    @log.log "bound subsystem " + sub.id.to_s
    write_packet(0x04, [sub.id].pack("s>"))
  end
  
  def send_subsystem_bind_failiure(sub)
    write_packet(0x03, [sub.id].pack("s>"))
  end

  def send_firmware_info
    write_packet(0x09, pack_str("robotd") + pack_str($VERSION) + [$PROTOCOL_VERSION].pack("s>"))
  end
  
  def run_input
    while(!@socket.eof) do
      size = @socket.read(2).unpack("s>")[0]
      type = @socket.read(1).unpack("c")[0]
      
      case(type)
      when 0x00
        @log.log "got invalid 0x00 packet"
      when 0x01 # keep alive
        time = @socket.read(8)
        if Time.now.to_f * 1000.0 - time.unpack("q>")[0] > 2000.0 then
          safe_mode
        end
        send_keepalive(time)
      when 0x02 # query subsystems
        send_sensors
        send_subsystems
        send_firmware_info
      when 0x03 # register driver
        @name = recv_str
        @log.prefix = "[#{@name}] "
      when 0x04 # bind subsystem
        sub = @robot.subsystems[@socket.read(2).unpack("s>")[0]]
        if sub.attempt_bind(self) then
          send_subsystem_bind_success sub
          @subsystems.push(sub)
        else
          send_subsystem_bind_failiure sub
        end
      when 0x05 # subsystem update
        sub = @robot.subsystems[@socket.read(2).unpack("s>")[0]]
        if sub.driver == self then
          sub.read @socket
        else
          @log.log "tried to update unbound system"
        end
      when 0x06 # unbind subsystem
        sub = @robot.subsystems[@socket.read(2).unpack("s>")[0]]
        if sub.driver == self then
          sub.unbind
        end
      else
        @log.log "got unknown 0x" + type.to_s(16) + " packet"
        @socket.read(size)
      end
    end
  end

  def subsystem_bound(sub, driver)
    write_packet(0x06, [sub.id].pack("s>") + pack_str(driver.name))
  end

  class NetLogger
    def initialize(driver)
      @driver = driver
    end

    def log(msg)
      @driver.write_packet(0x08, @driver.pack_str(msg))
    rescue
    end
  end
  
  attr_reader :name
end
