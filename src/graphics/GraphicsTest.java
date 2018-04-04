package graphics;

import java.awt.EventQueue;

import automata.Automaton;
import automata.State;

public class GraphicsTest {
	
	public static void debug(String msg) {  // to handle all debug stmts
		System.out.println(msg);            // (comment this out to stop output)
	}

	public static void main(String[] args) {
		
		// init test FA
		State s0 = new State();
		State s1 = new State(true);    // accept state
		State s2 = new State();

		//s0.addTransition("b", s1);
		s0.addTransition("e", s2);
//		s0.addTransition("a", s0);
//		s0.addTransition("f", s2);
//		s0.addTransition("g", s2);

		s1.addTransition("d", s0);
		
		s1.addTransition("c", s0);
		
		s2.addTransition("h", s0);
		
		final Automaton f = new Automaton();
		f.addState(s0, true);          // start state
		f.addState(s1);
		f.addState(s2);
		
		// launch window
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					final MainWindow frame = new MainWindow();
					frame.addAutomaton(f);
					frame.update();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});		
	}
}
