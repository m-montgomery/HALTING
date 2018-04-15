package automata;

@SuppressWarnings("serial")
public class NoTransitionDefined extends Exception {
	private String message;
	
	public NoTransitionDefined(String msg) {
		super(msg);
		message = msg;
	}
	public NoTransitionDefined(String name, String input) {
		super("No transition defined from state " + name +
				" on input " + input + ".");
		message = "No transition defined from state " + name +
				" on input " + input + ".";
	}
	public String getMessage() {
		return message;
	}
}
