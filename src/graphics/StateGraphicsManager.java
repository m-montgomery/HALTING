package graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import automata.Automaton;
import automata.State;
import automata.Transition;

public class StateGraphicsManager extends JPanel {

	private static final long serialVersionUID = 5205922192600518812L;
	
	private ArrayList<StateGraphic> stateGraphics;
	private MainWindow mainWindow;
	private Automaton machine;
	private StateGraphic currentState;

	public StateGraphicsManager(MainWindow win) {
		stateGraphics = new ArrayList<StateGraphic>();
		mainWindow = win;
		machine = null;
		currentState = null;
		
		addMouseListener(new MouseListener());
	}
	
	public void createStates(ArrayList<State> states) {
		int x = 25;
		int y = 25;
		for (State s : states) {
			addState(s, x, y);
			
			// MM: to do: implement algorithm to distribute states within panel
			//x += state.getDiameter()*2;
			y += s.getGraphic().getDiameter()*2;
			if (y > 25 + s.getGraphic().getDiameter()*2) {
				y = 25;
				x = 25 + s.getGraphic().getDiameter()*2;
			}
		}

	}
	
	private void addState(State s, int x, int y) {
		StateGraphic state = new StateGraphic(s);   // make new StateGraphic
		state.setLocation(x, y);                    // set its location
		s.setGraphic(state);                        // set state's StateGraphic
		stateGraphics.add(state);                   // add graphic to manager
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		//GraphicsTest.debug("In StateGraphicsManager paintComponent()");
		super.paintComponent(g);
		
		// MM: TO DO: reorganize paintComponent to call functions for easier
		//            viewing of paintComponent's actions? e.g. drawState()
		
		// draw each state
		for (StateGraphic s : stateGraphics) {
			
			// update selected state if any and if running
			if (machine.getCurrentState() == s.getState() && 
					machine.getStatus() != Automaton.READY) {

				// deselect previous state
				if (currentState != null)
					currentState.deselect();
				currentState = s; 
				
				// update current state status
				if (machine.getStatus() == Automaton.ACCEPT)
					currentState.accept();
				else if (machine.getStatus() == Automaton.REJECT)
					currentState.reject();
				else
					currentState.select();
			}
			
			// calculate bounding rectangle upperleft coords
			int rectX = s.x - s.diameter/2;    // s.x, s.y are center coords
			int rectY = s.y - s.diameter/2;    // fillOval needs rect coords
			
			// draw border (larger circle behind main circle)
			int dx = s.borderWidth / 2;        // shift over to center
			g.setColor(s.getBorderColor());
			g.fillOval(rectX - dx, rectY - dx, s.diameter, s.diameter);
			
			// draw main circle
			g.setColor(s.getCircleColor());
			if (s.getState().isAccept())           // make border wider for accept states
				g.fillOval(rectX + dx, rectY + dx, s.diameter - dx*4, s.diameter - dx*4);
			else
				g.fillOval(rectX, rectY, s.diameter - dx*2, s.diameter - dx*2);
			
			// draw state name
			g.setColor(s.getBorderColor());  
			g.drawString(s.getName(), s.x - (s.diameter/6), s.y + (s.diameter/10));
			// MM: to do: will need to find better way to position this ^
		}
		
		// draw all the arrows
		for (StateGraphic s : stateGraphics) {
			for (Transition t : s.getState().getTransitions()) {

				// get coordinates
				int x1 = s.getX();                              // start state
				int y1 = s.getY();
				StateGraphic target = t.getNext().getGraphic(); // target state
				int x2 = target.getX();
				int y2 = target.getY();
				
				// special case if pointing to self
				if (x1 == x2 && y1 == y2)
					continue;                // MM: TO DO

				// set up variables
				Graphics2D g2d = (Graphics2D) g;
				AffineTransform origTransform = g2d.getTransform(); // save
			    double px0, py0, px1, py1;                // pts on circles
			    float radius1 = s.getDiameter() / 2;
			    float radius2 = target.getDiameter() / 2;
			    
			    // calculate line angle in radians (0 = right; move clockwise)
			    double angle = Math.atan2(y2 - y1, x2 - x1);
			    
			    // calculate points of the line joining the states' centers
			    px0 = x1 + radius1 * Math.cos(angle);           // starting x
			    py0 = y1 + radius1 * Math.sin(angle);           // starting y
			    px1 = x2 + radius2 * Math.cos(angle + Math.PI); // target x
			    py1 = y2 + radius2 * Math.sin(angle + Math.PI); // target y
			    
			    // calculate arrow length
			    int arrowLength = (int) Math.sqrt((px1 - px0) * (px1 - px0) + 
			    		(py1 - py0) * (py1 - py0));
			    int arrowSize = 5;
			    
			    // translate drawing matrix to start arrow at origin
			    g2d.translate(px0, py0);  // origin is now start state's point
			    g2d.rotate(angle);        // rotate matrix by line angle
			    
			    // draw the arrow
			    int offset = 5;           // shift to account for double arrows
			    g2d.drawLine(0, offset, arrowLength, offset);        // shaft
			    g2d.drawLine(arrowLength, offset, 
			    		arrowLength - arrowSize, -arrowSize+offset); // head pt1
			    g2d.drawLine(arrowLength, offset, 
			    		arrowLength - arrowSize, arrowSize+offset);  // head pt2
			    
			    g2d.rotate(-angle);          // rotate back so input is upright
				g2d.setTransform(origTransform);        // reset transformation
			    
			    // calculate coordinates for input at 0 degree arrow (-->)
			    double bx, by, cx, cy;
			    offset = 4;                // looks better slightly smaller
			    bx = s.x + s.diameter/2;   // shift over by radius
			    by = s.y + 2*s.diameter/5; // shift over and down slightly
			    // (note that drawString takes x and y of bottom-left of input)
			    
			    // rotate the 0 deg. coords around circle at angle of arrow
			    cx = Math.cos(angle) * (bx - s.x) - 
			    		Math.sin(angle) * (by - s.y) + s.x - offset;
			    cy = Math.sin(angle) * (bx - s.x) + 
			    		Math.cos(angle) * (by - s.y) + s.y + offset;
			    
			    // draw the input				
				g.drawString(t.getInput(), (int)cx, (int)cy);
			}
		}
	}

