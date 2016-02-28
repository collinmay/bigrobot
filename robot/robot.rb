# Author: Collin May

class Robot
  def initialize
    @subsystems = []
    @batteries = []
  end

  def add_subsystem(sub)
    @subsystems.push sub
  end
  
  def add_battery(bat)
    @batteries.push bat
  end

  attr_reader :subsystems
  attr_reader :batteries
end
