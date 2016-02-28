# Author: Collin May

class Battery
  def initialize(name, full_voltage)
    @name = name
    @full_voltage = full_voltage # volts
    @current_voltage = nil
  end

  attr_reader :name
  attr_reader :full_voltage
  attr_reader :current_voltage
end
