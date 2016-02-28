package whs.botdriver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import whs.botdriver.events.Event;
import whs.botdriver.events.PingEvent;
import whs.botdriver.events.SubsystemUpdateEvent;

public class NetRobot implements Robot {
	private String name;
		
	private SocketChannel socket;
	private ByteBuffer outBuffer; //only the main thread may access this
	private ByteBuffer inBuffer; //only the main thread may access this
	private ByteBuffer headBuffer; //only the main thread may access this
	private Semaphore outMutex; //used to ensure that the keep alive thread and the main thread don't write at the same time
	
	private Thread keepaliveThread;
	private Thread inputThread;
	
	private long lastPacketTime = -1;
	
	private boolean isDead = false; //used to terminate threads
	private Throwable kill;
	
	private int currentPacketLength;
	
	private Subsystem[] subsystems;
	
	private Queue<Event> queue;
	
	public NetRobot(RobotStub stub) throws IOException {
		this.name = stub.getName();
		this.kill = null;
		this.queue = new ConcurrentLinkedQueue<Event>();
		this.socket = SocketChannel.open(new InetSocketAddress(stub.getAddr(), 25600));
		this.socket.configureBlocking(true);
		this.socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
		
		this.outBuffer = ByteBuffer.allocate(4096);
		this.inBuffer = ByteBuffer.allocate(4096);
		this.headBuffer = ByteBuffer.allocate(3);
		this.outMutex = new Semaphore(1);
		this.currentPacketLength = -1;
				
		this.keepaliveThread = new Thread(new Runnable() {
			@Override
			public void run() {
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
			}
		});
		this.keepaliveThread.start();
		
		this.inputThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!isDead) {
					runInput();
				}
			}
		});
		this.inputThread.start();
	}
	
	public Queue<Event> getEventQueue() {
		return queue;
	}
	
	public String getName() {
		return name;
	}
	
	public long lastPacketReceived() {
		return lastPacketTime;
	}
	
	private synchronized void pushEvent(Event e) {
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

			while(this.headBuffer.hasRemaining()) { this.socket.write(this.headBuffer); }
			while(            buf.hasRemaining()) { this.socket.write(buf);             }

			this.headBuffer.clear();
			buf.clear();
		} catch(Exception e) {
			kill(e);
		} finally {
			this.outMutex.release();
		}
	}
	
	public void querySubsystems() {
		sendPacket(2);
	}
	
	public void registerDriver(String name) {
		this.outBuffer.putShort((short) name.length());
		this.outBuffer.put(name.getBytes());
		sendPacket(3);
	}
	
	public void dispose() {
		this.isDead = true;
	}
	
	public void kill(Throwable t) {
		this.kill = t;
		this.isDead = true;
	}

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
					System.out.println("Got invalid 0 packet"); break;
				case 1: // Keep Alive
					this.pushEvent(new PingEvent(inBuffer.getLong(), System.currentTimeMillis())); break;
				case 2: // Subsystem Info
					synchronized(this) {
						int num_subsystems = inBuffer.getShort();
						Subsystem[] subsystems = new Subsystem[num_subsystems];
						
						for(int i = 0; i < num_subsystems; i++) {
							subsystems[i] = Subsystem.read(i, inBuffer);
						}
						
						this.subsystems = subsystems;
						this.pushEvent(new SubsystemUpdateEvent(this.subsystems));
					}
					break;
				default:
					System.out.println("Got unknown packet 0x" + Integer.toHexString(type) + " length 0x" + Integer.toHexString(this.currentPacketLength)); break;
				}
				
				inBuffer.position(start + this.currentPacketLength);
				
				this.currentPacketLength = -1;
			}
			inBuffer.compact();
		} catch(IOException e) {
			kill(e);
		}
	}
	
	public class KilledException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4605554839526934795L;
		
		public KilledException(Throwable t) {
			super(t);
		}
	}
}
