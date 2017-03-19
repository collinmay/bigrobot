# AUthor: Collin May

require_relative '../subsystem.rb'

module Subsystems
  module Drive
    class SkidSteer < Subsystem
      def initialize(name, left, right)
        super()
        @name = name
        @left_motor = left
        @right_motor = right
      end

      def self.from_json(robot, hash)
        if !hash["left"] then
          throw "no left motor specified"
        end
        if !hash["right"] then
          throw "no right motor specified"
        end
        
        left = $robot.resolve_motor(hash["left"])
        right = $robot.resolve_motor(hash["right"])
        
        return SkidSteer.new(hash["name"])
      end

      def numeric_type
        1
      end

      def pack_metadata
        ([@left_motor, @right_motor].map do |motor|
          ([:battery, :temperature, :current, :pwm].map do |sensor|
            next motor.sensors[sensor].id
          end).pack("S>*")
        end).join(String.new)
      end

      def read(sock)
        parts = sock.read(4).unpack("s>s>")
        left = parts[0]/2048.0
        right = parts[1]/2048.0
        @left_motor.drive(left)
        @right_motor.drive(right)
      end

      def safe_mode
        $log.log "{" + @name + "} safe mode"
        @left_motor.drive(0)
        @right_motor.drive(0)
      end
      
      def run_update
        @controller.poll do
          #left_current
          #left_temp
          #left_speed
          #right_current
          #right_temp
          #right_speed
        end
      end
      
      attr_reader :name
    end
  end
end
