package graphics;

import java.awt.Color;
import javax.swing.JComponent;
import automata.State;

public class StateGraphic extends JComponent {

	private static final long serialVersionUID = 1L;
	
	public int x;              // center coordinates
	public int y;
	public int diameter;       // including border
	public int borderWidth;
	
	private boolean selected;
	private Color currentColor;
	private State state;
	
	public static final Color borderColor = new Color(0, 0, 0);               // black
	public static final Color circleColor = new Color(255, 255, 255);         // white
	public static final Color selectedCircleColor = new Color(175, 200, 250); // blue
	public static final Color rejectedCircleColor = new Color(255, 50, 50);   // red
	public static final Color acceptedCircleColor = new Color(40, 255, 70);   // green

	public StateGraphic() {
		state = new State();
		init();
	}
	public StateGraphic(State s) {
		state = s;
		init();
	}
	
	private void init() {
		y = x = 25;
		diameter = 50;
		borderWidth = 2;
		selected = false;
		currentColor = circleColor;
	}
	
	public void setLocation(int newX, int newY) {
		x = newX;
		y = newY;
		GraphicsTest.debug("Location of " + state.getName() + " set to " + x + "," + y);
	}
	
	public void select() {
		currentColor = selectedCircleColor;
		selected = true;
	}
	
	public void deselect() {
		currentColor = circleColor;
		selected = false;
	}
	
	public int getDiameter() {
		return diameter;
	}
	
	public String getName() {
		return state.getName();
	}
	
	public State getState() {
		return state;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public Color getBorderColor() {
		return borderColor;
	}
	
	public Color getCircleColor() {
		return currentColor;
	}
	public void accept() {
		currentColor = acceptedCircleColor;
	}
	public void reject() {
		currentColor = rejectedCircleColor;
	}
}
