# AUthor: Collin May

require_relative '../subsystem.rb'

module Subsystems
  module Drive
    class SkidSteer < Subsystem
      def initialize(name)
        super()
        @name = name
      end

      def self.from_json(hash)
        return SkidSteer.new(hash["name"])
      end

      def numeric_type
        1
      end

      def read(sock)
        parts = sock.read(4).unpack("s>s>")
        left = parts[0]
        right = parts[1]
      end
      
      attr_reader :name
    end
  end
end
