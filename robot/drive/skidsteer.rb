# AUthor: Collin May

require_relative '../subsystem.rb'

module Subsystems
  module Drive
    class SkidSteer < Subsystem
      def initialize(name)
        @name = name
      end

      def self.from_json(hash)
        return SkidSteer.new(hash["name"])
      end

      def numeric_type
        1
      end
      
      attr_reader :name
    end
  end
end
