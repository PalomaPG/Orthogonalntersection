package dist_sweep;

import java.io.RandomAccessFile;
import java.util.LinkedList;

import sorties.*;

public class DistributionSweep {
	
	private String input, output, x_sort, y_sort;
	private int n_inter, pow_, bs, l_bytes;
	private double mem_portion;
	private MergeSort ms;
	
	
	public DistributionSweep(String input, String output, int pow_, int bs, double mem_portion){
		
		this.input = input;
		this.output = output;
		this.n_inter = 0;
		this.pow_ = pow_;
		this.bs = bs;
		l_bytes = 43;
		this.mem_portion = mem_portion;
		this.ms =new MergeSort((int)Math.pow(2, this.pow_), this.bs, l_bytes, mem_portion, this.input);
	}

	public void mainDS(double min, double max){
		/*range [min, max)*/
		//(int)Math.pow(2, 21), 4096, 43, 0.001, "../../test_sup.bin", "../../result.bin"
		sortInXandY();
		recursiveDS(this.x_sort, this.y_sort);
	}
	
	private void sortInXandY(){
		this.x_sort="x_sort_"+this.input;
		CompareLines cl = new CompareLinesInX(this.x_sort);
		this.ms.sortPhase(cl);
		this.ms.mergePhase(ms.getOFiles(), cl,0);
		this.ms.deleteFiles();
		this.y_sort="y_sort_"+this.input;
		cl = new CompareLinesInY(this.y_sort);
		this.ms.sortPhase(cl);
		this.ms.mergePhase(ms.getOFiles(), cl,0);
		this.ms.deleteFiles();
		System.err.println("DONE");
	}
	public void recursiveDS(String file_x, String file_y){
		
		
	}
	
	private void subSlabs(RandomAccessFile raf_in, double delta){
		
	}
	
	private LinkedList<String>[] add2List(LinkedList<String>[] ll_slabs, String s, double x, RandomAccessFile raf){
		
		
		return ll_slabs;
	}
	
	private LinkedList<String>[] reportIntersections(LinkedList<String>[] ll_slabs, double x1, double x2){
		return ll_slabs;
	}
	
}
