package sorties;

public abstract class CompareLines {

	private String out_file;
	public CompareLines(String out_file){
		this.setOut_file(out_file);
	}
	
	public abstract int compareLines(String in1, String in2);

	public String getOut_file() {
		return out_file;
	}

	public void setOut_file(String out_file) {
		this.out_file = out_file;
	}
}
