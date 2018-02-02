package automata;

import java.util.ArrayList;

public class Automaton {
	ArrayList<State> states;
	ArrayList<State> history;
	State startState;
	State currentState;
	
	String name;
	ArrayList<Character> input;
	int inputCount;
	int stepCount;                // separate from input for future epsilon moves
	static int automataCount = 0;
	
	public static void debug(String s) {  // DEBUG
		System.out.println(s);
	}
	
	public Automaton() {
		states = new ArrayList<State>();
		history = new ArrayList<State>();
		startState = null;
		currentState = null;
		input = new ArrayList<Character>();
		inputCount = 0;
		stepCount = 0;
		name = "Automaton " + Integer.toString(automataCount++);
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
		
		// show input
		s += "INPUT: ";
		for (Character c : input)
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
	
	public void addState(State s) {
		states.add(s);
	}
	
	public void addState(State s, boolean start) {
		states.add(s);
		if (start) {
			startState = s;
			currentState = startState;
			history.add(currentState);
		}
	}
	
	public void setInput(String inputString) {
		for (int i = 0; i < inputString.length(); i++)
			input.add(inputString.charAt(i));
	}
	
	public boolean run(String inputString) {
		
		if (startState == null) {
			// throw NoStartStateDefined exception
			debug("No start state defined.");
			return false;
		}
		
		// build input list
		int totalInput = inputString.length();
		setInput(inputString);
		
		// step through states based on input
		while (inputCount < totalInput)
			step();
		
		return accepted();
	}
	
	void step() {
		// don't continue past input
		if (inputCount >= input.size())   // MM: but what about epsilon moves after input?
			return;
		
		stepCount++;
		// find next state given current state & input
		char currentInput = input.get(inputCount++);
		//debug("curr: " + currentInput);  // DEBUG
		State nextState = currentState.getNextState(currentInput);
		if (nextState == null) {
			//throw noTransitionDefined exception
			debug("No transition defined.");
			return;
		}
		
		// transition
		history.add(nextState);  // add next state to history
		currentState = nextState;
		//debug("Adding state " + currentState.getPrintName() + " to history");
	}
	
	void stepBack() {
		// don't step back if at beginning
		if (inputCount == 0)
			return;
		
		inputCount--;
		stepCount--;
		history.remove(history.size() - 1);             // remove current state
		currentState = history.get(history.size() - 1); // return to previous state
		// indexing history with own size to allow for future epsilon moves
	}
	
	public void reset() {
		inputCount = 0;
		stepCount = 0;
		history.clear();
		history.add(startState);   // add start state to history
		currentState = startState;
	}
	
	public boolean accepted() {
		return currentState.isAccept();
	}
}
