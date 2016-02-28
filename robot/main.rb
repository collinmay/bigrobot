# Author: Collin May

require 'socket'
require 'ipaddr'
require 'json'

require_relative 'logger.rb'
require_relative 'driver.rb'
require_relative 'robot.rb'
require_relative 'adapter/sabertooth.rb'

require_relative 'drive/skidsteer.rb'

$log.log "Starting up..."

begin
  $robot = Robot.new
  
  $log.log "Loading config..."
  config_hash = JSON.parse(File.read("config.json"))
  
  adapters = Hash.new
  
  if config_hash.has_key? "adapters" then
    config_hash["adapters"].each do |name, adapter_hash|
      if !adapter_hash.has_key? "type" then
        throw "adapter declaration has no type"
      end
      case adapter_hash["type"]
      when "sabertooth"
        adapters[name] = Adapters::Sabertooth.from_json(adapter_hash)
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
        $robot.add_subsystem Subsystems::Drive::SkidSteer.from_json(sub_hash)
      else
        throw "unknown subsystem type '" + sub_hash["type"] + "'"
      end
    end
  end
  
  discovery_sock = UDPSocket.new
  discovery_group = IPAddr.new("238.160.102.2").hton + IPAddr.new("0.0.0.0").hton
  
  discovery_sock.setsockopt(:IPPROTO_IP, :IP_ADD_MEMBERSHIP, discovery_group)
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
  $log.log_exception e
  exit 1
end
