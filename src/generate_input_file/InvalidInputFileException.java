package generate_input_file;

public class InvalidInputFileException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String line;
	private int lineNumber;
	
	public InvalidInputFileException(String line, int lineNumber){
		this.setLine(line);
		this.setLineNumber(lineNumber);
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	

}
