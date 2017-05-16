package sorties;

public class CompareLinesInX implements CompareLines{

	@Override
	public int compareLines(String in1, String in2) {
		// TODO Auto-generated method stub
		double s1 = Double.parseDouble(in1.split(",")[0]);
		double s2 = Double.parseDouble(in2.split(",")[0]);
		
		if(s1>s2) return 1;
		else if (s1<s2) return -1;
		else return 0;
	}
}
