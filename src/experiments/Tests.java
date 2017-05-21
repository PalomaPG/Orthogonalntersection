package experiments;

import dist_sweep.*;
import experiments.*;
import seggen.*;
import sorties.*;

public abstract class Tests {

	private int min_, max_;
	private int [] n_records;
	private int [] io_acc, n_comps;
	private double [] run_t;
	private String path_;
	private double alpha, mem_per;
	
	public Tests(int min_, int max_, String path_, double alpha, double mem_per){
		
		this.min_ = min_;
		this.max_ = max_;
		int pow_ = this.max_ - this.min_ + 1;
		this.n_records = new int [pow_];
		
		for (int i=this.min_; i<=this.max_; i++){
			this.n_records[i] = (int)Math.pow(2.0, i);
		}
		
		this.setIo_acc(new int[pow_]);
		this.n_comps = new int[pow_];
		this.setRun_t(new double[pow_]);
		this.alpha = alpha;
		this.path_ = path_;
		this.mem_per = mem_per;
	}
	
	public abstract void generateTests();
	public abstract void applyDS();

	public int [] getN_comps() {
		return n_comps;
	}

	public void setN_comps(int [] n_comps) {
		this.n_comps = n_comps;
	}

	public int [] getIo_acc() {
		return io_acc;
	}

	public void setIo_acc(int [] io_acc) {
		this.io_acc = io_acc;
	}

	public double [] getRun_t() {
		return run_t;
	}

	public void setRun_t(double [] run_t) {
		this.run_t = run_t;
	}
}
