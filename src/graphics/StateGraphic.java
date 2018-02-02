package graphics;

import java.awt.Color;
import javax.swing.JComponent;
import automata.State;

public class StateGraphic extends JComponent {

	private static final long serialVersionUID = 1L;
	
	public int x;
	public int y;
	public int diameter;       // including border
	public int borderWidth;
	
	private boolean selected;
	private State state;
	
	private Color circleColor;
	private Color circleBorderColor;
	private Color selectedCircleColor;
	private Color selectedBorderColor;

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
		
		circleColor = new Color(255, 255, 255);          // white
		circleBorderColor = new Color(0, 0, 0);          // black border
		selectedCircleColor = new Color(175, 200, 250);  // pale blue
		selectedBorderColor = new Color(0, 0, 0);        // black border
	}
	
	public void setLocation(int newX, int newY) {
		x = newX;
		y = newY;
		GraphicsTest.debug("Location of " + state.getName() + " set to " + x + "," + y);
	}
	
	public void select() {
		selected = true;
	}
	
	public void deselect() {
		selected = false;
	}
	
	public int getDiameter() {
		return diameter;
	}
	
	public String getName() {
		return state.getName();
	}
	
	public Color getBorderColor() {
		if (selected)
			return selectedBorderColor;
		return circleBorderColor;
	}
	
	public Color getCircleColor() {
		if (selected)
			return selectedCircleColor;
		return circleColor;
	}
}
