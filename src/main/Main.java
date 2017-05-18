package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seggen.*;
import sorties.*;

public class Main {

	
	public static void main(String [] args) throws FileNotFoundException{
		
		if(args.length<1 || args.length>1){
			throw new java.lang.Error("Please, enter an execution option (and just one): -g "
					+ "(to generate files for tests) or -e (to running the program over already existent files ) ");
		}
		
		if(args[0].length()==2){
			if(args[0].equals("-g")){
				System.err.println("Generating files");
				int init = 100;
				int end = 200;
				String data_dir = "../../data/";
				String gauss_dir = "GAUSSIAN_DISTR/";
				String uni_dir = "UNIFORM_DISTR/";
				createDir(data_dir);
				gauss_dir = data_dir+gauss_dir;
				createDir(gauss_dir);
				uni_dir = data_dir+uni_dir;
				createDir(uni_dir);
				
				double [] alpha = {0.25, 0.5, 0.75};
				Set <Integer> range = IntStream.rangeClosed(9, 10).boxed().collect(Collectors.toSet());
				Integer [] expr = (Integer[])range.toArray(new Integer[range.size()]);
				
				System.err.println("Gaussian distribution");
				
				for(int j=0; j<alpha.length;j++){
					for(int i=0; i<expr.length; i++){
						int n =(int)Math.pow(2,expr[i]);
						String filename = String.format("gauss_%d_%d.bin", (int)(alpha[j]*100),expr[i]);
						genFile(true, init, end, alpha[j], n, gauss_dir+filename);
					}
				}
				
				System.err.println("Uniform distribution");
				for(int j=0; j<alpha.length;j++){
					for(int i=0; i<expr.length; i++){
						int n =(int)Math.pow(2,expr[i]);
						String filename = String.format("uni_%d_%d.bin", (int)(alpha[j]*100),expr[i]);
						genFile(false, init, end, alpha[j], n, uni_dir+filename);
					}
				}

			}
			else if(args[0].equals("-e")){
				System.err.println("Executing code over existent files");
				MergeSort ms = new MergeSort((int)Math.pow(2, 21), 4096, 43, 0.001, "../../test_sup.bin", "../../result.bin");
				CompareLines cl = new CompareLinesInY();
				ms.sort_phase(cl);
				ms.mergePhase(ms.getOFiles(), cl,0);
				System.err.println("DONE");
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
		if(norm){
			x1 = gen.normccoord(49.5, 0.09);
			y1 = gen.uniccoord();
		}
		else{
			x1 = gen.uniccoord();
			y1 = gen.uniccoord();
		}
		
		if(o==0){
			/*Constant in y*/
			y2 = y1;
			x2 = gen.uniccoord();
		}
		else{
			/*Constant in x*/
			x2 = x1;
			y2 = gen.uniccoord();
		}
		
		swapInCaseAndWrite(writer, x1,x2,y1,y2);
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
