package dist_sweep;

import java.io.EOFException;
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
		
	}

	public void mainDS(double min, double max){
		/*range [min, max)*/
		//(int)Math.pow(2, 21), 4096, 43, 0.001, "../../test_sup.bin", "../../result.bin"
		sortInXandY();
		try {
			RandomAccessFile raf_in = new RandomAccessFile("../main/"+this.x_sort, "rw");
			double delta = (max-min)/(ms.nb_av-1);
			subSlabs(raf_in, delta,min,max,"test_");
			
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
		
		
	}
	
	private void subSlabs(RandomAccessFile raf_in, double delta, double min, double max, String prefix_file) throws IOException{
		
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
	
	private LinkedList<double []>[] reportIntersections(LinkedList<double []>[] ll_slabs, double [] splited, String inter, double min, double max, double delta){
		
		LinkedList<double []> ll_slab=null;
		for(int i =0; i<ms.nb_av-1; i++){
			if(min+i*delta<=splited[0] && min+(i+1)*delta>splited[0]){
				for(int j=0; j<ll_slabs[i].size(); j++){
					//Si hay interseccion, verificar que horizontal cruza todo el intervalo, si no, no reportar
					
				}
				//Eliminar las que ya estan pasadas
				ListIterator<double []> it = ll_slabs[i].listIterator();
				while(it.hasNext()){
					double [] aux = it.next();
					if(aux[0]>splited[1])
						it.remove();
				}

				break;
			}
			
		}
		return ll_slabs;
	}
	
}
