package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dist_sweep.DistributionSweep;
import seggen.*;
import sorties.*;

public class Main {

	
	public static void main(String [] args) throws FileNotFoundException{
		
		if(args.length<1 || args.length>1){
			throw new java.lang.Error("Please, enter an execution option (and just one): -g "
					+ "(to generate files for tests) or -e (to running the program over already existent files ) ");
		}
		double [] alpha = {0.25, 0.5, 0.75};
		if(args[0].length()==2){
			if(args[0].equals("-g")){
				System.err.println("Generating files");
				int init = 100;
				int end = 200;
				
				
				Set <Integer> range = IntStream.rangeClosed(9, 21).boxed().collect(Collectors.toSet());
				Integer [] expr = (Integer[])range.toArray(new Integer[range.size()]);
				
				System.err.println("Gaussian distribution");
				
				for(int j=0; j<alpha.length;j++){
					for(int i=0; i<expr.length; i++){
						int n =(int)Math.pow(2,expr[i]);
						String filename = String.format("gauss_%d_%d.bin", (int)(alpha[j]*100),expr[i]);
						genFile(true, init, end, alpha[j], n, filename);
					}
				}
				
				System.err.println("Uniform distribution");
				for(int j=0; j<alpha.length;j++){
					for(int i=0; i<expr.length; i++){
						int n =(int)Math.pow(2,expr[i]);
						String filename = String.format("uni_%d_%d.bin", (int)(alpha[j]*100),expr[i]);
						genFile(false, init, end, alpha[j], n, filename);
					}
				}

			}
			else if(args[0].equals("-m")){
				System.err.println("Executing code over existent files");
				/*List all files of a certain distribution and alpha ratio*/

				/*List files of gaussian distribution*/
				Path currentRelativePath = Paths.get("");
				String s = currentRelativePath.toAbsolutePath().toString();
				File dir = new File(s);
				RandomAccessFile raf;

				for(double a : alpha){
					
					int alpha_ = (int) (a*100);
					raf =  new RandomAccessFile("gauss_param_measurements"+alpha_+".csv", "rw");
					File [] files = dir.listFiles(new FilenameFilter() {
						@Override
					    public boolean accept(File dir, String name) {
					        return name.matches("gauss_"+alpha_+"_(.*).bin");
					    }
					});
					
					for (File file_ : files) {
						//System.err.println(file_);
						int n_reg = Integer.parseInt((file_.getName().split("_|.bin")[2]));
						MergeSort ms = new MergeSort((int)Math.pow(2, n_reg), 4096, 43, 0.001, file_.getName());
						ms.deleteFiles();
						CompareLines cl = new CompareLinesInX("x_sorted_"+file_.getName());
						ms.sortPhase(cl);
						ms.mergePhase(ms.getOFiles(), cl,0);
						ms.deleteFiles();
						cl = new CompareLinesInX("y_sorted_"+file_.getName());
						ms.sortPhase(cl);
						ms.mergePhase(ms.getOFiles(), cl,0);
						ms.deleteFiles();
						try {
							raf.writeUTF(String.format("%d,%d,%d", ms.getIO_access(), ms.getN_comp(), ms.getEnd_time()-ms.getInit_time()));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//System.err.println(String.format("IO accesses: %d, Comparisions: %d, RT (ms): %d", 
						//	ms.getIO_access(), ms.getN_comp(), ms.getEnd_time()-ms.getInit_time()));
					}
					try {
						raf.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				for(double a : alpha){
					
					int alpha_ = (int) (a*100);
					raf =  new RandomAccessFile("uni_param_measurements"+alpha_+".csv", "rw");
					File [] files = dir.listFiles(new FilenameFilter() {
						@Override
					    public boolean accept(File dir, String name) {
					        return name.matches("uni_"+alpha_+"_(.*).bin");
					    }
					});
					
					for (File file_ : files) {
						//System.err.println(file_);
						int n_reg = Integer.parseInt((file_.getName().split("_|.bin")[2]));
						MergeSort ms = new MergeSort((int)Math.pow(2, n_reg), 4096, 43, 0.001, file_.getName());
						ms.deleteFiles();
						CompareLines cl = new CompareLinesInX("x_sorted_"+file_.getName());
						ms.sortPhase(cl);
						ms.mergePhase(ms.getOFiles(), cl,0);
						ms.deleteFiles();
						cl = new CompareLinesInX("y_sorted_"+file_.getName());
						ms.sortPhase(cl);
						ms.mergePhase(ms.getOFiles(), cl,0);
						ms.deleteFiles();
						try {
							raf.writeUTF(String.format("%d,%d,%d", ms.getIO_access(), ms.getN_comp(), ms.getEnd_time()-ms.getInit_time()));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//System.err.println(String.format("IO accesses: %d, Comparisions: %d, RT (ms): %d", 
						//	ms.getIO_access(), ms.getN_comp(), ms.getEnd_time()-ms.getInit_time()));

					}
					try {
						raf.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				System.err.println("DONE");
			}
			else if(args[0].equals("-t")){
				String s1 = "gauss_50_12.bin";
				String s2 = "uni_75_9.bin";
				
				String [] s1_split = s1.split("_|.bin");
				System.err.println(s1_split[2]);
				
				String [] s2_split = s2.split("_|.bin");
				System.err.println(s2_split[2]);
			}
			else if(args[0].equals("-d")){
				System.err.println("Executing code over existent files: DistributionSweep");
				MergeSort ms = new MergeSort((int)Math.pow(2, 9), 4096, 43, 0.001, "test.bin");
				int bs = 4096;/*Block size*/
				int l= 43;/*size of a record in file in bytes*/
				int n_rec_per_block = Math.floorDiv(bs, l);
				DistributionSweep ds = new DistributionSweep("test.bin","test_out.bin",4096, 0.001, ms, n_rec_per_block);
				ds.mainDS(100, 200);
				//DistributionSweep("../../test_sup.bin","../../test_sup_out.bin",4096, 0.001, ms, n_rec_per_block);
			}
			else{
				throw new java.lang.Error("Please, enter an execution option (and just one): -g "
						+ "(to generate files for tests) or -e (to running the program over already existent files ) ");
			}
		}
		else{
			throw new java.lang.Error("Please, enter an execution option (and just one): -g "
					+ "(to generate files for tests) or -e (to running the program over already existent files ) ");
		}

		//printData(new File("test.bin"), n);
		//MergeSort ms = new MergeSort(n, 4096, 43, 0.03, "test_sup.bin", "result.txt");
		//ms.sort_phase();
	}
	
	



	public static void createDir(String dir){
		File dir_ = new File(dir);
		
		
		if (!dir_.exists()) {
		    System.out.println("creating directory: " + dir_.getName());
		    boolean result = false;

		    try{
		        dir_.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}
	}
	
	public static void printWholeData(File f, int n){
		
		try{
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			for (int i = 0; i < n; i++) {
				System.out.println(dis.readUTF());
			}
			dis.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	public static void printData(File f,int n){
		
	   try {
		   DataInputStream dis = new DataInputStream(new FileInputStream(f));
		  
		   for (int i = 0; i < 5; i++) {
			System.out.println(dis.readUTF());
		   }
		   //dis.skipBytes(1);
		   System.out.println("------------------------");
		   int b =dis.skipBytes(43);
		   System.out.println(String.format("Number of bytes skipped: %d",b));
		   System.out.println("------------------------");
		   for (int i = 0; i < 5; i++) {
			System.out.println(dis.readUTF());
		   }
		   dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	public static void swapInCaseAndWrite(DataOutputStream writer,double x1, double x2, double y1, double y2) throws IOException{
		
		if(x1<=x2 && y1<=y2)
			writer.writeUTF(String.format("%f,%f,%f,%f", x1,y1,x2,y2).toString());
		
		else if(x1<=x2 && y1>y2)
			writer.writeUTF(String.format("%f,%f,%f,%f", x1,y2,x2,y1).toString());
		
		else if(x1>x2 && y1<=y2)
			writer.writeUTF(String.format("%f,%f,%f,%f", x2,y1,x1,y2).toString());
		
		else
			writer.writeUTF(String.format("%f,%f,%f,%f", x2,y2,x1,y1).toString());
	}
	
	public static void writeSegment(SegGen gen, boolean norm, DataOutputStream writer, int o) throws IOException{
		
		double x1,y1;
		double x2, y2;

		
		if(o==0){
			/*Constant in y*/
			y1=gen.uniccoord();
			y2 = y1;
			x2 = gen.uniccoord();
			x1 = gen.uniccoord();
		}
		else{
			/*Constant in x*/
			if(norm){
				x1=gen.normccoord(49.5, 0.09);
				x2 = x1;
				y1 = gen.uniccoord();
				y2 = gen.uniccoord();
			}
			else{
				x1=gen.uniccoord();
				x2=x1;
				y1 = gen.uniccoord();
				y2 = gen.uniccoord();
				
			}
		}
		
		swapInCaseAndWrite(writer,x1,x2,y1,y2);
	}
	
	public static void genFile(boolean norm, double init, double end, double alpha, int n, String filename){
		
		SegGen gen = new SegGen(init, end);
		int n_hor = (int)(alpha*n);
		int n_ver = n - n_hor;
		DataOutputStream  writer;
		File f=null;
		try{
		    f = new File (filename);
		    writer = new DataOutputStream(new FileOutputStream(f));
			//writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("filename.txt"), "utf-8"));
			int o;	
			
			while(n>0){

				o = gen.orientation();
				
				if(o == 0 && n_hor>0){
					writeSegment(gen, norm, writer, o);
					--n_hor;
					--n;
				}
				else if(o== 1 && n_ver>0){
					writeSegment(gen, norm, writer, o);
					--n_ver;
					--n;
				}
			}
			writer.close();
			
			//printData(new DataInputStream(new FileInputStream(f)), n);
		}catch(Exception e){
			System.err.println("Something happened at creating or writing file:"+e.toString());
		}
		
	}

}
