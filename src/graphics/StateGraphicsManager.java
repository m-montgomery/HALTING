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

public class StateGraphicsManager extends JPanel {

    private static final long serialVersionUID = 5205922192600518812L;
	
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
	int x = 40;
	int y = 40;
	for (State s : states) {
	    addState(s, x, y);
			
	    // MM: to do: implement algorithm to distribute states within panel
	    //x += state.getDiameter()*2;
	    y += s.getGraphic().getDiameter()*2;
	    if (y > 40 + s.getGraphic().getDiameter()*2) {
		y = 40;
		x = 40 + s.getGraphic().getDiameter()*2;
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

	super.paintComponent(g);
	
	// draw each state circle
	for (StateGraphic s : stateGraphics)
	    drawState(s, g);
		
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

		// special case if pointing to self
		if (targetState.getID() == sourceState.getID())
		    continue;                // MM: TO DO
				
		// calculate line angle in radians (0 = right; move clockwise)
		double angle = Math.atan2(target.getY() - source.getY(),
					  target.getX() - source.getX());

		// draw the arrow
		drawArrow(source, target, angle, g);

		// draw the transition input
		drawInput(source, entry.getValue(), angle, g);
	    }
	}
    }

    private void drawState(StateGraphic s, Graphics g) {

	// update selected state if any
	if (machine.getCurrentState() == s.getState()) {

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
	if (s.getState().isAccept())       // wider border for accept states
	    g.fillOval(rectX + dx, rectY + dx,
		       s.diameter - dx*4, s.diameter - dx*4);
	// MM: TO DO: ^ make the border even wider for accept states, and
	//              also handle this by changing size of border circle,
	//              not inner circle like this currently does
	else
	    g.fillOval(rectX, rectY,
		       s.diameter - dx*2, s.diameter - dx*2);
			
	// draw state name
	g.setColor(s.getBorderColor());  
	g.drawString(s.getName(), s.x - (s.diameter/6), s.y + (s.diameter/10));
	// MM: to do: will need to find better way to position this ^
	//            based on length of state name
    }

    private void drawArrow(StateGraphic source, StateGraphic target,
			   double angle, Graphics g) {

	// set up variables
	Graphics2D g2d = (Graphics2D) g;
	AffineTransform origTransform = g2d.getTransform();     // save
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

	// clean up
	g2d.rotate(-angle);          // rotate back so input is upright
	g2d.setTransform(origTransform);        // reset transformation
    }

    private void drawInput(StateGraphic source, ArrayList<String> allInputs,
			   double angle, Graphics g) {
	
	// build input string (comma-delimited if multiple transitions)
	String fullInput = "";
	for (int i = 0; i < allInputs.size()-1; i++)
	    fullInput = fullInput.concat(allInputs.get(i)).concat(", ");
	fullInput = fullInput.concat(allInputs.get(allInputs.size()-1));

	// calculate coordinates for input at 0 degree arrow (-->)
	double bx, by, cx, cy;
	int offset = 4;
	bx = source.x + source.diameter/2;   // shift over by radius
	by = source.y + 2*source.diameter/5; // shift over and down slightly
	// (string is drawn using x,y coordinates of bottom-left corner)
			    
	// rotate the 0 degree coords around circle at angle of arrow
	cx = Math.cos(angle) * (bx - source.x) - 
	    Math.sin(angle) * (by - source.y) + source.x - offset;
	cy = Math.sin(angle) * (bx - source.x) + 
	    Math.cos(angle) * (by - source.y) + source.y + offset;
			    
	// adjust horizontal location for longer strings
	double deg = Math.toDegrees(angle);
	if ((deg >= 45 && deg <= 180) || (deg > -180 && deg < -135))
	    cx -= 5*(fullInput.length()-1);
	// MM: ^ this works when there's only 2 or 3 transitions;
	//       need to find a better solution for general case

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

	// MM: TO DO: change mouse listener's selected state refs to clicked
	public StateGraphic selectedState = null;
	private boolean dragged = false;

	public void clear() {
	    // deselect any selected state
	    if (selectedState != null)
		selectedState.unclick();

	    // clear variables
	    selectedState = null;
	    dragged = false;

	    // update display
	    repaint();
	}
	
	public void mousePressed(MouseEvent me) {
	    // reset mouse drag tracking
	    dragged = false;
	    
	    // if didn't click on a state, exit
	    StateGraphic s = withinStateBounds(me.getPoint());
	    if (s == null)
		return;
		
	    // deselect previously clicked state
	    if (selectedState != null) {

		// if re-clicked on previous state, deselect and exit
		if (selectedState.getState().getID() == s.getState().getID()) {
		    clear();
		    return;
		}
		// otherwise, deselect and continue
		selectedState.unclick();
	    }
		
	    // mark current state as clicked
	    selectedState = s;
	    selectedState.click(); // change state color to show selection
	    repaint();             // update display
	}
	
	public void mouseDragged(MouseEvent me) {

	    // if there's a selected state, move it to mouse position
	    if (selectedState != null) {
		dragged = true;                       // track mouse drag
		selectedState.setLocation(me.getX(), me.getY());  // move
		repaint();                              // update display
	    }
	}

	public void mouseReleased(MouseEvent me) {
	    
	    // get state clicked on (if any) 
	    StateGraphic s = withinStateBounds(me.getPoint());

	    // if mouse is over a state
	    if (s != null) {

		// if right-clicked on a state, show options menu
		if (SwingUtilities.isRightMouseButton(me)) {
		    final DropDown stateMenu = new DropDown(s, machine, mainWindow);
		    stateMenu.show(me.getComponent(), me.getX(), me.getY());
		    return;
		}

		else if (!dragged)
		    return;
	    }

	    // if mouse isn't over a state
	    else {

		// if have a selected state, relocate to mouse position
		if (selectedState != null)
		    selectedState.setLocation(me.getX(), me.getY());

		// otherwise, make a new state at mouse position
		else {
		    State newState = new State();
		    machine.addState(newState);
		    addState(newState, me.getX(), me.getY());
		}
	    }

	    // reset selections and update display
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
			final StateWindow stateWindow = new StateWindow(s, machine, win);
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
