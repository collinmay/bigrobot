package whs.botdriver.desktop.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;

public class GraphWidget extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8197880747849492898L;

	private List<Point> points;
	
	private String label;
	
	public GraphWidget(String label) {
		this.points = new LinkedList<Point>();
		this.setMaximumSize(new Dimension(1000, 100));
		this.label = label;
	}
	
	private double getXWidth() {
		return this.getWidth() * 100; // 100 milliseconds per pxiel
	}
	
	public synchronized void shift(double x) {
		for(ListIterator<Point> i = points.listIterator(); i.hasNext();) {
			Point p = i.next();
			p.x-= x;
			if(p.x < -getXWidth()) {
				i.remove();
			}
		}
		this.repaint();
	}
	
	public synchronized void push(double x, double y) {
		this.points.add(new Point(x, y));
		this.repaint();
	}
	
	@Override
	public synchronized void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		if(points.size() > 1) {
			g.setColor(Color.BLACK);
			
			g.drawString(this.label, 40, 12);
			
			boolean isFirst = true;
			double x = 0;
			double y = 0;
			
			double yScale = (double) getHeight() / points.stream().mapToDouble((Point p) -> {
				return p.y;
			}).max().getAsDouble();
			
			g.drawString("0", 0, getHeight());
			g.drawString(Integer.toString((int) (getHeight() / yScale)), 0, 12);
			
			for(Iterator<Point> i = points.iterator(); i.hasNext();) {
				Point p = i.next();
				if(!isFirst) {
					g.drawLine((int) (x/100.0) + getWidth(), (int) (y*yScale), (int) (p.x/100.0) + getWidth(), (int) (p.y*yScale));
				}
				isFirst = false;
				x = p.x;
				y = p.y;
			}
		}
	}
	
	private class Point {
		public double x;
		public double y;
		
		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
