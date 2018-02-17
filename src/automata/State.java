package automata;

import java.util.ArrayList;
import java.util.Iterator;

import graphics.StateGraphic;

public class State {
	ArrayList<Transition> transitions;
	boolean isAccept;
	boolean isStart;
	String name;
	StateGraphic graphic = null;
	
	private int ID;             // unique ID assigned by static stateNumber
	static int stateNumber = 0;
	
	public State() {
		isAccept = false;
		init();
	}
	
	public State(boolean accept) {
		isAccept = accept;
		init();
	}
	
	void init() {
		transitions = new ArrayList<Transition>();
		name = "q";
		isStart = false;
		ID = stateNumber++;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return ID;
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

	
	public void addTransition(Transition t) {
		transitions.add(t);
	}
	
	public void addTransition(String i, State n) {
		transitions.add(new Transition(i, n));
	}
	
	public State getNextState(String input) {
		// find target state of transition with same input
		for (Transition t : transitions) {
			if (t.getInput().equals(input))
				return t.getNext();
		}
		return null;
	}

	public ArrayList<Transition> getTransitions() {
		return transitions;
	}

	public void setStart(boolean s) {
		isStart = s;
	}
	
	public boolean isStart() {
		return isStart;
	}

	public void setAccept(boolean a) {
		isAccept = a;
	}
	
	public boolean isAccept() {
		return isAccept;
	}

	public ArrayList<Transition> getTransitionsCopy() {
		ArrayList<Transition> copy = new ArrayList<Transition>();
		for (Transition t : transitions)
			copy.add(t.getCopy());
		return copy;
	}

	// used to edit transitions in runtime
	public void setTransitions(ArrayList<Transition> newTransitions) {
		transitions = newTransitions; 
	}
	
	public void setGraphic(StateGraphic s) {
		graphic = s;
	}
	
	public StateGraphic getGraphic() {
		return graphic;
	}

	public void removeTransitionsTo(State state) {
		for (Iterator<Transition> it = transitions.iterator(); it.hasNext();) {
			Transition t = it.next();
			if (t.getNext().getID() == state.getID())
				it.remove();
		}
	}
}
