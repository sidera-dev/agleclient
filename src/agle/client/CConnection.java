/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                            FILE CConnection
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.client;

// Java classes
import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

///////////////////////////////// CConnection \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CConnection\\\\\\\\\\\\\\\\\\\\\\
public class CConnection  
{
	Socket 			socket;
	String			host;
	int 			porta;	
	PrintWriter		out;
	BufferedReader	in;
    
	///////////////////////////////// CConnection \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This is the class constructor
	/////
	///////////////////////////////// CConnection \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public CConnection()
	{
		socket = null;
		host   = "localhost";
		porta  = 4000;	
	}
    
	///////////////////////////////// LOAD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Create connection with server   
	/////
	///////////////////////////////// LOAD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean creaConnessione(String Nome, String Password)
	{        
		try { 
			//create socket
			socket = new Socket(host, porta);
			// check server will respond in 20000 msec 
			//socket.setSoTimeout(200000);           		
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
   			in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//connection string
			String connect = "connect " + Nome + " " + Password;
			out.println(connect);            
			return true;
			           
		} catch (UnknownHostException e ) { 
			System.err.println("This host doesn't exits:" + host);
			return false;
                
		} catch (IOException e) { 
			System.err.println("Non si riesce a ottenere l'I/O per la connessione.");
			return false;
		}
	} 
	
	///////////////////////////////// SEND \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	this function write in the socket
	/////
	///////////////////////////////// SEND \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean Send(String passString)
	{
		String msg = "";
		try {
			msg = passString;
			out.println(msg); 
			return true;
   		} catch (Exception e) {
     		System.out.println("Error in socket reading");
     		return false;
   		}
	}
	
	///////////////////////////////// WAIT4DATA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	this function read from the socket and wait for msg string 
	/////
	///////////////////////////////// WAIT4DATA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public String Wait4Data(String msg)
	{  
		String line = "";	     
		try {
			while(true)
			{
				line = in.readLine();
				if(line.equals(msg))
					break;
     			System.out.println(line);
 			}
     		
   		} catch (Exception e){
     		System.out.println("Error in socket reading");
     		System.out.println(e);
     		System.exit(1);
   		}
 		return line;
	}        
    
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Questa funzione legge cosecutivamente dalla socket     
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public String Update()
	{  
		String line = "";	     
		try {
     		line = in.readLine();
     		
   		} catch (Exception e){
     		System.out.println("Error in socket reading");
     		System.exit(1);
   		}
 		return line;
	}   
    
	///////////////////////////////// CHIUDICINNESSIONE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Chiude la connessione con il server
	/////
	///////////////////////////////// CHIUICONNESSIONE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void chiudiConnessione(boolean connesso)
	{
		try {
			in.close();
			out.close();                               
			socket.close();
										
		} catch (IOException e) {
			System.out.println("Error in socket closing.");
		} 
	}                       
}
