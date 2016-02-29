# Author: Collin May

module Subsystems
  class Subsystem
    def initialize()
      @driver = nil
      @id = $robot.subsystems.length
    end

    def attempt_bind(driver)
      if @driver == nil then
        @driver = driver
        @driver.subsystem_bound(self)
        return true
      else
        return false
      end
    end

    def unbind
      @driver = nil
    end
    
    attr_accessor :driver
    attr_reader :id
  end
end
