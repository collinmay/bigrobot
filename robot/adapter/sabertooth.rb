# Author: Collin May

require_relative "../sensors.rb"

module Adapters
  class Sabertooth
    def initialize(connection, addr=128)
      @connection = connection
      @m1 = Motor.new(self, 1)
      @m2 = Motor.new(self, 2)
      @addr = addr
    end

    attr_reader :connection
    attr_reader :m1
    attr_reader :m2
    attr_reader :addr

    def name
      "sabertooth"
    end
    
    def conn
      @connection
    end
    
    class Motor
      def initialize(sabertooth, output)
        @st = sabertooth
        @out_id = output

        name = @st.name + " M" + output.to_s
        
        @sensors = {
          :battery => Sensors::Battery.new(name + " battery", 24),
          :temperature => Sensors::Thermometer.new(name + " thermometer"),
          :current => Sensors::Ammeter.new(name + " ammeter"),
          :pwm => Sensors::PWM.new(name + " pwm")
        }
        @in_speed = 0
        @out_speed = 0
      end

      def drive(speed)
        @in_speed = speed
        @st.conn.set(@st.addr, "M", @out_id, speed, 0)
        @sensors[:pwm].set(speed)
      end
      
      attr_reader :in_speed
      attr_reader :out_speed
      attr_reader :sensors
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
        end
      end
    end
  end
end
