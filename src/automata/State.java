package automata;

import java.util.ArrayList;

public class State {
	ArrayList<Transition> transitions;
	boolean isAccept;
	String name;
	static int stateCount = 0;
	
	public State() {
		transitions = new ArrayList<Transition>();
		isAccept = false;
		name = "q" + Integer.toString(stateCount++);
	}
	
	public State(boolean accept) {
		transitions = new ArrayList<Transition>();
		isAccept = accept;
		name = "q" + Integer.toString(stateCount++);
	}
	
	public String getName() {
		return name;
	}
	
	public String getPrintName() {
		String s = "[" + name + "]";      // [state]
		if (isAccept())
			s += "*";                     // [state]*  for accept states
		return s;
	}
	
	public void setName(String newName) {
		name = newName;
	}
	
	public String toString() {
		String s = getPrintName();
		for (Transition t : transitions)
			s += "\n   " + t.toString();  //    input -> [state]
		return s;
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

