package dist_sweep;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

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
				
				raf_aux.writeUTF(line);
				

			}catch(EOFException e){
				eof = true;
			}
		}
		if(raf_aux!=null){
			raf_aux.close();
		}
	}
	
	private LinkedList<double []>[] add2List(LinkedList<double []>[] ll_slabs, double [] splited, double delta, double min, double max){
		
		for(int i =0; i<ms.nb_av-1; i++){
			if(min+i*delta<=splited[0] && min+(i+1)*delta>splited[0]){
				double [] ys = {splited[1], splited[3]};
				ll_slabs[i].add(ys);
				break;
			}
		}
		
		return ll_slabs;
	}
	
	private LinkedList<double []>[] reportIntersections(LinkedList<double []>[] ll_slabs, double [] splited, String inter, double min, double max, double delta){
		
		LinkedList<double []> ll_slab=null;
		for(int i =0; i<ms.nb_av-1; i++){
			if(min+i*delta<=splited[0] && min+(i+1)*delta>splited[0]){
				for(int j=0; j<ll_slabs[j].size(); j++){
					//Si hay interseccion, verificar que horizontal cruza todo el intervalo, si no, no reportar
					
				}
				//Eliminar las que ya estan pasadas
				for(int j=0; j<ll_slabs[j].size(); j++){
					if(ll_slabs[i].get(j)[0]>splited[1]){
						ll_slabs[i].remove(j);
					}
				}
				break;
			}
			
		}
		return ll_slabs;
	}
	
}
