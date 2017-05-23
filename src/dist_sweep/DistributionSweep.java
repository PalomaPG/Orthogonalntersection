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
	private int rec_call;
	
	
	public DistributionSweep(String input, String output,int bs, double mem_portion, MergeSort ms, int n_rec_per_block){
		this.rec_call = 0;
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
		System.err.println(String.format("b: %d", ms.nb_av));
		recursiveDS(this.x_sort, this.y_sort, min, max);
		try {
			raf_output.close();
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
		
		System.err.println(String.format("Range: [%f, %f ]", min_, max_));
		/*Base case*/
		double delta = max_-min_/(ms.nb_av-1);
		int [] file_AL = new int[ms.nb_av-1];
		LinkedList<String>[] ll_slabs= new LinkedList[ms.nb_av-1];
		LinkedList<String>[] al_full = new LinkedList[ms.nb_av-1];
		String s;
		for(int i=0; i<ms.nb_av-1; i++){
			ll_slabs[i] = new LinkedList<String>();
			al_full[i] = new LinkedList<String>();
		}
		boolean eof = false;
		
		if(new File(file_x).length()<=ms.nb_av*bs){
			/*Don't perform recursive call*/
			/*Sweep from top to bottom*/
			
			try {
				RandomAccessFile raf_y = new RandomAccessFile(file_y,"r");
				while(!eof){
					try{
						s = raf_y.readUTF();
						ll_slabs=add2List(ll_slabs, s, file_AL, delta, min_, max_, al_full);
						ll_slabs=reportIntersections(ll_slabs, s, min_, max_, delta, al_full);
					}catch(EOFException e){
							eof=true;
					}
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
					try{
						s = raf_y.readUTF();
						ll_slabs=add2List(ll_slabs, s, file_AL ,delta, min_, max_, al_full);
						ll_slabs=reportIntersections(ll_slabs, s, min_, max_, delta, al_full);
					}catch(EOFException e){
						eof=true;
					}
				}
				
				raf_y.close();
				
				LinkedList<String> sub_slabs = subSlabs(new RandomAccessFile(file_x, "r"), delta, min_, max_, "prefix");
				
				ListIterator<String> it = sub_slabs.listIterator();
				int k = 0;
				while(it.hasNext() && min_+(k+1)*delta<=max_){
					String file_ = it.next();
					recursiveDS(file_, file_y, min_+k*delta, min_+(k+1)*delta);
					k++;
					
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private LinkedList<String> subSlabs(RandomAccessFile raf_in, double delta, double min, double max, String prefix_file) throws IOException{
		/*Generate linkedList with all the files created*/
		boolean eof = false; 
		LinkedList<String> ll_slabs = new LinkedList<String>();
		
		String line;
		RandomAccessFile raf_aux=null;
		double min_aux=min;
		double max_aux=min+delta;
		double x1,x2;
		String range_pref=min_aux+"_"+max_aux;
		LinkedList<String> aux_list=new LinkedList<String>(); 
		String file_name = prefix_file+range_pref+".bin";
		ll_slabs.add(file_name);
		raf_aux = new RandomAccessFile(file_name, "rw");
		
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
					file_name =	prefix_file+range_pref+".bin";				
					raf_aux = new RandomAccessFile(file_name, "rw");
					ll_slabs.add(file_name);
					
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
		
		return ll_slabs;
	}
	
	private LinkedList<String>[] add2List(LinkedList<String>[] ll_slabs, String s, int [] file_counter_AL,
			double delta, double min, double max, LinkedList<String>[] active_lists) throws FileNotFoundException{
		
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
						String al_full = "AL_slab_"+min_aux+"_"+max_aux+"_"+file_counter_AL[i]+".bin";
						RandomAccessFile raf = new RandomAccessFile( al_full,"rw");
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
	
	private LinkedList<String>[] reportIntersections(LinkedList<String>[] ll_slabs, String s,
			double min, double max, double delta, LinkedList<String>[] al_full){
		
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
						ll_slabs[i] = process_lst(ll_slabs[i], y1);
					}
					
					/*2. Check over files */
						/*Check if there are files, if not return*/
					if(al_full[i].size()>0){
						double min_aux= min+i*delta;
						double max_aux = min+(i+1)*delta;
						String prefix_al = "AL_slab_"+min_aux+"_"+max_aux;
						String curr = prefix_al+"_current.bin";
						if(ll_slabs[i].size()>0){
							/*Write the current active list into a file, and empty it*/
							ll_slabs[i]= ll2fileAndClear(ll_slabs[i], curr);
						}
						
						/*Load the active list with old vertical segments from previous readings*/
						ListIterator<String> it_al = al_full[i].listIterator();
						while(it_al.hasNext()){
							ll_slabs[i] = file2ll(ll_slabs[i], it_al.next());
							ll_slabs[i] = process_lst(ll_slabs[i], y1);
							ll_slabs[i] = ll2fileAndClear(ll_slabs[i], it_al.next());
						}
						ll_slabs[i].clear();
						ll_slabs[i] = file2ll(ll_slabs[i], curr);
					}
					
						/*If there are files, read them one by one, write the current active list in "current" file*/
					
				}
			}
		}
		return ll_slabs;
	}
	
	public LinkedList<String> file2ll(LinkedList<String> ll, String filename){
				
		try {
			RandomAccessFile raf = new RandomAccessFile(filename, "r");
			boolean eof = false;
			while(!eof){
				try{
					String s = raf.readUTF();
					ll.add(s);
				}catch(EOFException e){
					eof=true;
				}
			}
			
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ll;
	}
	
	public LinkedList<String> ll2fileAndClear(LinkedList<String> ll, String filename){
		
		ListIterator<String> it =ll.listIterator();
		try {
			RandomAccessFile raf = new RandomAccessFile(filename,"rw");
			while(it.hasNext()){
				raf.writeUTF(it.next());
			}
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ll.clear();
		return ll;
	}
	
	public LinkedList<String> process_lst(LinkedList<String> ll, double y1){
		
		ListIterator<String> it = ll.listIterator();
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
		return ll;
	}
}
