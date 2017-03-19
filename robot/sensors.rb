module Sensors
  class Sensor
    def initialize(name)
      @name = name
      @id = $robot.sensors.length
      $robot.sensors.push(self)
    end

    attr_reader :name
    attr_reader :id
  end
  
  class OneValueSensor < Sensor
    def set(value)
      @value = value
    end

    def pack_metadata
      String.new
    end

    def pack_values
      [@value].pack("l>")
    end
    attr_reader :value
  end

  class Battery < OneValueSensor
    def initialize(name, max_voltage)
      super(name)
      @max_voltage = max_voltage
    end
    
    def numeric_type
      1
    end

    def pack_metadata
      [@max_voltage].pack("l>")
    end
  end

  class Thermometer < OneValueSensor
    def numeric_type
      2
    end
  end

  class Ammeter < OneValueSensor
    def numeric_type
      3
    end
  end

  class PWM < OneValueSensor
    def numeric_type
      4
    end
  end
end
