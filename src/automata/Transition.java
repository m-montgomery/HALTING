package automata;

public class Transition {
	String input;
	State next;
	
	public Transition(String i, State n) {
		input = i;
		next = n;
	}
	
	public String toString() {
		return "'" + input + "'" + " -> " + next.getPrintName();
	}
	
	public String getInput() {
		return input;
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
