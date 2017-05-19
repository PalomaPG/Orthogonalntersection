package dist_sweep;

import sorties.*;

public class DistributionSweep {
	
	private String input, output, x_sort, y_sort;
	private int n_inter, pow_, bs, l_bytes;
	private double mem_portion;
	
	
	public DistributionSweep(String input, String output, int pow_, int bs, double mem_portion){
		
		this.input = input;
		this.output = output;
		this.n_inter = 0;
		this.pow_ = pow_;
		this.bs = bs;
		l_bytes = 43;
		this.mem_portion = mem_portion;
	}

	public void mainDS(double min, double max){
		/*range [min, max)*/
		//(int)Math.pow(2, 21), 4096, 43, 0.001, "../../test_sup.bin", "../../result.bin"
		sortInXandY();
		recursiveDS(this.x_sort, this.y_sort);
	}
	
	private void sortInXandY(){
		MergeSort ms= new MergeSort((int)Math.pow(2, this.pow_), this.bs, l_bytes, mem_portion, this.input);
		this.x_sort="x_sort_"+this.input;
		CompareLines cl = new CompareLinesInX(this.x_sort);
		ms.sortPhase(cl);
		ms.mergePhase(ms.getOFiles(), cl,0);
		ms.deleteFiles();
		this.y_sort="y_sort_"+this.input;
		cl = new CompareLinesInY(this.y_sort);
		ms.sortPhase(cl);
		ms.mergePhase(ms.getOFiles(), cl,0);
		ms.deleteFiles();
		System.err.println("DONE");
	}
	public void recursiveDS(String in_x, String in_y){
		
	}
}
