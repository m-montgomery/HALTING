package automata;

public class NoStartStateDefined extends Exception {
	private String message;
	
	public NoStartStateDefined(String msg) {
		super(msg);
		message = msg;
	}
	public NoStartStateDefined() {
		super("No start state defined for this automaton.");
		message = "No start state defined for this automaton.";
	}
	public String getMessage() {
		return message;
	}
}
