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
		s0.addTransition('a', s0);
		s0.addTransition('b', s1);
		s1.addTransition('a', s0);
		Automaton f = new Automaton();
		f.addState(s0, true);          // start state
		f.addState(s1);
		
		// launch window
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
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
