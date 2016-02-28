# Author: Collin May

module Subsystems
  class Subsystem
    def initialize
      @driver = nil
    end

    attr_accessor :driver
  end
end
