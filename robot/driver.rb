# Author: Collin May

require 'stringio'

class Driver
  def initialize(robot, socket)
    @robot = robot
    @socket = socket

    @log = $log.sub("[" + @socket.remote_address.ip_address + "] ")
    
    @input_thread = Thread.new do
      begin
        run_input
      rescue => e
        @log.log_exception e
      ensure
        @log.log "lost connection"
        @socket.close
      end
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

  def send_subsystem_info
    reports = @robot.subsystems.collect do |sub|
      driver_name = sub.driver == nil ? "" : sub.driver.name
      sub.numeric_type.chr + pack_str(sub.name) + pack_str(driver_name)
    end
    write_packet(0x02, [reports.length].pack("S>") + reports.join(String.new))
  end

  def send_battery_info
    reports = @robot.batteries.collect do |bat|
      full = bat.full_voltage * 1000
      current = bat.current_voltage * 1000
      name = bat.name
      next [full, current].pack("l>l>") + pack_str(name)
    end
    write_packet(0x07, [reports.length].pack("S>") + reports.join(String.new))
  end
  
  def run_input
    while(!@socket.eof) do
      size = @socket.read(2).unpack("s>")[0]
      type = @socket.read(1).unpack("c")[0]
      
      case(type)
      when 0x00
        @log.log "got invalid 0x00 packet"
      when 0x01 # keep alive
        send_keepalive(@socket.read(8))
      when 0x02 # query subsystems
        @log.log "got subsystem query"
        send_subsystem_info
        send_battery_info
      when 0x03 # register driver
        @name = recv_str
        @log.prefix = "[#{@name}] "
        @log.log "received name"
      end
    end
  end

  attr_reader :name
end
