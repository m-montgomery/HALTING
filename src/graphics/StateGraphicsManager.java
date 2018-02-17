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
		}

	}
	
	private void addState(State s, int x, int y) {
		//GraphicsTest.debug("Just made graphic for state " + s.getName() + " at " + x + "," + y);
		StateGraphic state = new StateGraphic(s);
		state.setLocation(x, y);
		s.setGraphic(state);
		stateGraphics.add(state);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		GraphicsTest.debug("In StateGraphicsManager paintComponent()");
		super.paintComponent(g);
		int arrowSize = 5;
		
		// draw all the arrows
//		for (StateGraphic s : stateGraphics) {
//			for (Transition t : s.getState().getTransitions()) {
//
//				// get coordinates
//				int x1 = s.getX();                        // start state
//				int y1 = s.getY();
//				int x2 = t.getNext().getGraphic().getX(); // target state
//				int y2 = t.getNext().getGraphic().getY();
//				GraphicsTest.debug("Arrow from: " + x1 + "," + y1 + " to: " + x2 + "," + y2);
//				
//				// special case if pointing to self
//				if (x1 == x2 && y1 == y2)
//					continue;                // MM: TO DO
//
//				// draw arrow
//				// following from SO answer to start
//				// https://stackoverflow.com/questions/4112701/drawing-a-line-with-arrow-in-java
//				Graphics2D g1 = (Graphics2D) g.create();
//				int ARR_SIZE = 7;
//				double dx = x2 - x1, dy = y2 - y1;
//				double angle = Math.atan2(dy, dx);
//				int len = (int) Math.sqrt(dx*dx + dy*dy);
//				AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
//				at.concatenate(AffineTransform.getRotateInstance(angle));
//				g1.transform(at);
//
//				// Draw horizontal arrow starting in (0, 0)
//				g1.drawLine(0, 0, len, 0);
//				g1.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
//						new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
//			}
//		}
		// draw each state
		for (StateGraphic s : stateGraphics) {
			//GraphicsTest.debug("Painting state " + s.getName());
			
			// update selected state
			if (machine.getCurrentState() == s.getState() && 
					machine.getStatus() != Automaton.READY) {    // if running
				
				if (currentState != null) {                // deselect previous
					currentState.deselect();
					//GraphicsTest.debug("Deselecting " + currentState.getName());
				}
				currentState = s; 
				
				if (machine.getStatus() == Automaton.ACCEPT)
					currentState.accept();
				else if (machine.getStatus() == Automaton.REJECT)
					currentState.reject();
				else
					currentState.select();
				//GraphicsTest.debug("Selecting " + currentState.getName());
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
		GraphicsTest.debug("");  // newline
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
					DropDown stateMenu = new DropDown(s, machine, mainWindow);
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
		
		public DropDown(StateGraphic s, Automaton machine, MainWindow win) {

			// edit state
			edit = new JMenuItem(new AbstractAction("Edit") {
				@Override
				public void actionPerformed(ActionEvent ae) {
					
					// open a new state editing window
					StateWindow stateWindow = new StateWindow(s, machine, win);
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
