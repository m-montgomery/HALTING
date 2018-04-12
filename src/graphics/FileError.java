package graphics;

public class FileError extends Exception {
	private String message;
	
	public FileError(String msg) {
		super(msg);
		message = msg;
	}
	public FileError(String msg, boolean saving) {
		super("Unable to save automaton to file:\n" + msg);
		message = "Unable to save automaton to file:\n" + msg;
	}
	public FileError(String name, String msg) {
		super("Unable to load automaton from file " + name + "\n" + msg);
		message = "Unable to load automaton from file " + name + "\n" + msg;
	}
	public FileError() {
		super("Unable to load automaton from file.");
		message = "Unable to load automaton from file";
	}
	public String getMessage() {
		return message;
	}
}
