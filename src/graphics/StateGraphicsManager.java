package graphics;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;

import automata.State;

public class StateGraphicsManager extends JPanel {

	private static final long serialVersionUID = 5205922192600518812L;
	private ArrayList<StateGraphic> stateGraphics;

	public StateGraphicsManager() {
		stateGraphics = new ArrayList<StateGraphic>();
		addMouseListener(new MouseListener());
	}
	
	public void createStates(ArrayList<State> states) {
		int x = 25;
		int y = 25;
		for (State s : states) {
			StateGraphic state = new StateGraphic(s);
			state.setLocation(x, y);
			stateGraphics.add(state);
			
			// MM: to do: implement algorithm to distribute states within panel
			//x += state.getDiameter()*2;
			y += state.getDiameter()*2;
		}

	}
	
	@Override
	protected void paintComponent(Graphics g) {
		GraphicsTest.debug("In StateGraphicsManager paintComponent()");
		super.paintComponent(g);
		
		// for each state
		for (StateGraphic s : stateGraphics) {
			GraphicsTest.debug("Painting state " + s.getName());
			
			// calculate bounding rectangle upperleft coords
			int rectX = s.x - s.diameter/2;    // s.x, s.y are center coords
			int rectY = s.y - s.diameter/2;    // fillOval needs rect coords
			
			// draw border (larger circle behind main circle)
			int dx = s.borderWidth / 2;        // shift over to center
			g.setColor(s.getBorderColor());
			g.fillOval(rectX - dx, rectY - dx, s.diameter, s.diameter);
			
			// draw main circle
			g.setColor(s.getCircleColor());
			g.fillOval(rectX, rectY, s.diameter - dx*2, s.diameter - dx*2);
			
			// draw state name
			g.setColor(s.getBorderColor());  
			g.drawString(s.getName(), s.x - (s.diameter/6), s.y + (s.diameter/10));
			// MM: to do: will need to find better way to position this ^
		}
		GraphicsTest.debug("");  // newline
	}

	
	class MouseListener extends MouseAdapter {
		
		private Point startingPoint;         // track previous mouse location
		private StateGraphic selectedState;
		
		public MouseListener() {
			startingPoint = null;  // MM: should this have a default initial value?
			selectedState = null;
		}
		
		public void mousePressed(MouseEvent me) { 
	        //System.out.println(me);               // debug
			GraphicsTest.debug("Mouse pressed!");
			startingPoint = me.getPoint();
			
			// save selected state, if any
			StateGraphic s = withinStateBounds(startingPoint);
			if (s != null) {
				
				// deselect previously selected state
				if (selectedState != null)
					selectedState.deselect();
				
				// mark new state as selected
				selectedState = s;
				selectedState.select(); // change state color to show selection
				repaint();
				
				GraphicsTest.debug("Selected state: " + s.getName());
			}
		}
		
		public void mouseDragged(MouseEvent me) {          // MM: to do: check if this registers when using a real mouse
			GraphicsTest.debug("Mouse dragged!");
			
			if (selectedState != null) {
				selectedState.setLocation(me.getX(), me.getY());
				repaint();
			}
		}
		
		public void mouseReleased(MouseEvent me) {
			GraphicsTest.debug("Mouse released!");
			
			// if have selected state and didn't click inside another state
			if (selectedState != null && withinStateBounds(me.getPoint()) == null) { 
				GraphicsTest.debug("Moving state " + selectedState.getName() + 
						" to " + me.getX() + "," + me.getY());
				
				// relocate selected state and deselect it
				selectedState.setLocation(me.getX(), me.getY());
				selectedState.deselect();
				selectedState = null;
				repaint();
			}
		}
	}

	public StateGraphic withinStateBounds(Point point) {
		
		double x = point.getX();
		double y = point.getY();
		GraphicsTest.debug("Checking if " + x + "," + y + " is inside a state");
		
		// if point is within a state's circle, return the state
		for (StateGraphic state : stateGraphics) {
			// calculation to determine if point is inside circle:
			// (point x - state x)^2 + (point y - state y)^2 < (state radius)^2 
			
			double radiusSq = Math.pow(state.diameter / 2, 2);
			if (Math.pow((x-state.x), 2) + Math.pow((y-state.y), 2) < radiusSq)
				return state;
		}
		return null;
	}
}

