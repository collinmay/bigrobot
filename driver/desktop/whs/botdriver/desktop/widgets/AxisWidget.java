package whs.botdriver.desktop.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

public class AxisWidget extends JComponent {
	private double value;
	
	public AxisWidget() {
		this.setPreferredSize(new Dimension(10, 10));
	}
	
	public void setValue(double v) {
		this.value = v;
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int halfHeight = getHeight()/2;
		int val = (int) (value * halfHeight);
				
		g.setColor(Color.GREEN);
		if(value > 0) {
			g.fillRect(0, halfHeight - val, getWidth(), val);
		}
		if(value < 0) {
			g.fillRect(0, halfHeight, getWidth(), -val);
		}
	}
}
