package whs.botdriver.desktop.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CoordWidget extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7318098608102222567L;
	private double x;
	private double y;
	private boolean hasJoystick;
	private boolean diagonalAxes;
	
	public CoordWidget() {
		this(false);
	}
	
	public CoordWidget(boolean diagonalAxes) {
		super();
		this.setPreferredSize(new Dimension(200, 200));
		this.x = 0;
		this.y = 0;
		this.hasJoystick = false;
		this.diagonalAxes = diagonalAxes;
	}
	
	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
		this.repaint();
	}
	
	public void setX(double x) {
		this.x = x;
		this.repaint();
	}
	
	public void setY(double y) {
		this.y = y;
		this.repaint();
	}
	
	public void setHasJoystick(boolean has) {
		this.hasJoystick = has;
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int sz = Math.min(getWidth(), getHeight());
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, sz, sz);
		
		if(hasJoystick) {
			g.setColor(Color.BLACK);
			g.drawOval(0, 0, sz, sz);
			g.fillOval((int) (x*(sz/2)) + (sz/2) - 5, (int) (y*(sz/2)) + (sz/2) - 5, 10, 10);
		} else {
			g.setColor(Color.RED);
			String str = "no joystick set";
			g.drawString(str, (sz/2)-(g.getFontMetrics().stringWidth(str)/2), (sz/2)-6);
		}
	}
	
	public static JPanel withLabel(CoordWidget w, String label) {
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(new JLabel(label), BorderLayout.NORTH);
		container.add(w, BorderLayout.CENTER);
		return container;
	}
}
