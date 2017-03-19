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

$VERSION = "1.2"
$PROTOCOL_VERSION = 2

$config = {}

$config[:SABERTOOTH_PORT] = "/dev/ttyACM0"
$config[:MULTICAST_IFACE] = "10.0.7.1"

arg_no = 0
while ARGV.length > 0 do
  arg = ARGV.shift
  if arg[0, 2] == "--" then
    if arg == "--dev" then
      $config[:SABERTOOTH_PORT] = "/dev/null"
      $config[:MULTICAST_IFACE] = "0.0.0.0"
    else
      $log.log "Unknown option: " + arg
    end
  else
    $log.log "Too many arguments"
    exit 1
    arg_no+= 1
  end
end

$log.log "Starting up..."

$log.log "Opening sabertooth on " + $config[:SABERTOOTH_PORT]

SABERTOOTH_IO = File.open($config[:SABERTOOTH_PORT], "r+b")

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
  $log.log "Not running as root, not attempting privilege drop"
  begin
    Process::Sys.setuid(0)
  rescue Errno::EPERM
  else
    $log.log "Can escalate privileges to root level?"
    exit 1
  end
end

begin
  $robot = Robot.new
  
  sabertooth = Adapters::Sabertooth.new(Adapters::Sabertooth::Connections::TextConnection.new(SABERTOOTH_IO))
  $robot.add_adapter("sabertooth", sabertooth)
  drive = Subsystems::Drive::SkidSteer.new("Main Drive", sabertooth.m2, sabertooth.m1)
  $robot.add_subsystem drive
  
  discovery_sock = UDPSocket.new
  discovery_group = IPAddr.new("238.160.102.2").hton + IPAddr.new($config[:MULTICAST_IFACE]).hton
  
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

  $log.log "Accepting connections..."
  loop do
    sock = server.accept
    $log.log "Accepted connection from " + sock.remote_address.ip_address
    $robot.drivers.push(Driver.new($robot, sock))
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
