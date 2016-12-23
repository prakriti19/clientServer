//package clientServer;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	private ExecutorService executorService = Executors.newCachedThreadPool();
	private ServerSocket  serverSocket;
	static ConcurrentHashMap<Socket,Integer> clientMap = new ConcurrentHashMap<Socket,Integer>();
	static int clientNum = 1;

	private static final int sPort = 8000;   //The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		System.out.println(" server ");
		Server myServer = new Server();
		myServer.runServer();
        	
		
    }
	
	private void runServer(){
        
		 try {
	            System.out.println("Starting Server");
	            serverSocket = new ServerSocket(sPort); 
	            
	            while(true) {
	                System.out.println("Waiting for request");
	                try {
	                    Socket s = serverSocket.accept();
	                    File theDir = new File("client"+clientNum);

	                 // if the directory does not exist, create it
	                 if (!theDir.exists()) {
	                     System.out.println("creating directory: " + "client"+clientNum);
	                     boolean result = false;

	                     try{
	                         theDir.mkdir();
	                         result = true;
	                     } 
	                     catch(SecurityException se){
	                         //handle it
	                     }        
	                     if(result) {    
	                         System.out.println("DIR created");  
	                     }
	                 }
	                    PrintStream connectionNum = new PrintStream(s.getOutputStream());
	                    System.out.println("connected with client no " + clientNum );
	                    clientMap.put(s,clientNum);
	                    connectionNum.println(clientNum);
	          		    connectionNum.flush();
	                    executorService.execute(new Handler(s,clientNum));
	                	clientNum++;
	                    
	                } catch(IOException ioe) {
	                    System.out.println("Error accepting connection");
	                    ioe.printStackTrace();
	                }
	            }
	        }catch(IOException e) {
	            System.out.println("Error starting Server on "+sPort);
	            e.printStackTrace();
	        }
	}
	
	private void stopServer(){
		executorService.shutdownNow();
        try {
            //Stop accepting requests.
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error in server shutdown");
            e.printStackTrace();
        }
        System.exit(0);
	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler implements Runnable {
        private String message;    //message received from the client
		//private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
		private int no;		//The index number of the client
		BufferedReader in;
		
        public Handler(Socket connection, int no) {
            		this.connection = connection;
	    		this.no = no;
        }

        public void run() {
 		try{
			//initialize Input and Output streams
 			//PrintStream out = new PrintStream(connection.getOutputStream());
			//out.println(message);
			//System.out.println(" in run ");
 			
			while(true)
			{
				in = new BufferedReader( new InputStreamReader(connection.getInputStream()));
 		      	String msss = in.readLine();
 		      	System.out.println(" new request " + msss);
				String msgFromClient[] = msss.split("-");
				String command = msgFromClient[0];
				String secondArg = msgFromClient[1];
				
				//String secondArg = in.readLine();
				//System.out.println(" command " + command);
				//System.out.println(" secondArg " + secondArg);
				if(command.equalsIgnoreCase("broadcast")){
					if(!secondArg.equalsIgnoreCase("file")){
						System.out.println(" broadcast msg ");
						message = secondArg;
						//MESSAGE = message.toUpperCase();
						System.out.println(" message " + message);
						sendMessageBroadcast(message);
					}else{
						//System.out.println(" broadcast file "+ message);
						//System.out.println(" file name " + msgFromClient[2]);
						//System.out.println(" fileLen " + msgFromClient[3]);
						sendFileBroadcast(msgFromClient[2],Long.parseLong(msgFromClient[3]));
					}
				}else if(command.equalsIgnoreCase("blockcast")){
					int client = Integer.parseInt(secondArg.replaceAll("[^0-9?!\\.]",""));
						String thirdArg = msgFromClient[2];
    					
    					if(!thirdArg.equalsIgnoreCase("file")){
    						message = thirdArg;
    						//MESSAGE = message.toUpperCase();
    						System.out.println(" blockcast "+ message + " to " + client);
    						sendMessageBlockcast(message,client);
    					}else{
    						//System.out.println(" blockcast file to " + client + " with msg " + message);
    						//message = in.readLine();
    						//String msgArr[] = message.split("-");
    						//System.out.println(" file name " + msgFromClient[3]);
    						//System.out.println(" fileLen " + msgFromClient[4]);
    						sendFileBlockcast(msgFromClient[3],Long.parseLong(msgFromClient[4]),client);
    					
    					}
                    
				}else if(command.equalsIgnoreCase("unicast")){
					int client = Integer.parseInt(secondArg.replaceAll("[^0-9?!\\.]",""));

		    	    //else{
		            	String thirdArg = msgFromClient[2];
						if(!thirdArg.equalsIgnoreCase("file")){
							message = thirdArg;
							//MESSAGE = message.toUpperCase();
							System.out.println(" unicast " + message + " to " + client);
							sendMessageUnicast(message,client);
						}else{
							//System.out.println(" unicast file  to " + client+ " with msg " + message);
							//message = in.readLine();
							//String msgArr[] = message.split("-");
							//System.out.println(" file name " + msgFromClient[3]);
							//System.out.println(" fileLen " + msgFromClient[4]);
							sendFileUnicast(msgFromClient[3],Long.parseLong(msgFromClient[4]),client);
						}
		            //}
					
				}else{
					System.out.println(" command " + command);
					System.out.println(" invalid function ");
				}
				
				System.out.println(command + " excecuted successfully!!!");
			}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
			for(Socket s:clientMap.keySet()){
				if(clientMap.get(s)==no){
					clientMap.remove(s);
					System.out.println(" removed " + s + " with number " + no);
					File delFile = new File("client"+no);
					deleteDir(delFile);
				}
			}
		}
		finally{
			//Close connections
			try{
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
				for(Socket s:clientMap.keySet()){
					if(clientMap.get(s)==no){
						clientMap.remove(s);
						System.out.println(" removed " + s + " with number " + no);
						File delFile = new File("client"+no);
						deleteDir(delFile);
						
					}
				}
			}
		}
	}
    
        public void deleteDir(File file) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteDir(f);
                }
            }
            file.delete();
        }
        /******* FUNCTIONS FOR FILE TRANSFER*******/
    	
        public void sendFileBroadcast(String fileName, long fileLen) throws IOException{
        	System.out.println(" send file to everyone");
        	DataInputStream myis = new DataInputStream(connection.getInputStream());
        	//System.out.println(" obj " + myis.available());
        	
        	byte br[] = new byte[(int)fileLen];
        	//myis.readFully(br); 
        	myis.readFully(br,0,br.length); 
        	//System.out.println(br.length + " ");
        	//byte br[] = new byte[1024];
    		int count = 0;
    		long flen = fileLen;
    		//System.out.println(" written file");
    		
    		System.out.println(" file recieved at server");
    		
    		
    		for(Socket s: clientMap.keySet()){
    			if( clientMap.get(s) != no){
    			//	System.out.println("Sending from client"+no);
    				OutputStream oss = (s.getOutputStream());
    				InputStream is = s.getInputStream();
    				DataOutputStream os1 = new DataOutputStream(oss);
    				//PrintStream ps1 = new PrintStream(oss);
    				
    				String fileKeyword = "file";
    				String client = "client"+ clientMap.get(s);
    				String fromClient = "client"+no;
    				String msg = fileKeyword+"-"+client+"-"+fileName+"-"+fileLen+"-"+fromClient;
    				//ps1.println(msg);
    				//ps1.flush();
    				os1.writeUTF(msg);
    				//System.out.println(fileKeyword + "  " + fileName + "  " + fileLen);
    				os1.write(br,0,br.length);
    				os1.flush();
    				System.out.println(" file send to client broadcast client" + clientMap.get(s));
    			} 
    		}
    		
        }
                
        
        public void sendFileBlockcast(String fileName, long fileLen, int clientNo) throws IOException{
        	System.out.println(" send file to all except " + clientNo);
        	DataInputStream myis = new DataInputStream(connection.getInputStream());
        	//System.out.println(" obj " + myis.available());
        	byte br[] = new byte[(int)fileLen];
        	myis.readFully(br); 
        	//System.out.println(br.length + " ");
        	System.out.println(" file recieved at server");
    		
    		for(Socket s: clientMap.keySet()){
    			if( clientMap.get(s) != no && clientMap.get(s) != clientNo){
    				//System.out.println("Sending from client"+no);
    				OutputStream oss = (s.getOutputStream());
    				InputStream is = s.getInputStream();
    				DataOutputStream os1 = new DataOutputStream(oss);
    			//	PrintStream ps1 = new PrintStream(oss);
    				
    				String fileKeyword = "file";
    				String client = "client"+ clientMap.get(s);
    				String fromClient = "client"+no;
    				String msg = fileKeyword+"-"+client+"-"+fileName+"-"+fileLen+"-"+fromClient;
    				//ps1.println(msg);
    				//ps1.flush();
    				os1.writeUTF(msg);
    				//System.out.println(fileKeyword + "  " + fileName + "  " + fileLen);
    				os1.write(br,0,br.length);
    				os1.flush();
    				System.out.println(" file send to client blockcast client" + clientMap.get(s));
    			} 
    		}
    		
    		if(!clientMap.containsValue(clientNo)){
            	PrintStream sendResopnse = new PrintStream(connection.getOutputStream());
  		         String msg = "client"+clientNo+"does not exist can't block it";
  		         sendResopnse.println(msg);
  		         sendResopnse.flush();
            }
        }
        
         public void sendFileUnicast(String fileName, long fileLen, int clientNo) throws IOException{
        	System.out.println(" send file one");
        	DataInputStream myis = new DataInputStream(connection.getInputStream());
        	//System.out.println(" obj " + myis.available());
        	
        	byte br[] = new byte[(int)fileLen];
        	myis.readFully(br, 0, br.length); 
        	//System.out.println(br.length);
        	System.out.println(" file recieved at server");
    		
        	for(Socket s: clientMap.keySet()){
    			if( clientMap.get(s) == clientNo && clientNo != no){
    				//System.out.println("Sending from client"+no);
    				OutputStream oss = (s.getOutputStream());
    				InputStream is = s.getInputStream();
    				DataOutputStream os1 = new DataOutputStream(oss);
    				//PrintStream ps1 = new PrintStream(oss);
    				
    				String fileKeyword = "file";
    				String client = "client"+ clientMap.get(s);
    				String fromClient = "client"+no;
    				String msg = fileKeyword+"-"+client+"-"+fileName+"-"+fileLen+"-"+fromClient;
    				//ps1.println(msg);
    				//ps1.flush();
    				os1.writeUTF(msg);
    				//System.out.println(fileKeyword + "  " + fileName + "  " + fileLen);
    				os1.write(br,0,br.length);
    				os1.flush();
    				System.out.println(" file send to client unicast client" + clientMap.get(s));
    			} 
    		}
        	
        	if(!clientMap.containsValue(clientNo)){
            	PrintStream sendResopnse = new PrintStream(connection.getOutputStream());
  		         String msg = "client"+clientNo+"does not exist can't unicast";
  		         sendResopnse.println(msg);
  		         sendResopnse.flush();
            }
        		
            
        }
         
         /******FUNCTIONS FOR MESSAGE TRANSFER******/
         
        
	    public void sendMessageBroadcast(String msg)
		{
	    	System.out.println(" send message to all ");
			try{
				Iterator it = clientMap.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			       // System.out.println(pair.getKey() + " = " + pair.getValue());
			        //System.out.println(" my number " + no);
			        if((int)(pair.getValue()) != no){
			        	Socket tempS = (Socket)pair.getKey();
				        System.out.println(" sending to " + (int) (pair.getValue()));
				        //PrintStream printMsg = new PrintStream(tempS.getOutputStream());
			        	DataOutputStream dis = new DataOutputStream(tempS.getOutputStream());
				        //System.out.println(" msg is " + msg);
				        String send="@Client" + no+": "+msg;
				        //System.out.println(" msg " + send);
				        //printMsg.println(send);
						//printMsg.println(" from Client" + no);
				        dis.writeUTF(send);
				        //System.out.println(" message sent");
				        dis.flush();
						//printMsg.flush();
				  
			        }
			        
			    }
				//System.out.println("Send message: " + msg + " from Client " + no);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	
	    
	    public void sendMessageBlockcast(String msg, int num)
		{
	    	System.out.println(" send to all except " + num);
			try{
				Iterator it = clientMap.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			       // System.out.println(pair.getKey() + " = " + pair.getValue());
			        //System.out.println(" my number " + no);
			        if((int)(pair.getValue()) != num && (int)(pair.getValue()) != no ){
			        	Socket tempS = (Socket)pair.getKey();
				        System.out.println(" sending to " + (int) (pair.getValue()));
			        	//PrintStream printMsg = new PrintStream(tempS.getOutputStream());
			        	DataOutputStream dis = new DataOutputStream(tempS.getOutputStream());
				        //System.out.println(" msg is " + msg);
			        	String send="@Client" + no+": "+msg;
			        	//System.out.println(" msg " + send);
				        //printMsg.println(send);
						//printMsg.println(" from Client" + no);
				        dis.writeUTF(send);
				        System.out.println(" message sent");
				        dis.flush();
						//printMsg.flush();
				   
			        }
			        
			    }
			    
			    if(!clientMap.containsValue(num)){
	            	PrintStream sendResopnse = new PrintStream(connection.getOutputStream());
	  		         String err = "client"+num+"does not exist can't block it";
	  		         sendResopnse.println(err);
	  		         sendResopnse.flush();
	            }
				//System.out.println("Send message: " + msg + " to Client " + no);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
		
	    
	    public void sendMessageUnicast(String msg, int num)
		{
	    	System.out.println(" send  " + msg + " to "+ num);
			try{
				Iterator it = clientMap.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			       // System.out.println(pair.getKey() + " = " + pair.getValue());
			        //System.out.println(" my number " + no);
			        if((int)(pair.getValue()) == num && num != no){
			        	Socket tempS = (Socket)pair.getKey();
				        System.out.println(" sending to " + (int) (pair.getValue()));
				      //PrintStream printMsg = new PrintStream(tempS.getOutputStream());
			        	DataOutputStream dis = new DataOutputStream(tempS.getOutputStream());
				        //System.out.println(" msg is " + msg);
			        	String send="@Client" + no+": "+msg;
			        	//System.out.println(" msg " + send);
				        //printMsg.println(send);
						//printMsg.println(" from Client" + no);
				        dis.writeUTF(send);
				        System.out.println(" message sent");
				        dis.flush();
						//printMsg.flush();				   
			        }
			        
			    }
			    
			    if(!clientMap.containsValue(num)){
	            	PrintStream sendResopnse = new PrintStream(connection.getOutputStream());
	  		         String err = "client"+num+"does not exist can't unicast";
	  		         sendResopnse.println(msg);
	  		         sendResopnse.flush();
	            }
				//System.out.println("Send message: " + msg + " to Client " + no);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
    
	    //end of all methods

    } //end of server

}