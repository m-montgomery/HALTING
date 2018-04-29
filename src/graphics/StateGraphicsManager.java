package graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import automata.Automaton;
import automata.State;
import automata.Transition;

@SuppressWarnings("serial")
public class StateGraphicsManager extends JPanel {

	private ArrayList<StateGraphic> stateGraphics;
	private MainWindow mainWindow;
	private Automaton machine;
	private StateGraphic currentState;
	private MouseListener mouseListener;

	public StateGraphicsManager(MainWindow win) {
		stateGraphics = new ArrayList<StateGraphic>();
		mainWindow = win;
		machine = null;
		currentState = null;

		// set up mouse listening
		mouseListener = new MouseListener();
		addMouseListener(mouseListener);          // receive mouse events
		addMouseMotionListener(mouseListener);    // receive mouse motion events
	}

	public void createStates(ArrayList<State> states) {
		int x = 50;
		int y = 70;
		for (State s : states) {
			addState(s, x, y);

			y += s.getGraphic().getDiameter()*2;
			if (y > 70 + s.getGraphic().getDiameter()*2) {
				y = 70;
				x += s.getGraphic().getDiameter()*2;
			}
		}

	}

	public void addState(State state, int x, int y) {
		StateGraphic stateG = new StateGraphic(state);   // make new graphic
		stateG.setLocation(x, y);
		state.setGraphic(stateG);                        // save to state
		
		if (state.isStart())                             // select if start
			stateG.select();
		stateGraphics.add(stateG);                       // add to manager
	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		// draw each state circle
		for (StateGraphic state : stateGraphics)
			drawState(state, g);

		// for every state, draw all its transitions (arrow & input)
		for (StateGraphic source : stateGraphics) {
			State sourceState = source.getState();

			// draw one arrow for every state->state pair
			// (using condensed transitions grouped by target state)
			for (Map.Entry<State, ArrayList<String> > entry :
				condenseTransitions(source).entrySet()) {

				// get target state for current transition(s)
				State targetState = entry.getKey();
				StateGraphic target = targetState.getGraphic();

				// calculate line angle in radians (0 = right; move clockwise)
				double angle = Math.atan2(target.getY() - source.getY(),
						target.getX() - source.getX());
				
				// determine if state points to itself in this transition
				boolean circular = sourceState.getID() == targetState.getID();

				// draw the arrow and input
				drawArrow(source, target, entry.getValue(), angle, circular, g);
			}
		}
	}

	private void drawState(StateGraphic s, Graphics g) {

		// update current state
		State curr = machine.getCurrentState();
		if (curr != null && curr.getID() == s.getState().getID()) {

			// update current state status
			currentState = s; 
			if (machine.getStatus() == Automaton.ACCEPT)
				currentState.accept();
			else if (machine.getStatus() == Automaton.REJECT)
				currentState.reject();
			else
				currentState.select();
		}
		// ensure other states are deselected
		else
			s.deselect();

		// calculate bounding rectangle upperleft coords
		int rectX = s.x - s.diameter/2;    // s.x, s.y are center coords
		int rectY = s.y - s.diameter/2;    // fillOval needs rect coords

		// draw border (larger circle behind main circle)
		int dx = s.borderWidth / 2;        // shift over to center
		g.setColor(s.getBorderColor());
		g.fillOval(rectX - dx, rectY - dx, s.diameter, s.diameter);

		// draw main circle
		g.setColor(s.getCircleColor());
		if (s.getState().isAccept())       // wider border for accept states
			g.fillOval(rectX + dx*2, rectY + dx*2,
					s.diameter - dx*6, s.diameter - dx*6);
		else
			g.fillOval(rectX, rectY,
					s.diameter - dx*2, s.diameter - dx*2);

		// draw state name
		g.setColor(s.getBorderColor());
		int x = s.x - (s.diameter/6) - (3 * (s.getName().length()-2));
		int y = s.y + (s.diameter/10);
		g.drawString(s.getName(), x, y);
	}

