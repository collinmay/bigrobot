# Author: Collin May
# Description: Implement a dummy sabertooth so I can test this stuff before our sabertooths actually arrive
# See also: sabertooth.rb

class Nulltooth
  def initialize
    @timeout = 16
    @keepalive = Time.now
    @thread = Thread.new do # this thread pretty much just waits for a serial timeout
      loop do
        sleep (@keepalive - Time.now) + (@timeout/1000.0)
        if (Time.now - @keepalive) * 1000 > @timeout then
          puts "nulltooth: timed out"
        end
      end
    end
    
    @values = Hash.new
  end
  
  def set(output, val)
    @values[output] = val
    puts "nulltooth: set #{output} to #{val}"
  end

  def keepalive
    @keepalive = Time.now
  end

  def shutdown(channel)
    puts "nulltooth: power off #{channel}"
  end

  def poweron(channel)
    puts "nulltooth: power on #{channel}"
  end

  def timeout(ms)
    @timeout = ms
  end

  def query(channel)
    return @values[channel]
  end

  def query_battery(target) # volts
    return 24.0 # fudge our battery level because we don't have a real one to query
  end

  def query_current(target) # amps
    return 3.0 # fudge this, too
  end

  def query_temperature(target) # celsius
    return 18.0 # fudge more values1
  end
end
