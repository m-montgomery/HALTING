package automata;

public class Transition {    // MM: using class instead of simple pair struct
	Character input;         //     for future dev w pushdown automata
	State next;
	
	public Transition(Character i, State n) {
		input = i;
		next = n;
	}
	
	public String toString() {
		return "'" + input + "'" + " -> " + next.getPrintName();
	}
	
	public Character getInput() {
		return input;
	}
	
	public State getNext() {
		return next;
	}
}