	private void drawArrow(StateGraphic source, StateGraphic target,
			ArrayList<String> allInputs,
			double angle, boolean selfPointing, Graphics g) {

		// save current graphics coordinate system
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform origTransform = g2d.getTransform();

		// build input string (comma-delimited if multiple transitions)
		String fullInput = "";
		for (int i = 0; i < allInputs.size() - 1; i++)
			fullInput = fullInput.concat(allInputs.get(i)).concat(", ");
		fullInput = fullInput.concat(allInputs.get(allInputs.size() - 1));

		// special case if pointing to self
		if (selfPointing) {
			
			// calculate upper-lefthand corner of bounding rectangle
			int radius = (int) (source.getDiameter() / 2);
			int rectX = (int) (source.getX() - radius / 2);
			int rectY = (int) (source.getY() - radius * 1.9);  
			// ^ y delta slightly less than diameter so arc touches state
			
			// draw arc on top of state circle
			g.drawArc(rectX, rectY, radius, radius*2, 0, 180);
			
			// translate drawing matrix to start arrow head at arc left end pt
			g2d.translate(rectX, rectY + radius);
			g2d.rotate(-Math.PI / 2);              // rotate matrix by -90 deg
			
			// draw arrow head
			int arrowSize = 5;
			g2d.drawLine(0, 0, arrowSize, -arrowSize);
			g2d.drawLine(0, 0, arrowSize, arrowSize);

	        // reset coordinate system
			g2d.setTransform(origTransform);
			
			// draw input
			// draw input on top of circular arrow
			int x = source.getX() - 3 * fullInput.length();
			int y = source.getY() - source.getDiameter();
			g.drawString(fullInput, x, y);			
			return;
		}
				
		double px0, py0, px1, py1;                // points of the line
		float radius1 = source.getDiameter() / 2;
		float radius2 = target.getDiameter() / 2;

		// calculate points of the line joining the states' centers
		px0 = source.getX() + radius1 * Math.cos(angle);           // source x
		py0 = source.getY() + radius1 * Math.sin(angle);           // source y
		px1 = target.getX() + radius2 * Math.cos(angle + Math.PI); // target x
		py1 = target.getY() + radius2 * Math.sin(angle + Math.PI); // target y

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

        // reset coordinate system
		g2d.setTransform(origTransform);
		
		// calculate coordinates for input at 0 degree arrow (-->)
		double bx, by, cx, cy;
		bx = source.getX() + radius1 + arrowLength / 2;  // arrow center
		by = source.getY() + 15;                         // below arrow shaft
		// (string is drawn using x,y coordinates of bottom-left corner)

		// rotate the 0 degree coords around circle at angle of arrow
		cx = Math.cos(angle) * (bx - source.x) - 
				Math.sin(angle) * (by - source.y) + source.x - offset;
		cy = Math.sin(angle) * (bx - source.x) + 
				Math.cos(angle) * (by - source.y) + source.y + offset;

		// draw the input
		g.drawString(fullInput, (int)cx, (int)cy);
	}

	private HashMap<State, ArrayList<String> > condenseTransitions(StateGraphic s) {

		HashMap<State, ArrayList<String> > condensed =
				new HashMap<State, ArrayList<String> >();

		// for every transition starting from current state
		for (Transition t : s.getState().getTransitions()) {
			State targetState = t.getNext();

			// initialize list of transitions per target state
			if (! condensed.containsKey(targetState))
				condensed.put(targetState, new ArrayList<String>());

			// add current transition input to target state's listing
			condensed.get(targetState).add(t.getInput());
		}

		return condensed;
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
			state.resetStatus();
		currentState = null;
		mouseListener.clear();
	}

	// CLASS: MouseListener //
	class MouseListener extends MouseAdapter {

		public StateGraphic clickedState = null; // save clicked-on state
		private boolean dragged = false;         // is mouse dragging?

		public void clear() {

			// unclick any clicked state
			if (clickedState != null)
				clickedState.unclick();

			// clear variables
			clickedState = null;
			dragged = false;

			// update display
			repaint();
		}

		public void mousePressed(MouseEvent me) {
			
			// reset mouse drag tracking
			dragged = false;

			// if didn't click on a state, exit
			StateGraphic state = withinStateBounds(me.getPoint());
			if (state == null)
				return;

			// check for previously clicked state
			if (clickedState != null) {

				// if re-clicked on previous state, deselect and exit
				if (clickedState.getState().getID() == state.getState().getID()) {
					clear();
					return;
				}
				// otherwise, deselect and continue
				clickedState.unclick();
			}

			// mark current state as clicked
			clickedState = state;
			clickedState.click(); // changes state color to show selection
			
			// update display
			repaint(); 
		}

		public void mouseDragged(MouseEvent me) {

			// if there's a clicked state
			if (clickedState != null) {
				
				// track mouse dragging & move to mouse position
				dragged = true;
				clickedState.setLocation(me.getX(), me.getY());
				
				// update display
				repaint();
			}
		}

		public void mouseReleased(MouseEvent me) {

			// get state clicked on (if any) 
			StateGraphic state = withinStateBounds(me.getPoint());

			// if mouse is over a state
			if (state != null) {

				// if right-clicked on a state, show options menu
				if (SwingUtilities.isRightMouseButton(me) || me.isControlDown()) {
					
					if (machine.getStatus().equals(Automaton.READY)) {
						final DropDown stateMenu = new DropDown(
								state, machine, mainWindow);
						stateMenu.show(me.getComponent(), me.getX(), me.getY());
					}
					else
						JOptionPane.showMessageDialog(null,
							"Cannot edit or delete states while the machine is running.", 
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				else if (!dragged)  // unless just finished dragging,
					return;         // don't clear clicked state 
			}

			// if mouse isn't over a state
			else {

				// if have a clicked state, relocate to mouse position
				if (clickedState != null)
					clickedState.setLocation(me.getX(), me.getY());
				
				// otherwise, make a new state at mouse position
				else {
					State newState = new State();
					machine.addState(newState);
					addState(newState, me.getX(), me.getY());
				}
			}

			// reset clicked state and update display
			clear();
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
					new StateWindow(s, machine, win);
					mouseListener.clear();
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
						stateGraphics.remove(s);            // state graphic
						machine.removeState(s.getState());  // state
						win.update();
					}
					mouseListener.clear();          // clear state selection
				}
			});
			add(delete);
		}
	}
}
