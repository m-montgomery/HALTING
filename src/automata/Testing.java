package automata;

public class Testing {

	public static void main(String[] args) {
		Automaton f = new Automaton();
		
		State s1 = new State();
		State s2 = new State(true);  // accept state
		s1.addTransition('a', s2);
		s2.addTransition('b', s1);
		
		f.addState(s1, true);  // start state
		f.addState(s2);
		if (f.run("aba"))
			System.out.println("Accepted!");
		else
			System.out.println("Rejected.");
		
		f.reset();
		f.step();
		f.step();
		f.step();
		if (f.accepted())
			System.out.println("Accepted!");
	}
}
