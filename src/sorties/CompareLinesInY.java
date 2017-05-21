package sorties;

public class CompareLinesInY extends CompareLines {
	public CompareLinesInY(String out_file) {
		super(out_file);
		// TODO Auto-generated constructor stub
	}

	/*Upper extreme coordinate are scanned first*/
	@Override
	public int compareLines(String in1, String in2) {
		// TODO Auto-generated method stub
		double s1_y2 = Double.parseDouble(in1.split(",")[3]);
		double s2_y2 = Double.parseDouble(in2.split(",")[3]);
		double s1_y1 = Double.parseDouble(in1.split(",")[1]);
		double s2_y1 = Double.parseDouble(in2.split(",")[1]);
		if(s1_y2<s2_y2) return 1;
		else if (s1_y2>s2_y2) return -1;
		else{ 
			/*One of them is an horizontal segment */
			if((s1_y1==s1_y2 && s2_y1==s2_y2) || (s1_y1!=s1_y2 && s2_y1!=s2_y2) )return 0;
			else if((s1_y1==s1_y2 && s2_y1!=s2_y2)) return 2;
			else return -2;
			//return 2;
			/*Both are verticals*/
		}
	}

}
