package automata;

public class Transition {
	private String input;
	private State next;
	private int nextID;
	
	private int ID;               // unique ID assigned by static transitionNumber
	static int transitionNumber = 0;
	
	public Transition(String i, State n) {
		input = i;
		next = n;
		nextID = next.getID();
		ID = transitionNumber++;
	}
	
	public Transition(String i, int n, int id) {
		input = i;
		next = null;
		nextID = n;
		ID = id;
	}
	
	public String toString() {
		return "'" + input + "'" + " -> " + next.getPrintName();
	}
	
	public String getInput() {
		return input;
	}
	
	public int getID() {
		return ID;
	}
	
	public State getNext() {
		return next;
	}
	
	public int getNextID() {
		return nextID;
	}

	public Transition getCopy() {
		return new Transition(input, next);
	}

	public void setNext(State state) {
		next = state;
	}

	public void setInput(String text) {
		input = text;
	}
}
