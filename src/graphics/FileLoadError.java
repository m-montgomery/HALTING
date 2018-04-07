package graphics;

public class FileLoadError extends Exception {
	private String message;
	
	public FileLoadError(String msg) {
		super("Unable to load automaton from file:\n" + msg);
		message = "Unable to load automaton from file:\n" + msg;
	}
	public FileLoadError() {
		super("Unable to load automaton from file.");
		message = "Unable to load automaton from file";
	}
	public String getMessage() {
		return message;
	}
}
