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
	private AxisLines axisLines;
	private Shape shape;
	private String msg;
	
	public CoordWidget() {
		super();
		this.setPreferredSize(new Dimension(200, 200));
		this.x = 0;
		this.y = 0;
		this.msg = "";
		this.axisLines = AxisLines.OFF;
		this.shape = Shape.CIRCLE;
	}
	
	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
		this.repaint();
	}
	
	public void setPolar(double t, double r) {
		this.x = r * Math.cos(t);
		this.y = r * Math.sin(t);
		this.repaint();
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public void setX(double x) {
		this.x = x;
		this.repaint();
	}
	
	public void setY(double y) {
		this.y = y;
		this.repaint();
	}
	
	public void setAxisLines(AxisLines type) {
		this.axisLines = type;
	}
	
	public void setMessage(String string) {
		this.msg = string;
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int sz = Math.min(getWidth(), getHeight());
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, sz, sz);
		
		if(msg == null || msg.equals("")) {
			g.setColor(Color.BLACK);
			
			switch(axisLines) {
			case OFF: break;
			case NORMAL:
				g.drawLine(0, sz/2, sz, sz/2);
				g.drawLine(sz/2, 0, sz/2, sz);
				break;
			case DIAGONAL:
				g.drawLine(0, 0, sz, sz);
				g.drawLine(0, sz, sz, 0);
				break;
			}
			
			switch(shape) {
			case OFF: break;
			case CIRCLE:
				g.drawOval(0, 0, sz, sz);
				break;
			case SQUARE:
				g.drawRect(0, 0, sz, sz);
				break;
			}
			
			g.fillOval((int) (x*(sz/2)) + (sz/2) - 5, (int) (y*(sz/2)) + (sz/2) - 5, 10, 10);
		} else {
			g.setColor(Color.RED);
			g.drawString(msg, (sz/2)-(g.getFontMetrics().stringWidth(msg)/2), (sz/2)-6);
		}
	}
	
	public static JPanel withLabel(CoordWidget w, String label) {
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(new JLabel(label), BorderLayout.NORTH);
		container.add(w, BorderLayout.CENTER);
		return container;
	}
	
	public enum AxisLines {
		OFF, NORMAL, DIAGONAL
	}
	
	public enum Shape {
		OFF, CIRCLE, SQUARE
	}
}
