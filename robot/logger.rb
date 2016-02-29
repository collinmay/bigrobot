# Author: Collin May

class Logger
  def log_exception(e)
    current = e
    log current.inspect
    while current do
      current.backtrace.each do |loc|
        log "  " + loc
      end
      current = current.cause
      if current then
        log "Caused by: " + current.inspect
      end
    end
  end

  def log
  end
  
  def sub(prefix)
    logger = PrefixLogger.new
    logger.prefix = prefix
    logger.add_target(self)
    return logger
  end
end

class PrefixLogger < Logger
  def initialize(*targets)
    @targets = targets
    @prefix = ""
  end

  def add_target(tgt)
    @targets.push tgt
  end
  
  def log(msg)
    @targets.each do |tgt|
      tgt.log @prefix + msg
    end
  end
  
  attr_accessor :prefix
end

class TimeLogger < Logger
  def initialize(fmt, *targets)
    @targets = targets
    @fmt = fmt
  end

  def add_target(tgt)
    @targets.push tgt
  end

  def remove_target(tgt)
    @targets.delete tgt
  end
  
  def log(msg)
    prefix = Time.now.strftime(@fmt)
    
    @targets.each do |tgt|
      tgt.log prefix + msg
    end
  end
end

class FileLogger < Logger
  def initialize(io)
    @io = io
  end

  def log(msg)
    @io.puts msg
    @io.flush
  end
end

$log = TimeLogger.new("[%b %e %_Y %r] ", FileLogger.new($stdout), FileLogger.new(File.open("log", "a")))
