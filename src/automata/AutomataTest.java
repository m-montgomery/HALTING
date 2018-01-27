package automata;

public class AutomataTest {

	public static void main(String[] args) {
		
		// make states
		State s0 = new State();
		State s1 = new State(true);    // accept state
		s0.addTransition('a', s0);
		s0.addTransition('b', s1);
		s1.addTransition('a', s0);
		//System.out.println(s0);
		//System.out.println(s1);
		
		// make FA
		Automaton f = new Automaton();
		f.addState(s0, true);          // start state
		s0.setName("Start");
		f.addState(s1);
		//System.out.println("\n" + f);
		
		// run FA
		//System.out.println(f.run("aba") ? "Accepted!" : "Rejected.");
		//f.reset();
		
		// step through FA
		f.setInput("aba");
		System.out.println("\n" + f.snapshot());
		f.step();
		System.out.println("\n" + f.snapshot());
		f.step();
		System.out.println("\n" + f.snapshot());
		f.step();
		System.out.println("\n" + f.snapshot());
		System.out.println(f.accepted() ? "Accepted!" : "Rejected.");
	}
}
