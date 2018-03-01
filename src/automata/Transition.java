package automata;

public class Transition {
	private String input;
	private State next;
	
	private int ID;               // unique ID assigned by static transitionNumber
	static int transitionNumber = 0;
	
	public Transition(String i, State n) {
		input = i;
		next = n;
		ID = transitionNumber++;
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
