package automata;

import java.util.ArrayList;

public class Automaton {
	ArrayList<State> states;
	ArrayList<State> history;
	State startState;
	State currentState;
	
	ArrayList<Character> input;
	int inputCount;
	
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
	}
	
	public void addState(State s) {
		states.add(s);
	}
	
	public void addState(State s, boolean start) {
		states.add(s);
		if (start) {
			startState = s;
			currentState = startState;
		}
	}
	
	public boolean run(String inputString) {
		
		if (startState == null) {
			// throw NoStartStateDefined exception
			debug("No start state defined.");
			return false;
		}
		
		// build input list
		int totalInput = inputString.length();
		for (int i = 0; i < totalInput; i++)
			input.add(inputString.charAt(i));
		
		// step through states based on input
		while (inputCount < totalInput)
			step();
		
		return accepted();
	}
	
	void step() {
		// don't continue past input
		if (inputCount >= input.size())   // MM: but what about epsilon moves after input?
			return;
		
		// find next state given current state & input
		char currentInput = input.get(inputCount++);
		debug("curr: " + currentInput);  // DEBUG
		State nextState = currentState.getNextState(currentInput);
		if (nextState == null) {
			//throw noTransitionDefined exception
			debug("No transition defined.");
			return;
		}
		
		// transition
		currentState = nextState;
		history.add(currentState);
	}
	
	void stepBack() {
		// don't step back if at beginning
		if (inputCount == 0)
			return;
		
		inputCount--;
		history.remove(history.size() - 1);             // remove current state
		currentState = history.get(history.size() - 1); // return to previous state
		// indexing history with own size to allow for future epsilon moves
	}
	
	public void reset() {
		inputCount = 0;
		currentState = startState;
		history = new ArrayList<State>();
	}
	
	public boolean accepted() {
		return currentState.isAccept();
	}
}
