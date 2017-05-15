package sorties;

import java.util.Comparator;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MergeSort {
	/*n: # of records; l: size of (each) record; bs: page size*/
	private int n, l, nb_need;
	private int bs;
	private int n_rec_per_block;
	private int mem;
	private int nb_av;
	private int runs;
	private String input, output;
	private Comparator <String> cmp;
	private double mem_percent;

	public MergeSort(int n, int bs, int l,double mem_percent, String input,  String output){
		this.n = n;/*Number of lines*/
		this.bs = bs;/*Block size*/
		this.l= l;/*size of a record in file in bytes*/
		this.n_rec_per_block = Math.floorDiv(this.bs, this.l);
		this.nb_need = (int)Math.ceil(((double)n)/this.n_rec_per_block);
		this.output = output;/*output file, complete path*/
		this.mem_percent = mem_percent;
		this.input = input;/*input file, complete path*/
		
	}
	
	
	private void avail_mem(){
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
	
	public int compareLines(String in1, String in2){
		double s1 = Double.parseDouble(in1.split(",")[0]);
		double s2 = Double.parseDouble(in2.split(",")[0]);
		
		if(s1>s2) return 1;
		else if (s1<s2) return -1;
		else return 0;
	}
	
	
	public void  main_mergesort(String [] s){
		
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
			
			main_mergesort(s1);
			main_mergesort(s2);
			
			main_merge(s1,s2,s);
			
			
		}
		return;
		
	}
	
	public void main_merge(String [] s1, String [] s2, String [] s){
		
        int i = 0, j = 0, k = 0;  
        
        while(s1.length != j && s2.length != k) {  
            if(compareLines(s1[j],s2[k])<=0) {  
                s[i] = s1[j];  
                i++;  
                j++;  
            } else {  
                s[i] = s2[k];  
                i++;  
                k++;  
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

	public void read_sort_save(int n_reads, boolean eof, String [] n_read_lines, RandomAccessFile src, String out_file){
		
		int lines = 0;
		for(int i=0; i<n_reads && !eof; i++){
			
			try{
				n_read_lines[i]=src.readUTF();
				lines++;
			}catch(EOFException e){
				eof=true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("lines before sorting");
		print10elements(n_read_lines);
		/*Sort in main memory*/
		
		//System.out.println(String.format("# estimated lines: %d", n_reads));
		//System.out.println(String.format("real # of lines: %d", lines));
		main_mergesort(n_read_lines);
		System.out.println("lines after sorting");
		print10elements(n_read_lines);
	}
	
	
	
	public void sort_phase(){
		avail_mem();
		print_sys_prop();
		int n_reads; /*Number of records read per run*/
		boolean eof = false;
		String [] n_read_lines;
		try{
			RandomAccessFile src = new RandomAccessFile(this.input, "r");
			
			if(this.nb_av<this.nb_need){
				System.out.println("there are less available blocks than needed ones");
				n_reads= this.nb_av*this.n_rec_per_block;
				int r = this.runs;
				int count = 0;
				int aux_n = this.n;
				
				while(r>0 && !eof && aux_n>0){
					
					count++;
					if(aux_n > n_reads){
						aux_n= aux_n- n_reads;
						n_read_lines = new String[n_reads];
						read_sort_save(n_reads, eof, n_read_lines, src, "out"+r+".bin");
					
					}
					else{
						
						n_read_lines = new String[aux_n];
						read_sort_save(aux_n, eof, n_read_lines, src, "out"+r+".bin");
						aux_n=0;
					}
					
					r--;
				}
			}
			else{
				System.out.println("are enough available blocks");
				n_read_lines = new String[this.n];
				n_reads = this.n;
				read_sort_save(n_reads, eof, n_read_lines, src, "out.txt");

				writeoutput(n_read_lines, "test_out.bin");
			}
			
			src.close();
			
		}catch(Exception e){
					e.printStackTrace();
		}
		
	}
	
	
	public void writeoutput(String [] s, String out_path){
		
		try{
			RandomAccessFile dest = new RandomAccessFile(out_path, "rw");
		
			for(int i=0; i<s.length; i++){
				dest.writeBytes(s[i]);
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
	
	
}
