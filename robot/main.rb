# Author: Collin May

require 'socket'
require 'ipaddr'
require 'json'
require 'etc'

require_relative 'logger.rb'
require_relative 'driver.rb'
require_relative 'robot.rb'
require_relative 'adapter/sabertooth.rb'

require_relative 'drive/skidsteer.rb'

SABERTOOTH_PORT = ARGV[0] || "/dev/ttyACM0"

$log.log "Starting up..."

$log.log "Opening sabertooth on " + SABERTOOTH_PORT

SABERTOOTH_IO = File.open(SABERTOOTH_PORT, "r+b")

$log.log "RUID: " + Process.uid.to_s + " EUID: " + Process.euid.to_s

if Process.uid == 0 || Process.euid == 0 then
  $log.log "Attempting privelege drop..."
  
  begin
    tgt_uid = Etc.getpwnam("robotd").uid
    Process::Sys.setresuid(tgt_uid, tgt_uid, tgt_uid)
    $log.log "Dropped to UID " + tgt_uid.to_s
    $log.log "RUID: " + Process.uid.to_s + " EUID: " + Process.euid.to_s
    $log.log "Testing privelege drop..."
    
    begin
      Process::Sys.setuid(0)
    rescue Errno::EPERM
      $log.log "Success!"
    else
      $log.log "Privelege drop didn't work?"
      exit 1
    end
  rescue => e
    $log.log_exception e
  end
else
  begin
    Process::Sys.setuid(0)
  rescue Errno::EPERM
  else
    $log.log "Can escalate to root"
    exit 1
  end
end

begin
  $robot = Robot.new
  
=begin
  $log.log "Loading config..."
  config_hash = JSON.parse(File.read("config.json"))
  
  $robot.adapters = Hash.new
  
  if config_hash.has_key? "adapters" then
    config_hash["adapters"].each do |name, adapter_hash|
      if !adapter_hash.has_key? "type" then
        throw "adapter declaration has no type"
      end
      case adapter_hash["type"]
      when "sabertooth"
        $robot.add_adapter(name, Adapters::Sabertooth.from_json(adapter_hash))
      else
        throw "unknown adapter type '" + adapter_hash["type"] + "'"
      end
    end
  end
  
  if config_hash.has_key? "subsystems" then
    config_hash["subsystems"].each do |sub_hash|
      if !sub_hash.has_key? "name" then
        throw "subsystem declaration has no name"
      end
      if !sub_hash.has_key? "type" then
        throw "subsystem declaration has no type"
      end
      case sub_hash["type"]
      when "skid steer drive"
        $robot.add_subsystem Subsystems::Drive::SkidSteer.from_json($robot, sub_hash)
      else
        throw "unknown subsystem type '" + sub_hash["type"] + "'"
      end
    end
  end
=end

  sabertooth = Adapters::Sabertooth.new(Adapters::Sabertooth::Connections::TextConnection.new(SABERTOOTH_IO))
  $robot.add_adapter("sabertooth", sabertooth)
  drive = Subsystems::Drive::SkidSteer.new("Main Drive", sabertooth.m2, sabertooth.m1)
  $robot.add_subsystem drive
  
  discovery_sock = UDPSocket.new
  discovery_group = IPAddr.new("238.160.102.2").hton + IPAddr.new("0.0.0.0").hton
  discovery_group_wlan = IPAddr.new("238.160.102.2").hton + IPAddr.new("10.0.7.1").hton
  
  #discovery_sock.setsockopt(:IPPROTO_IP, :IP_ADD_MEMBERSHIP, discovery_group)
  discovery_sock.setsockopt(:IPPROTO_IP, :IP_ADD_MEMBERSHIP, discovery_group_wlan)
  discovery_sock.bind("0.0.0.0", 25601)
  
  discovery_thread = Thread.new do
    loop do
      message, peer = discovery_sock.recvfrom(255)
      if message == "find robots" then
        discovery_sock.send(Socket.gethostname, 0, peer[3], peer[1])
      end
    end
  end

  $log.log "Launched discovery server thread"
  
  server = TCPServer.new 25600
  drivers = []

  $log.log "Accepting connections..."
  loop do
    sock = server.accept
    $log.log "Accepted connection from " + sock.remote_address.ip_address
    drivers.push(Driver.new($robot, sock))
  end
rescue => e
  begin
    $log.log_exception e
    exit 1
  rescue => e2
    puts e2.inspect
    e2.backtrace.each do |loc|
      puts "  " + loc
    end
    puts " while logging " + e.inspect
    e.backtrace.each do |loc|
      puts "  " + loc
    end
    exit 1
  end
end
