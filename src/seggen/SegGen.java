package seggen;

import java.util.Random;

public class SegGen {
	
	protected double init, end, range_length;
	protected Random rdmgen;
	
	public SegGen(double init, double end){
		
		this.init = init;
		this.end = end;
		this.range_length = end-init;
		this.rdmgen = new Random();
	}
	
	public double normccoord(double mean, double stdev){
		return mean + rdmgen.nextGaussian() * stdev;
	}
	
	
	public double uniccoord(){
		return this.rdmgen.nextDouble()*this.range_length + init;
		
	}
	
	public int orientation(){
		return rdmgen.nextDouble() <0.5? 0 : 1; //horizontal: 0, vertical: 1
	}
}
