# Author: Collin May

class Robot
  def initialize
    @subsystems = []
    @batteries = []
    @adapters = {}
  end

  def add_subsystem(sub)
    @subsystems.push sub
  end
  
  def add_battery(bat)
    @batteries.push bat
  end

  def add_adapter(name, adapter)
    @adapters[name] = adapter
  end

  def resolve_motor(name)
    idx = name.index "."
    adapter_name = name[0, idx]
    motor_name = name[idx, name.length]
    return @adapters[adapter_name][motor_name]
  end

  attr_reader :subsystems
  attr_reader :batteries
  attr_reader :adapters
end
