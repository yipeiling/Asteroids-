import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.util.List;

public class testServer extends ServerSocket
{
	 private static final int SERVER_PORT =2014;
	 private static List<Threadplayer> thread_list =new ArrayList<Threadplayer>();//thread list
	 
	 public testServer ()throws IOException
	 {
	    
		 super(SERVER_PORT);
		 try {
	      		while(true)
	        	{
	      			Socket socket = accept();
	      			new Threadplayer(socket);
	        	}
	        }catch (Exception e){}finally{close();}
	 }        		 
	 class Threadplayer extends Thread
	 {
		 ObjectOutputStream objectOutput;
		 ObjectInputStream objectInput;
		 public volatile Shipstate shipstate = new Shipstate();
		 public Threadplayer(Socket socket)throws IOException
		 {
			
			 objectOutput = new ObjectOutputStream(socket.getOutputStream());
	 		 objectInput = new ObjectInputStream(socket.getInputStream());
	 		 start();
		 }
		 
		 public Shipstate getshipstate()
		 {
			 return shipstate;
		 }
 		 public void run() 
 		 {
 			try 
 			{
 				thread_list.add(this);
 				
 				while(true)
 				{
 					Shipstate message = (Shipstate)objectInput.readObject();
 	
 					Thread.sleep(2000); //for Artificial Network Delay
 				
 					for( Threadplayer thread : thread_list)
 					{
	 					
 			
	 					if(thread.getName()!=this.getName()) 
 						{
	 						thread.pushMessage(message);
	 						
	 						
 						}
	 					
 					}
	
 				}
 			}
 			catch (Exception e) {
 				thread_list.remove(this);
 	}
 
 		  }
 		 public void pushMessage(Shipstate state)
 		 {
 			 try {	
 			 		objectOutput.writeObject(state);
 			 		objectOutput.flush();
 			 		objectOutput.reset();
 			 	}catch (Exception e) {}
 		 }
	 }

     public static void main(String[] args) throws IOException 
    
     {
          testServer server =  new testServer();
        
     }
}
