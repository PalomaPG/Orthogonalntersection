package main;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;


import seggen.*;
import sorties.*;

public class Main {

	
	public static void main(String [] args) throws FileNotFoundException{
		int init = 100;
		int end = 200;
		double alpha = 0.75;
		int n =(int)Math.pow(2,21);
		//double alpha = args[];
		//String test= "hola que tal";
		//byte [] b = test.getBytes();
		//System.out.println(b.length);
		//Syste.out.println()
		
		
		//genFile(false, init, end, alpha, n);
		//printData(new File("test.bin"), n);
		MergeSort ms = new MergeSort(n, 4096, 43, 0.03, "test_sup.bin", "result.txt");
		ms.sort_phase();
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
		
		writer.writeUTF(String.format("%f,%f,%f,%f", x1,y1,x2,y2).toString());
	}
	
	public static void genFile(boolean norm, double init, double end, double alpha, int n){
		
		SegGen gen = new SegGen(init, end);
		int n_hor = (int)(alpha*n);
		int n_ver = n - n_hor;
		DataOutputStream  writer;
		File f=null;
		try{
		    f = new File ("test_sup.bin");
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
