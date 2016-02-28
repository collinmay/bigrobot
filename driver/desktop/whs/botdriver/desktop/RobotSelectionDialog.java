package whs.botdriver.desktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import whs.botdriver.RobotStub;

public class RobotSelectionDialog extends JDialog implements WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7618446332191144511L;

	private DefaultListModel<RobotStub> model;
	private HashMap<InetAddress, RobotStub> stubmap = new HashMap<InetAddress, RobotStub>();
	
	private boolean searching = false;
	private Thread searchThread;
	private DatagramSocket searchSocket;
	
	private RobotStub selectedRobot;
	
	private static final String MAGIC_MESSAGE = "find robots";
	private static final String MULTICAST_GROUP = "238.160.102.2";
	private static final int MULTICAST_PORT = 25601;
	
	public RobotSelectionDialog() {
		super();
		
		this.setTitle("Select robot...");
		
		model = new DefaultListModel<RobotStub>();
		JList<RobotStub> list = new JList<RobotStub>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) { // double clicks only
					Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
					if (r != null && r.contains(e.getPoint())) {
						int index = list.locationToIndex(e.getPoint());
						
						RobotSelectionDialog.this.selectedRobot = list.getModel().getElementAt(index);
						RobotSelectionDialog.this.stop();
					}
				}
			}
		});
		
		JScrollPane scroller = new JScrollPane(list);
		scroller.setPreferredSize(new Dimension(200, 300));

		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("Select a robot to drive");
		label.setHorizontalAlignment(JLabel.CENTER);
		listPane.add(label);
		listPane.add(Box.createVerticalStrut(5));
		listPane.add(scroller);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		JButton cancel = new JButton("Cancel");
		JButton drive = new JButton("Drive");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RobotSelectionDialog.this.selectedRobot = null;
				RobotSelectionDialog.this.stop();
			}
		});
		drive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RobotSelectionDialog.this.selectedRobot = list.getSelectedValue();
				RobotSelectionDialog.this.stop();
			}
		});
		
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancel);
		buttonPane.add(Box.createHorizontalStrut(5));
		buttonPane.add(drive);
		
		this.getContentPane().setLayout(new BorderLayout());		
		this.getContentPane().add(listPane, BorderLayout.CENTER);
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		this.addWindowListener(this);
	}
	
	public RobotStub open() throws IOException {
		this.pack();
		this.setVisible(true);
		this.setLocation(200, 200);
		
		DatagramSocket sock = new DatagramSocket();
		searchSocket = sock;
		searchThread = Thread.currentThread();
		
		byte[] sendbuf = MAGIC_MESSAGE.getBytes();
		DatagramPacket ping = new DatagramPacket(sendbuf, sendbuf.length, InetAddress.getByName(MULTICAST_GROUP), MULTICAST_PORT);
		byte[] recvbuf = new byte[256];
		DatagramPacket pong = new DatagramPacket(recvbuf, recvbuf.length);
		sock.setSoTimeout(1000);
		
		searching = true;
		
		while(searching) {
			try {
				sock.send(ping);
				sock.receive(pong);
				if(!stubmap.containsKey(pong.getAddress())) {
					RobotStub stub = new RobotStub(pong);
					stubmap.put(pong.getAddress(), stub);
					model.addElement(stub);
				}
				Thread.sleep(1000);
			} catch(SocketTimeoutException e) {
			} catch(InterruptedException e) {}
		}
		
		this.setVisible(false);
		this.dispose();
		
		return selectedRobot;
	}

	private void stop() {
		if(this.searching) {
			this.searching = false;
			this.searchThread.interrupt();
			this.searchSocket.close();
		}
	}
	
	@Override public void windowClosed(WindowEvent e) { }
	@Override public void windowOpened(WindowEvent e) { }
	@Override public void windowClosing(WindowEvent e) {
		this.selectedRobot = null;
		this.stop();
	}
	@Override public void windowIconified(WindowEvent e) { }
	@Override public void windowDeiconified(WindowEvent e) { }
	@Override public void windowActivated(WindowEvent e) { }
	@Override public void windowDeactivated(WindowEvent e) { }
}
