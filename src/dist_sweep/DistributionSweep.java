package dist_sweep;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.ListIterator;

import sorties.*;

public class DistributionSweep {
	
	private String input, output, x_sort, y_sort;
	private int bs, l_bytes;
	private int n_rec_per_block;
	private int io_acc, n_comp;
	private int n_inter;
	private double run_t;
	private double mem_portion;
	public MergeSort ms;
	private LinkedList<String> reports;
	private RandomAccessFile raf_output;
	
	
	public DistributionSweep(String input, String output,int bs, double mem_portion, MergeSort ms, int n_rec_per_block){
		
		this.input = input;
		this.output = output;
		this.n_inter = 0;
		this.bs = bs;
		l_bytes = 43;
		this.mem_portion = mem_portion;
		this.ms =ms;
		this.reports = new LinkedList<String>();
		this.n_rec_per_block = n_rec_per_block;
		try {
			raf_output = new RandomAccessFile(output, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void mainDS(double min, double max){
		/*range [min, max)*/
		//(int)Math.pow(2, 21), 4096, 43, 0.001, "../../test_sup.bin", "../../result.bin"
		sortInXandY();
		try {
			
			RandomAccessFile raf_in = new RandomAccessFile("../main/"+this.x_sort, "rw");
			double delta = (max-min)/(ms.nb_av-1);
			subSlabs(raf_in, delta,min,max,"test_");
			this.raf_output.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//recursiveDS(this.x_sort, this.y_sort, min, max);
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
	
	public void recursiveDS(String file_x, String file_y, double min_, double max_){
		
		/*Base case*/
		double delta = max_-min_/(ms.nb_av-1);
		int [] file_AL = new int[ms.nb_av-1];
		LinkedList<String>[] ll_slabs= new LinkedList[ms.nb_av-1];
		String s;
		for(int i=0; i<ms.nb_av-1; i++){
			ll_slabs[i] = new LinkedList<String>();
		}
		boolean eof = false;
		if(new File(file_x).length()<=ms.nb_av*bs){
			/*Don't perform recursive call*/
			/*Sweep from top to bottom*/
			
			try {
				RandomAccessFile raf_y = new RandomAccessFile(file_y,"r");
				while(!eof){
					s = raf_y.readUTF();
					ll_slabs=add2List(ll_slabs, s, file_AL, delta, min_, max_);
					ll_slabs=reportIntersections(ll_slabs, file_AL, s, min_, max_, delta);
				}
				raf_y.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			/*Perform recursive call*/
			RandomAccessFile raf_y;
			try {
				raf_y = new RandomAccessFile(file_y,"r");

				while(!eof){
					s = raf_y.readUTF();
					ll_slabs=add2List(ll_slabs, s, file_AL, delta, min_, max_);
					ll_slabs=reportIntersections(ll_slabs, file_AL, s, min_, max_, delta);
				}
				
				raf_y.close();
				
				subSlabs(new RandomAccessFile(file_x, "r"), delta, min_, max_, "prefix");
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void subSlabs(RandomAccessFile raf_in, double delta, double min, double max, String prefix_file) throws IOException{
		/*Generate linkedList with all the files created*/
		boolean eof = false; 

		String line;
		RandomAccessFile raf_aux=null;
		double min_aux=min;
		double max_aux=min+delta;
		double x1,x2;
		String range_pref=min_aux+"_"+max_aux;
		LinkedList<String> aux_list=new LinkedList<String>(); 
		raf_aux = new RandomAccessFile(prefix_file+range_pref+".bin", "rw");
		
		while(!eof){
			try{
				
				line = raf_in.readUTF();
				
				String [] split_line = line.split(",");
				x1 = Double.parseDouble(split_line[0]);
				x2 = Double.parseDouble(split_line[2]);
				/*surpass the maximum*/
				if(x1>max_aux ){
					min_aux=max_aux;
					max_aux=max_aux+delta;
					range_pref=min_aux+"_"+max_aux;
					
					if(raf_aux!=null)
						raf_aux.close();
										
					raf_aux = new RandomAccessFile(prefix_file+range_pref+".bin", "rw");

					for(int i = 0; i<aux_list.size();i++){
						raf_aux.writeUTF(aux_list.get(i));
					}
					
					for(int i=0; i<aux_list.size(); i++)
						aux_list.remove(i);
					
				}
				
				if(x2>max_aux){
					x2 = max_aux;
					aux_list.add(x2+","+split_line[1]+","+split_line[2]+","+split_line[3]);
					line=split_line[0]+","+split_line[1]+","+x2+split_line[3];
				}
				if(x2-x1<delta)
					raf_aux.writeUTF(line);
				

			}catch(EOFException e){
				eof = true;
			}
		}
		if(raf_aux!=null){
			raf_aux.close();
		}
	}
	
	private LinkedList<String>[] add2List(LinkedList<String>[] ll_slabs, String s, int [] file_counter_AL,
			double delta, double min, double max) throws FileNotFoundException{
		
		String [] split_s = s.split(",");
		double x1 = Double.parseDouble(split_s[0]);
		double x2 = Double.parseDouble(split_s[2]);
		if(x1==x2){
		
			for(int i =0; i<ms.nb_av-1; i++){
				double min_aux = min+i*delta;
				double max_aux = min+(i+1)*delta;
				if(min_aux<=x1 && max_aux>x1){
					ll_slabs[i].add(s);
					if(ll_slabs[i].size()>=this.n_rec_per_block){
						//write the content and empty the list
						RandomAccessFile raf = new RandomAccessFile("AL_slab_"+min_aux+"_"+max_aux+"_"+file_counter_AL[i]+".bin" ,"rw");
						ListIterator<String> it = ll_slabs[i].listIterator();
						while(it.hasNext()){
							try {
								raf.writeUTF(it.next());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						try {
							raf.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//write content
						//empty list
						file_counter_AL[i]=file_counter_AL[i]+1;
						ll_slabs[i].clear();
					}
					break;
				}
			}
		}
		
		return ll_slabs;
	}
	
	private LinkedList<String>[] reportIntersections(LinkedList<String>[] ll_slabs, int [] file_counter_AL,String s,double min, double max, double delta){
		
		String [] split_s = s.split(",");
		double x1 = Double.parseDouble(split_s[0]);
		double x2 = Double.parseDouble(split_s[2]);
		double y1 = Double.parseDouble(split_s[1]);
		
		if(x1!=x2){
			for(int i =0; i<ms.nb_av-1; i++){
				if(min+i*delta>=x1 && min+(i+1)*delta<=x2){
					/*1. Check over active list in main memory*/
						/*Check if there are elements in active list if not, search in files*/
					if(ll_slabs[i].size()>0){
						ListIterator<String> it = ll_slabs[i].listIterator();
						while(it.hasNext()){
							
							String aux_s = it.next();
							String [] aux_split = aux_s.split(",");
							double y = Double.parseDouble(aux_split[1]);
							if(y>y1) it.remove();
							else{
								try {
									this.raf_output.writeUTF(aux_split[0]+","+y1);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
									/*report intersection*/
								
							}
						}
					}
					
					/*2. Check over files */
						/*Check if there are files, if not return*/
					if(file_counter_AL[i]>0){
						if(ll_slabs[i].size()>0){
							/*Write the current active list into a file, and empty it*/
							
						}
						/*Load the active list with old vertical segments from previous readings*/
						for(int j = 0; j<file_counter_AL[i]; i++){
							/*Read and process the content of every file one by one
							 * then recreate the new list, even if it contains lesser objects than before*/
						}
					}
					
						/*If there are files, read them one by one, write the current active list in "current" file*/
					
				}
			}
		}
		return ll_slabs;
	}
	
}
