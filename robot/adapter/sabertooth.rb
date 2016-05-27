# Author: Collin May

module Adapters
  class Sabertooth
    def initialize(connection, addr=128)
      @connection = connection
      @m1 = Motor.new(self, 1)
      @m2 = Motor.new(self, 2)
      @addr = addr
    end
    
    def self.from_json(hash)
      if !hash["connection"] then
        throw "no connection specified"
      end

      conn = nil
      
      case hash["connection"]
      when "dummy"
        conn = Connections::DummyConnection.new
      else
        throw "invalid connection type '" + hash["connection"] + "'"
      end

      if !hash["outputs"] then
        throw "no outputs specified"
      end
      
      hash["outputs"].each do |output, name|
        @motors[name] = Motor.new(self, output)
      end
    end

    def [](name)
      return @motors[name]
    end

    attr_reader :connection
    attr_reader :motors
    attr_reader :m1
    attr_reader :m2
    attr_reader :addr
    
    def conn
      @connection
    end
    
    class Motor
      def initialize(sabertooth, output)
        @st = sabertooth
        @out_id = output
        @in_speed = 0
        @out_speed = 0
        @current = 0
        @temperature = 0
      end

      def drive(speed)
        @in_speed = speed
        @st.conn.set(@st.addr, "M", @out_id, speed, 0)
        @out_speed = speed
      end
      
      attr_reader :in_speed
      attr_reader :out_speed
      attr_reader :current
      attr_reader :temperature
    end
    
    module Connections
      class DummyConnection
      end

      class TextConnection
        def initialize(io)
          @io = io
        end

        def set(addr, type, num, val, flags)
          @io.puts type + num.to_s + ": " + (val*2047).floor.to_s
          puts "write update"
        end
      end

      # probably broken
      class PacketConnection
        def initialize(io)
          @io = io
        end

        SET_VALUE	= 0x00
        SET_KEEPALIVE	= 0x10
        SET_SHUTDOWN	= 0x20
        SET_TIMEOUT	= 0x40

        CMD_SET = 40
        CMD_GET = 41
        CMD_GET_REPLY = 73

        GET_VALUE	= 0x00
        GET_BATTERY	= 0x10
        GET_CURRENT	= 0x20
        GET_TEMPERATURE	= 0x40
        
        def set_power(addr, id, speed)
          set(addr, "P", id, speed*16383, SET_VALUE)
        end
        
        def set_timeout(addr, ms)
          set(addr, "M", "*".ord, ms, SET_TIMEOUT)
        end
        
        def keepalive(addr)
          set(addr, "M", "*".ord, 0, SET_KEEPALIVE)
        end
        
        def set(addr, type, num, val, flags)
          val = val.floor
          if val < -16383 then
            val = -16383
          end
          if val > 16383 then
            val = 16383
          end
          if val < 0 then
            val = -val
            flags = flags | 1
          end

          data = [
            (val >> 0) & 0x7f,
            (val >> 7) & 0x7f,
            type.ord,
            num].pack("CCCCC")

          command(addr, CMD_SET, flags, data)
        end

        def get(addr, type, num, flags)
          command(addr, CMD_GET, flags, [
                    type.ord,
                    num
                  ].pack("CC"))
          
        end
        
        def command(addr, type, value, data)
          packet = [
            addr,
            type,
            value,
            (addr+type+value) & 0x7f].pack("CCCC") + data + (data == "" ? "" : (data.each_byte.inject(0, :+) & 0x7f).chr)

          @io.write(packet)
        end
      end
    end
  end
end
