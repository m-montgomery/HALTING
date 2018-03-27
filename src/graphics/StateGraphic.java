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
	private boolean accepted;
	private boolean rejected;
	private boolean clicked;
	
	private Color currentColor;
	private State state;
	
	// default colors
	public static final Color borderColor = new Color(0, 0, 0);               // black
	public static final Color defaultCircleColor = new Color(255, 255, 255);  // white
	
	// colors specific to state status
	public static final Color selectedCircleColor = new Color(175, 200, 250); // blue
	public static final Color clickedCircleColor  = new Color(240, 240, 150); // tan
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
		selected = accepted = rejected = clicked = false;
		currentColor = defaultCircleColor;
	}
	
	public void setLocation(int newX, int newY) {
		x = newX;
		y = newY;
	}
	
	private void updateColor() {
	    if (clicked)
			currentColor = clickedCircleColor;
		else if (accepted)
			currentColor = acceptedCircleColor;
		else if (rejected)
			currentColor = rejectedCircleColor;
		else if (selected)
			currentColor = selectedCircleColor;
		else
			currentColor = defaultCircleColor;
	}
	
	public void resetStatus() {
		selected = accepted = rejected = clicked = false;
		updateColor();
	}
	
	public void click() {
		clicked = true;
		updateColor();
	}
	public void unclick() {
		clicked = false;
		updateColor();
	}
	public void select() {
		selected = true;
		updateColor();
	}
	public void deselect() {
		selected = false;
		updateColor();
	}
	public void accept() {
		accepted = true;
		updateColor();
	}
	public void reject() {
		rejected = true;
		updateColor();
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
}
