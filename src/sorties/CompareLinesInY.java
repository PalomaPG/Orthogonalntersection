package sorties;

public class CompareLinesInY implements CompareLines {
	/*Upper extreme coordinate are scanned first*/
	@Override
	public int compareLines(String in1, String in2) {
		// TODO Auto-generated method stub
		double s1 = Double.parseDouble(in1.split(",")[3]);
		double s2 = Double.parseDouble(in2.split(",")[3]);
		
		if(s1<s2) return 1;
		else if (s1>s2) return -1;
		else return 0;
	}

}