	public StateGraphic withinStateBounds(Point point) {
		
		double x = point.getX();
		double y = point.getY();
		
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

	public void addAutomaton(Automaton auto) {
		machine = auto;
	}

	public void clearStates() {
		for (StateGraphic state : stateGraphics)
			state.deselect();
		currentState = null;
	}

	// CLASS: MouseListener //
	class MouseListener extends MouseAdapter {
		
		private StateGraphic selectedState = null;
		
		public void mouseDragged(MouseEvent me) {   // MM: to do: check if this registers when using a real mouse
			GraphicsTest.debug("Mouse dragged!");
			
			if (selectedState != null) {
				selectedState.setLocation(me.getX(), me.getY());
				repaint();
			}
		}

		public void mouseReleased(MouseEvent me) {
			GraphicsTest.debug("Mouse released!");
			StateGraphic s = withinStateBounds(me.getPoint());

			// if clicked on a state
			if (s != null) {
				
				// if left click, select
				if (SwingUtilities.isLeftMouseButton(me)) {
					
					// deselect previously selected state
					if (selectedState != null)
						selectedState.deselect();
					
					// mark new state as selected
					selectedState = s;
					selectedState.select(); // change state color to show selection
					repaint();
					
					GraphicsTest.debug("Selected state: " + s.getName());
				}
				
				// if right click, pull up menu
				else {
					final DropDown stateMenu = new DropDown(s, machine, mainWindow);
					stateMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			}

			
			// if didn't click on a state
			else {
			//if (withinStateBounds(me.getPoint()) == null) { 

				// if have a selected state, relocate it
				if (selectedState != null) {
					GraphicsTest.debug("Moving state " + selectedState.getName() + 
							" to " + me.getX() + "," + me.getY());

					// relocate selected state and deselect it
					selectedState.setLocation(me.getX(), me.getY());
					selectedState.deselect();
					selectedState = null;
				}

				// if no selected state, make new state 
				else {
					State newState = new State();
					machine.addState(newState);
					addState(newState, me.getX(), me.getY());
				}
				
				// refresh display
				repaint();
			}
		}
	}

	// CLASS: DropDown //
	class DropDown extends JPopupMenu {
		JMenuItem edit;
		JMenuItem delete;
		
		public DropDown(final StateGraphic s, final Automaton machine, final MainWindow win) {

			// edit state
			edit = new JMenuItem(new AbstractAction("Edit") {
				@Override
				public void actionPerformed(ActionEvent ae) {
					
					// open a new state editing window
					final StateWindow stateWindow = new StateWindow(s, machine, win);
				}
			});
			add(edit);
			
			// delete state
			delete = new JMenuItem(new AbstractAction("Delete") {
				@Override
				public void actionPerformed(ActionEvent ae) {
					
					// ask user to confirm deletion
					int response = JOptionPane.showConfirmDialog(null, 
							"Are you sure you want to delete state " + s.getName() + "?", 
							"Warning", JOptionPane.YES_NO_OPTION);
					
					// delete state if confirmed
					if (response == JOptionPane.YES_OPTION) {
						stateGraphics.remove(s);           // state graphic
						machine.removeState(s.getState()); // state
						win.update();
					}
				}
			});
			add(delete);
		}
	}
}
