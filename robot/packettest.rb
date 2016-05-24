require 'socket'

server = TCPServer.new 25602
socket = server.accept

loop do
  size = socket.read(2).unpack("S>")[0]
  type = socket.read(1).unpack("C")[0]
  
  packet = socket.read(size)

  puts "got packet of type 0x" + type.to_s(16) + " length " + size.to_s
end
