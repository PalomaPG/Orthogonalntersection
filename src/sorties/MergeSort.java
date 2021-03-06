package sorties;

import java.util.LinkedList;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MergeSort {
	/*n: # of records; l: size of (each) record; bs: page size*/
	private int n, l, nb_need;
	private int bs;
	private int n_rec_per_block;
	private int mem;
	public int nb_av;
	private int runs;
	private String input, output;
	private double mem_percent;
	private int io_access;
	private int n_comp;
	private long init_time, end_time;
	private LinkedList<String> o_files;

	public MergeSort(int n, int bs, int l,double mem_percent, String input){
		
		this.n = n;/*Number of lines*/
		this.bs = bs;/*Block size*/
		this.l= l;/*size of a record in file in bytes*/
		this.n_rec_per_block = Math.floorDiv(this.bs, this.l);
		this.nb_need = (int)Math.ceil(((double)n)/this.n_rec_per_block);
		this.output = output;/*output file, complete path*/
		this.mem_percent = mem_percent;
		this.input = input;/*input file, complete path*/
		this.setIO_access(0);
		this.setN_comp(0);
		this.o_files = new LinkedList<String>();
		
	}
	
	public LinkedList<String> getOFiles(){
		return o_files;
	}
	
	
	private void availMem(){
		System.gc();
		Runtime rt = Runtime.getRuntime();
		long alloc_mem = rt.totalMemory() - rt.freeMemory();
		int avail_mem = (int)(rt.maxMemory() - alloc_mem);
		this.mem = (int) (((double)avail_mem)*this.mem_percent);
		this.nb_av= (int)Math.floor((double)this.mem/(double)this.bs); /*Available blocks*/
		this.runs = (int)Math.ceil(((double)this.nb_need)/((double)this.nb_av));
	}
	
	protected void print_sys_prop(){
		String sys_prop = String.format(
		"---------SYSTEM PROPERTIES--------\n\n"+		
		"# of lines or records: %d\n"+
		"Size of each record (bytes): %d\n"+
		"block size in external memory (bytes): %d\n"+
		"Number of records per block: %d\n"+
		"Number of blocks needed: %d\n"+
		"Available memory for sorting (in JVM, bytes): %d\n"+
		"Available memory (# of blocks): %d\n", 
		this.n, this.l, this.bs, this.n_rec_per_block, this.nb_need,this.mem,this.nb_av);
		System.out.println(sys_prop);
	}

	public void  main_mergesort(String [] s, CompareLines cl){
		
		int l = s.length;
		
		if(l>1){
			
			int m;
			String [] s1, s2;
			
			if(l%2==0){ //l even
				m = l/2;
				s2 = new String[m];
				for(int i=m, j=0; i<2*m; i++, j++)
					s2[j] = s[i];
			}
			
			else{
				m = (int) Math.floorDiv(l, 2);
				s2 =new String[m+1];
				for(int i=m, j=0; i<2*m+1; i++, j++)
					s2[j] = s[i];
			}
			
			s1 = new String[m];
			for(int i=0; i<m; i++)
				s1[i] = s[i];
			
			main_mergesort(s1, cl);
			main_mergesort(s2, cl);
			
			main_merge(s1,s2,s, cl);
			
		}
		return;
		
	}
	
	public void main_merge(String [] s1, String [] s2, String [] s, CompareLines cl){
		
        int i = 0, j = 0, k = 0;  
        
        while(s1.length != j && s2.length != k) {
        	
            if(cl.compareLines(s1[j],s2[k])<=0) {  
                s[i] = s1[j];  
                i++;  
                j++;
                this.setN_comp(this.getN_comp() + 1);
            } else {  
                s[i] = s2[k];  
                i++;  
                k++;
                this.setN_comp(this.getN_comp() + 1);
            }
            
        }
        
        while(s1.length != j) { 
        	
        		s[i] = s1[j];  
        		i++;  
        		j++;
        	
        }  
        while(s2.length != k) { 

        		s[i] = s2[k];  
        		i++;  
        		k++;
        	
        } 

	}

	public void read_sort_save(int n_reads, boolean eof, LinkedList<String> n_read_lines, RandomAccessFile src, String out_file,
			CompareLines cl){

		for(int i=0; i<n_reads && !eof; i++){
			
			try{
				String s = src.readUTF();
				if(s==null) break;
				n_read_lines.add(s);
				this.setIO_access(this.getIO_access() + 1);
			}catch(EOFException e){
				eof=true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("lines before sorting");
		//print10elements(n_read_lines);
		/*Sort in main memory*/
		
		//System.out.println(String.format("# estimated lines: %d", n_reads));
		//System.out.println(String.format("real # of lines: %d", lines));
		main_mergesort(n_read_lines.toArray(new String[n_reads]), cl);
		//System.out.println("lines after sorting");
		//print10elements(n_read_lines);
		writeoutput(n_read_lines, out_file);
	}
	
	
	
	public void sortPhase(CompareLines cl){
		this.setInit_time(System.currentTimeMillis());
		availMem();
		print_sys_prop();
		int n_reads; /*Number of records read per run*/
		boolean eof = false;
		LinkedList<String> n_read_lines;
		
		try{
			RandomAccessFile src = new RandomAccessFile(this.input, "r");
			
			if(this.nb_av<this.nb_need){
				System.out.println("there are less available blocks than needed ones");
				n_reads= this.nb_av*this.n_rec_per_block;
				int r = this.runs;
				String o_file;
				int aux_n = this.n;
				o_files = new LinkedList<String>();
				
				while(r>0 && !eof && aux_n>0){
					
					if(aux_n > n_reads){
						aux_n= aux_n- n_reads;
						n_read_lines = new LinkedList<String>();
						o_file=  "../../out"+r+".bin";
						read_sort_save(n_reads, eof, n_read_lines, src,o_file, cl);
						o_files.add(o_file);
						
						//System.out.println(String.format("List size: %d",o_files.size()));
					
					}
					else{
						
						n_read_lines = new LinkedList<String>();
						o_file=  "../../out"+r+".bin";
						read_sort_save(aux_n, eof, n_read_lines, src, o_file, cl);
						o_files.add(o_file);
						//System.out.println(String.format("List size (last run): %d",o_files.size()));
						aux_n=0;
					}
					
					r--;
				}
			}
			else{
				
				System.out.println("are enough available blocks");
				n_read_lines = new LinkedList<String>();
				n_reads = this.n;
				read_sort_save(n_reads, eof, n_read_lines, src, "out.txt", cl);
				this.output = cl.getOut_file();
				writeoutput(n_read_lines, this.output);
			}
			
			src.close();
			
		}catch(Exception e){
					e.printStackTrace();
		}
		
	}
	
	
	public void writeoutput(LinkedList<String> s, String out_path){
		
		try{
			RandomAccessFile dest = new RandomAccessFile(out_path, "rw");
		
			for(int i=0; i<s.size(); i++){
				dest.writeUTF(s.get(i));
				this.setIO_access(this.getIO_access() + 1);
			}
			dest.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void print10elements(String [] s){
		System.out.println("First 5 elements");
		if(s.length > 10){
			for(int i = 0; i<5; i++){
				System.out.println(s[i]);
			}
			System.out.println("Last 5 elements");
			for(int i= s.length-5; i<s.length; i++){
				System.out.println(s[i]);
			}
		}
		
		else{
			for(int i=0; i<s.length; i++){
				System.out.println(s[i]);
			}
		}
	}
	
	/*MERGE PHASE*/
	public void mergePhase(LinkedList<String> files, CompareLines cl, int rec){
		/*No auxiliary files were generated, the whole input file fit into main memory*/
		if(files.isEmpty()) {
			this.setEnd_time(System.currentTimeMillis());
			return;
			}
		
		else{
			/*Calculate the # merge runs*/
			//System.out.println(String.format("# files to merge: %d",files.size()));
			int merge_runs =(int)Math.ceil(((double)files.size())/((double)this.nb_av-1));
			if(merge_runs==1){
				/*#files <= #available blocks in memory - 1*/
				System.err.println(String.format("# FILES TO MERGE: %d", files.size()));
				for(int i=0; i<files.size();i++)
					System.err.println(files.get(i));
				
				//System.err.println(String.format("List size... files: %d", files.size()));
				this.output =cl.getOut_file();
				merge_runs(files, cl, this.output);
				this.setEnd_time(System.currentTimeMillis());
				return;
			}
			
			else{
				
				System.out.println("There are more files than available blocks");
				LinkedList<String> out_m_files = new LinkedList<String>();
				LinkedList<String> trim_list;
				int run_ = 1;
				String out_run;
				while(run_<=merge_runs){
					trim_list = getNextFiles(run_, this.nb_av-1, files);
					
					out_run = "../../out_run"+run_+"_rec_"+rec+".bin";
					merge_runs(trim_list, cl, out_run);
					out_m_files.add(out_run);
					/*read the next nb_av-1 blocks
					for(int i = 0; i<trim_list.size();i++){
						System.out.println(trim_list.get(i));
					}*/
					run_++;
			
				}
				rec = rec+1;
				mergePhase(out_m_files, cl, rec);
			}
		}
	}
		
	private LinkedList<String> getNextFiles(int run_, int off, LinkedList<String> files) {
		// TODO Auto-generated method stub
		LinkedList<String> trim_lst= new LinkedList<String>();
		//System.out.println("Creating trimmed list");
		for(int i=0; i<this.nb_av-1; i++){
			try{
				trim_lst.add(files.get(i+((run_-1)*off)));
			}catch(IndexOutOfBoundsException e){
				System.err.println(String.format("Error at index: %d, run: %d", i, run_));
				break;
			}
		}
		//System.out.println("Creation done: Trimmed list ready to be processed");
		return trim_lst;
	}

	public void merge_runs(LinkedList<String> files, CompareLines cl, String out){
		
		LinkedList<String> reg2merge_l;
		RandomAccessFile [] raf_array = getRAFfromStringList(files);
				
		try {
			RandomAccessFile o_raf = new RandomAccessFile(out, "rw");
			for( int off_buffer=0 ; off_buffer<this.nb_av; off_buffer++ ){
				reg2merge_l = new LinkedList<String>();
				//reg2merge = new String[files.size()*this.n_rec_per_block];
	
				for(int raf_i=0; raf_i<raf_array.length;raf_i++){
					for(int i=0; i<this.n_rec_per_block; i++){

						try{
							String s = raf_array[raf_i].readUTF();
							this.setIO_access(this.getIO_access() + 1);
							reg2merge_l.add(s);
						}catch(EOFException e){
							//System.out.println();
							break;
						}
					}
				}
				/*Mergesort & write re2merge*/
				String [] reg2merge = new String[reg2merge_l.size()];
				reg2merge_l.toArray(reg2merge);
				main_mergesort(reg2merge, cl);
				//System.err.println("Elementos ordenados");
				//print10elements(reg2merge);
				write_raf(reg2merge, o_raf);
			
			}

			for(int j=0; j<raf_array.length;j++)
				raf_array[j].close();
		
			o_raf.close();
		
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void write_raf(String[] reg2merge, RandomAccessFile o_raf) throws IOException {
		// TODO Auto-generated method stub
		for(int i=0; i<reg2merge.length; i++){
			//System.out.println(reg2merge[i]);
			o_raf.writeUTF(reg2merge[i]);
			this.setIO_access(this.getIO_access() + 1);
		}
	}


	public RandomAccessFile [] getRAFfromStringList(LinkedList<String> files){
		
		RandomAccessFile [] raf_array = new RandomAccessFile[files.size()];
		
		for(int i=0; i<files.size();i++){
			try {
				raf_array[i] = new RandomAccessFile(files.get(i), "r");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*System.out.println(raf_array.length);*/
		return raf_array;
	}
	
	public void deleteFiles(){
		
	    
	    try (DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get("../../"), "out*")) {
	        for (final Path newDirectoryStreamItem : newDirectoryStream) {
	            Files.delete(newDirectoryStreamItem);
	        }
	    } catch (final Exception e) {
	        
	        e.printStackTrace();
	    }
	    
	}

	public int getIO_access() {
		return io_access;
	}

	public void setIO_access(int io_access) {
		this.io_access = io_access;
	}

	public int getN_comp() {
		return n_comp;
	}

	public void setN_comp(int n_comp) {
		this.n_comp = n_comp;
	}

	public long getInit_time() {
		return init_time;
	}

	public void setInit_time(long init_time) {
		this.init_time = init_time;
	}

	public long getEnd_time() {
		return end_time;
	}

	public void setEnd_time(long end_time) {
		this.end_time = end_time;
	}
}
