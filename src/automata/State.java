package automata;

import java.util.ArrayList;

public class State {
	ArrayList<Transition> transitions;
	boolean isAccept;
	
	public State() {
		transitions = new ArrayList<Transition>();
		isAccept = false;
	}
	
	public State(boolean accept) {
		transitions = new ArrayList<Transition>();
		isAccept = accept;
	}
	
	public boolean isAccept() {
		return isAccept;
	}
	
	public void addTransition(Transition t) {
		transitions.add(t);
	}
	
	public void addTransition(Character i, State n) {
		transitions.add(new Transition(i, n));
	}
	
	public State getNextState(Character input) {
		for (Transition t : transitions) { // MM: better to use hash map instead of ArrayList?
			if (t.getInput() == input)     //     or would that not work w NFAs..?
				return t.getNext();
		}
		return null;
	}
}

