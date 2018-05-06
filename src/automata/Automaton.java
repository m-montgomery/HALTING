package automata;

import java.util.ArrayList;
import java.util.Iterator;

public class Automaton {
	ArrayList<State> states;
	ArrayList<State> history;
	State startState;
	State currentState;
	
	String name;
	String type;
	String status;
	
	ArrayList<String> input;
	String inputString = "";
	int inputCount;
	int stepCount;
	int stateCount;
	
	static int automataCount = 0;
	
	public static final String READY = "Ready";
	public static final String ACCEPT = "Accepted";
	public static final String ERROR = "Error";
	public static final String REJECT = "Rejected";
	public static final String RUN = "Running";
	
	public Automaton() {
		name = "Automaton " + Integer.toString(automataCount++);
		type = "DFA";
		states = new ArrayList<State>();
		init();
	}
	
	public Automaton(String n, String t, ArrayList<State> s) {
		name = n;
		type = t;
		states = s;
		
		// update static counts for unique ID assignment
		State.stateNumber += states.size();
		for (State myState : states)
			Transition.transitionNumber += myState.getTransitions().size();
		
		init();
	}
	
	private void init() {
		history = new ArrayList<State>();
		startState = currentState = null;
		input = new ArrayList<String>();
		inputCount = stepCount = 0;
		stateCount = states.size();
		status = READY;
	}

	public String toString() {
		String s = name;
		for (State st : states)
			s += "\n " + st.toString();
		return s;
	}
	
	
	String snapshot() {
		// show name 
		String s = name + " - Steps: " + Integer.toString(stepCount) + "\n";
		int titleLength = s.length() - 1;
		for (int i = 0; i < titleLength; i++)
			s += "-";
		s += "\n";
		
		// show history
		if (currentState != null) {
			if (history.size() > 1) {
				int count = 0;
				for (int i = 0; i < stepCount; i++) {
					State st = history.get(i);
					s += st.getPrintName();
					s += " --" + input.get(count++) + "-> ";
				}
				s += currentState.getPrintName() + "\n\n";
			}
			
			s += "STATE: " + currentState.getPrintName() + "\n";
		}
		
		// show input
		s += "INPUT: ";
		for (String c : input)
			s += c;
		s += "\n";
		for (int i = 0; i < inputCount + 7; i++)  // 6 for "INPUT: "
			s += " ";
		s += "^";
		
		return s;
	}
	
	public ArrayList<State> getStates() {
		return states;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String newName) {
		name = newName;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String newType) {
		type = newType;
	}

	public String getStatus() {
		return status;
	}
	
	public void addState(State s) {
		states.add(s);
		s.setName(s.getName() + Integer.toString(stateCount++));
	}
	
	public void addState(State s, boolean start) {
		addState(s);
		if (start) {
			startState = s;
			currentState = startState;
			history.add(currentState);
			s.setStart(true);
		}
	}
	
	public void setInput(String newInput) {
		input.clear();
		inputString = newInput;
		for (int i = 0; i < newInput.length(); i++)
			input.add(newInput.substring(i, i+1));
	}
	
	public boolean run(String inputString) throws NoStartStateDefined, NoTransitionDefined {
		setInput(inputString);
		return run();
	}
	
	public boolean run() throws NoStartStateDefined, NoTransitionDefined {
		
		// assert there is a start state
		if (startState == null) {
			status = ERROR;
			throw new NoStartStateDefined();
		}

		reset();
		status = RUN;
		
		// check for empty string
		if (reachedEndOfInput())
			status = accepted() ? ACCEPT : REJECT;

		// build input list
		int totalInput = input.size();
		
		// step through states based on input
		while (inputCount < totalInput)
			step();
		
		return accepted();
	}
	
	public void step() throws NoTransitionDefined, NoStartStateDefined {
		
		// don't continue past input
		if (inputCount >= input.size())
			return;
		
		// assert there is a start state
		if (startState == null) {
			status = ERROR;
			throw new NoStartStateDefined();
		}
		// if first step, set current state to start
		else if (currentState == null) {
			currentState = startState;
			history.add(currentState);
		}
		
		status = RUN;
		stepCount++;
		
		// find next state given current state & input
		String currentInput = input.get(inputCount++);
		State nextState = currentState.getNextState(currentInput);
		
		// report transition error
		if (nextState == null) {
			status = ERROR;
			throw new NoTransitionDefined(currentState.getName(), currentInput);
		}
		
		// transition
		history.add(nextState);  // add next state to history
		currentState = nextState;
		if (reachedEndOfInput())
			status = accepted() ? ACCEPT : REJECT;
	}
	
	public void stepBack() {
		// don't step back if at beginning
		if (atStart())
			return;
		
		status = RUN;
		
		inputCount--;
		stepCount--;
		history.remove(history.size() - 1);             // remove current state
		currentState = history.get(history.size() - 1); // return to previous state
		// indexing history with own size to allow for future epsilon moves
	}
	
	public void reset() {
		// reset runtime variables, keep original input
		inputCount = 0;
		stepCount = 0;
		history.clear();
		history.add(startState);   // add start state to history
		currentState = startState;		
		status = READY;
	}
	
	boolean reachedEndOfInput() {
		return inputCount == input.size();
	}
	
	boolean accepted() {
		return currentState.isAccept();
	}
	
	public State getCurrentState() {
		return currentState;
	}

	public void setStart(State state) {
		
		removeStart();
		
		// set new start state 
		for (State s : states) {
			if (s.getID() == state.getID()) {
				s.setStart(true);
				startState = s;
			}
		}
		reset();
	}
	
	public void removeState(State state) {
		
		// clear start state if needed
		if (state.getID() == startState.getID())
			startState = null;
		
		// check every state
		for (Iterator<State> it = states.iterator(); it.hasNext(); ) {
			State s = it.next();
			
			// remove references to the state (transitions)
			s.removeTransitionsTo(state);
			
			// remove the state itself
			if (s.getID() == state.getID())
				it.remove();
		}
	}
	
	public void removeStart() {
		// unset start status of current start state
		if (startState != null)
			startState.setStart(false);
		startState = null;
		reset();
	}
	
	public State getStartState() {
		return startState;
	}

	public boolean hasStateNamed(String name) {
		return getStateNamed(name) != null;
	}

	public State getStateNamed(String name) {
		for (State s : states) {
			if (s.getName().equals(name))
				return s;
		}
		return null;
	}

	public State getStateWithID(int ID) {
		for (State s : states) {
			if (s.getID() == ID)
				return s;
		}
		return null;
	}

	public String getInput() {
		return inputString;
	}
	
	public String getCurrentInput() {
		if (inputCount > 0)
			return input.get(inputCount-1);
		return input.get(0);
	}

	public boolean atStart() {
		return inputCount == 0;
	}

	public int getInputNum() {
		return inputCount;
	}
}
