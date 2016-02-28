# Author: Collin May
# Description: Abstract the Sabertooth motor driver packet protocol to make it actually usable
# See also: https://www.dimensionengineering.com/datasheets/USBSabertoothPacketSerialReference.pdf

require 'thread'

module Adapters
  class Sabertooth
    class Packet
      def initialize(command, value, data)
        @command = command
        @value = value
        @data = data
      end
      
      def self.read(io, addr)
        header = io.read(4).unpack("CCCC")
        if(header[0] != addr) then
          throw "received packet from another sabertooth #{packet[0]}"
        end
        command = packet[1]
        value = packet[2]
        expected_checksum = (packet[0] + packet[1] + packet[2]) & 0b1111111
        if expected_checksum != packet[3] then
          throw "invalid checksum (expected #{expected_checksum}, got #{packet[3]})"
        end
        
        if command != 73 then
          throw "invalid command number (got #{command}, expected 73)"
        end
        
        data = io.read(4)
        expected_data_checksum = 0
        data.each_byte do |b|
          expected_data_checksum+= b
        end
        expected_data_checksum = expected_data_checksum % 0b1111111
        data_checksum = io.read(1).unpack("C")
        if expected_data_checksum != data_checksum then
          throw "invalid data checksum (expected #{expected_data_checksum}, got #{data_checksum})"
        end
        
        return Packet.new(command, value, data)
      end
    
      def write(io, addr)
        addr|= 0b10000000
        control_checksum = (addr + command + value) & 0b1111111 # low 7 bits of sum, as per specification
        data_checksum = 0
        data.each_byte do |b|
          data_checksum+= b
        end
        data_checksum = data_checksum % 0b1111111
        
        packet = [addr, command, value, control_checksum].pack("CCCC")
        if data then
          packet+= data
          packet+= data_checksum.ord
        end
        
        io.write(packet)    
      end
      
      attr_reader :command
      attr_reader :value
      attr_reader :data
    end
    
    
    # @param io 		serial port connected to the sabertooth
    # @param addr 	the address of the sabertooth. by default, 128
    def initialize(io, address=128)
      @io = io
      @addr = address
      
      @packet = nil
      
      @value = Hash.new
      @battery = Hash.new
      @current = Hash.new
      @temperature = Hash.new
      @mutex = Mutex.new
      
      @thread = Thread.new do # this thread reads data from the sabertooth
        packet = Packet.read(@io, @addr)
        
        data_parts = packet.data.unpack("CCa2")
        response_value = (data_parts[0] | (data_parts[1] << 7)) * ((packet.value & 1 == 0) ? 1 : -1)
        response_source = data_parts[2]
        
        type = packet.value & 0b1111110
        @mutex.synchronize do
          puts "got #{type} for #{response_source} = #{response_value}"
          if type == 0 then
            @value[response_source] = response_value
          else if type == 16 then
            @battery[response_source] = response_value
        else if type == 32 then
          @current[response_source] = response_value
          else if type == 64 then
            @temperature[response_source] = response_value
          else
            throw "invalid type #{type}"
          end
        end
      end
    end
    
    # send a basic command to the sabertooth
    def send_command(command, value, data)
      Packet.new(command, value, data).write(@io, @addr)
    end
    
    def set_command(type, target, value)
      data = String.new
      data+= target
      if(value < 0) then
        value = -value
        data+= (value >> 0) & 0b1111111
        data+= (value >> 7) & 0b1111111
        send_command(40, type + 1, data)
      else
        data+= (value >> 0) & 0b1111111
        data+= (value >> 7) & 0b1111111
      send_command(40, type + 0, data)
      end
    end
  
    # set a value on the sabertooth
    # value is one of M1, M2, MD, MT, P1, P2, Q1, Q2, R1, R2, T1, or T2
    def set(target, value)
      set_command(0, target, value)
    end
    
    def keepalive
      set_command(16, "M*", 0)
    end
    
    def shutdown(channel)
      set_command(32, channel, 1)
    end
    
    def poweron(channel)
      set_command(32, channel, 0)
    end
    
    def timeout(ms)
      set_command(64, "M*", ms)
    end
    
    def get_command(type, target)
      send_command(41, type, target)
    end
    
    def query(target)
      get_command(0, target)
    end
    
    def query_battery(target)
      get_command(16, target)
    end
    
    def query_current(target)
      get_command(32, target)
    end
    
    def query_temperature(target)
      get_command(64, target)
    end
  end
end
