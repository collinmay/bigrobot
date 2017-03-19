package whs.botdriver;

import whs.botdriver.events.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class NetRobot implements Robot {
  private String name;

  private SocketChannel socket;
  private ByteBuffer outBuffer; //only the main thread may access this
  private ByteBuffer inBuffer; //only the main thread may access this
  private ByteBuffer headBuffer; //only the main thread may access this
  private Semaphore outMutex; //used to ensure that the keep alive thread and the main thread don't write at the same time

  private Thread keepaliveThread;
  private Thread inputThread;
  private Thread outputThread;

  private long lastPacketTime = -1;

  private boolean isDead = false; //used to terminate threads
  private Throwable kill;

  private int currentPacketLength;

  private NetSubsystem[] subsystems;

  private BlockingQueue<Event> queue;

  public NetRobot(RobotStub stub) throws IOException {
    this.name = stub.getName();
    this.kill = null;
    this.queue = new LinkedBlockingQueue<>();
    this.socket = SocketChannel.open(new InetSocketAddress(stub.getAddr(), stub.getPort()));
    this.socket.configureBlocking(true);
    this.socket.setOption(StandardSocketOptions.TCP_NODELAY, true);

    this.outBuffer = ByteBuffer.allocate(4096);
    this.inBuffer = ByteBuffer.allocate(4096);
    this.headBuffer = ByteBuffer.allocate(5);
    this.outMutex = new Semaphore(1);
    this.currentPacketLength = -1;

    this.keepaliveThread = new Thread(() -> {
      ByteBuffer kaBuffer = ByteBuffer.allocate(9);

      try {
        while(!isDead) {
          kaBuffer.putLong(System.currentTimeMillis());
          sendPacket(1, kaBuffer);

          Thread.sleep(100);
        }
      } catch(Exception e) {
        kill(e);
      }
    });
    this.keepaliveThread.setName("NetRobot keepalive thread");
    this.keepaliveThread.start();

    this.outputThread = new Thread(() -> {
      try {
        while(!isDead) {
          synchronized(NetRobot.this) {
            if(subsystems != null) {
              for(NetSubsystem sub : subsystems) {
                sub.netTick();
              }
            }
          }
          Thread.sleep(10);
        }
      } catch(InterruptedException e) {
      } catch(Exception e) {
        kill(e);
      }
    });
    this.outputThread.setName("NetRobot output thread");
    this.outputThread.start();

    this.inputThread = new Thread(() -> {
      while(!isDead) {
        runInput();
      }
    });
    this.inputThread.setName("NetRobot input thread");
    this.inputThread.start();
  }

  @Override
  public BlockingQueue<Event> getEventQueue() {
    return queue;
  }

  public String getName() {
    return name;
  }

  public long lastPacketReceived() {
    return lastPacketTime;
  }

  @Override
  public synchronized void pushEvent(Event e) {
    queue.add(e);
    this.notifyAll();
  }

  private void sendPacket(int type) {
    sendPacket(type, this.outBuffer);
  }

  private void sendPacket(int type, ByteBuffer buf) {
    try {
      this.outMutex.acquire();
      buf.flip();
      this.headBuffer.putShort((short) buf.limit());
      this.headBuffer.put((byte) type);
      this.headBuffer.flip();

      while(this.headBuffer.hasRemaining()) {
        this.socket.write(this.headBuffer);
      }
      while(buf.hasRemaining()) {
        this.socket.write(buf);
      }

      this.headBuffer.clear();
      buf.clear();
    } catch(Exception e) {
      kill(e);
    } finally {
      this.outMutex.release();
    }
  }

  public void sendSubsystemPacket(NetSubsystem subsystem, ByteBuffer buf) {
    try {
      this.outMutex.acquire();
      buf.flip();
      this.headBuffer.putShort((short) (buf.limit() + 2));
      this.headBuffer.put((byte) 5);
      this.headBuffer.putShort((short) subsystem.getId());
      this.headBuffer.flip();

      while(this.headBuffer.hasRemaining()) {
        this.socket.write(this.headBuffer);
      }
      while(buf.hasRemaining()) {
        this.socket.write(buf);
      }

      this.headBuffer.clear();
      buf.clear();
    } catch(Exception e) {
      kill(e);
    } finally {
      this.outMutex.release();
    }
  }

  @Override
  public synchronized void querySubsystems() {
    sendPacket(2);
  }

  @Override
  public synchronized void registerDriver(String name) {
    this.outBuffer.putShort((short) name.length());
    this.outBuffer.put(name.getBytes());
    sendPacket(3);
  }

  @Override
  public synchronized void bindSubsystem(Subsystem sub) {
    this.outBuffer.putShort((short) sub.getId());
    sendPacket(4);
  }


  @Override
  public synchronized void unbindSubsystem(Subsystem sub) {
    this.outBuffer.putShort((short) sub.getId());
    sendPacket(6);
  }

  @Override
  public void dispose() {
    this.isDead = true;
    try {
      this.socket.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
    this.inputThread.interrupt();
    this.keepaliveThread.interrupt();
  }

  @Override
  public void kill(Throwable t) {
    this.kill = t;
    dispose();
  }

  @Override
  public boolean checkStatus() throws RobotKilledException {
    if(this.kill != null) {
      throw new RobotKilledException(this.kill);
    }
    return !this.isDead;
  }

  public void runInput() {
    try {
      //inBuffer.clear();
      socket.read(inBuffer);
      inBuffer.flip();
      if(this.currentPacketLength < 0) {
        if(this.inBuffer.remaining() >= 2) {
          this.currentPacketLength = this.inBuffer.getShort();
        }
      }
      if(this.currentPacketLength >= 0 && this.inBuffer.remaining() >= this.currentPacketLength + 1) {
        lastPacketTime = System.currentTimeMillis();

        int type = inBuffer.get();
        int start = inBuffer.position();

        switch(type) {
          case 0:
            System.out.println("Got invalid 0 packet");
            break;
          case 1: // Keep Alive
            this.pushEvent(new PingEvent(inBuffer.getLong(), System.currentTimeMillis()));
            break;
          case 2: // Subsystem Info
            synchronized(this) {
              int num_subsystems = inBuffer.getShort();
              NetSubsystem[] subsystems = new NetSubsystem[num_subsystems];

              System.out.println("Read subsystems");
              for(int i = 0; i < num_subsystems; i++) {
                subsystems[i] = NetSubsystem.read(this, i, inBuffer);
                System.out.println("Read subsystem " + subsystems[i].getName());
              }

              this.subsystems = subsystems;
              this.pushEvent(new SubsystemUpdateEvent(this.subsystems));
            }
            break;
          case 3: { // Subsystem Bind Failiure
            Subsystem sub = subsystems[inBuffer.getShort()];
            sub.pushEvent(new SubsystemBindFailiureEvent(sub));
            break;
          }
          case 4: { // Subsystem Bind Success
            Subsystem sub = subsystems[inBuffer.getShort()];
            sub.pushEvent(new SubsystemBindSuccessEvent(sub));
            break;
          }
          case 5: { // Battery Update

          }
          case 8: // Log Message
            int len = inBuffer.getShort();
            byte[] bytes = new byte[len];
            inBuffer.get(bytes);
            String msg = new String(bytes);
            this.pushEvent(new whs.botdriver.LogEvent(msg));
            break;
          default:
            System.out.println("Got unknown packet 0x" + Integer.toHexString(type) + " length 0x" + Integer.toHexString(this.currentPacketLength));
            break;
        }

        inBuffer.position(start + this.currentPacketLength);

        this.currentPacketLength = -1;
      }
      inBuffer.compact();
    } catch(IOException e) {
      kill(e);
    }
  }
}
