/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                            FILE CMAIN
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.converter;

// Java classes
import java.io.*;
import java.lang.*;
import java.util.*;

// classes
import agle.lib.loaders.*;

///////////////////////////////// CMAIN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CMAIN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CMain
{	
	public static C3DModel World = new C3DModel();
	
	public static void main(String[] args) 
	{
		C3DModelLoader loader = new C3DModelLoader();
		World = loader.Import3DS("../data/" + args[0]);
		CMain conv = new CMain();
		conv.wtf(args[1]);
	}
	
	// funzione write to file
	public void wtf(String filename)
	{
		FileOutputStream   out;
		ObjectOutputStream s_out;	
		try {
			out   = new FileOutputStream("../data/" + filename);
			s_out = new ObjectOutputStream(out);
			s_out.writeObject((C3DModel)World);
			s_out.close();
		System.out.println("Conversion Done");  
		} catch (IOException e) {
			e.printStackTrace(); 
			System.out.println("Impossibile scrivere e chiudere il file");  
		}
	}
}
