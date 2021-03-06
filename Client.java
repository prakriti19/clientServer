//package clientServer;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
	Socket requestSocket;           //socket connect to the server
	String message;                //message send to the server
	int ID=-1;	
	//capitalized message read from the server

	void runClient() throws Exception
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			BufferedReader getMyID = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));
			ID = Integer.parseInt(getMyID.readLine());
			System.out.println("Connected to localhost in port 8000 with ID "+ ID);
			//creating an executor to run the sender and reciever
			ExecutorService executor = Executors.newCachedThreadPool();
			executor.execute(new Reciever());
			executor.execute(new Sender());
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//main method
	public static void main(String args[]) throws Exception
	{
		//creating a client and calling the run method
		Client client = new Client();
		client.runClient();
	}
	
	private class Sender implements Runnable{
		//One of the two threads in Client, the sender, it takes the commands, files from the sender and send it to the server for futher proessing
		BufferedReader bufferedReader;
		PrintStream printMsg;
		OutputStream os;
		
		public Sender(){
			try {
				//getting the output stream to send the data to server
				this.os =  requestSocket.getOutputStream();
				//creating the reader to read the commands from console 
				bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				//creating a printstream on top of output stream to send the data
				printMsg = new PrintStream(os);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				
				while(true)
				{
					System.out.println("Please enter command as a string: ");
					//read a sentence from the standard input
					
					message = bufferedReader.readLine();
					//System.out.println(" read msg " + message);
					message = message.trim();
					//String msgs[] = message.split("-");
					String msgs[] = message.split("\"");
					/*System.out.println(" len " + msgs.length);
					for(int i=0;i<msgs.length;i++){
						System.out.println( i + " " + msgs[i]);
					}*/
					String partOne = msgs[0];
					String content = msgs[1];
					String partThree = null;
					if(msgs.length==3){
						partThree = msgs[2];
						partThree = partThree.trim();
					}
					if(msgs.length <2){
						System.out.println(" invalid command");
						return;
					}
					
					String command = partOne.split(" ")[0];
					String typeOfMessage = partOne.split(" ")[1];
					String msg = content;
					/*various commands possible are 
					Broadcast message "my message"
					Broadcast file "filename"
					Blockcast message "my message" except to client
					Blockcast file "filename" except to client
					Unicast message "my message" to client
					Unicast file "filename" to client
					*/
					/* Filtering the commands based on the first keyword of the command and later arguments for further processing*/
					
					if(command.equalsIgnoreCase("broadcast") && typeOfMessage.equalsIgnoreCase("message")){ //BroadCast message
							String clientNo = "-1";
						//	System.out.println(" command " + command + " type " + typeOfMessage + " client no " + clientNo + " msg " + msg);
							sendMsg(command,clientNo,msg);
							System.out.println("Message sent");
					}else if(command.equalsIgnoreCase("broadcast") && typeOfMessage.equalsIgnoreCase("file")){ //Unicast and BlockCast message or file broadcast
						//System.out.println(" case 3 broadcasting file");
						String clientNum="-1";
						String FileKeyword = typeOfMessage;
						String filePath = msg;
						//System.out.println(" command " + command + " type " + typeOfMessage + " client no " + clientNum + " filePath " + filePath);
						sendFile(command, clientNum, FileKeyword, filePath);
						System.out.println("File sent");
					}else if(command.equalsIgnoreCase("unicast") && typeOfMessage.equalsIgnoreCase("message") && partThree != null ){ //Unicast and BlockCast message or file broadcast
						//System.out.println(" in case 2");
							String clientNo = partThree;
							//System.out.println(" command " + command + " type " + typeOfMessage + " client no " + clientNo + " msg " + msg);
							sendMsg(command,clientNo,msg);
							System.out.println("Message sent");
					}else if(command.equalsIgnoreCase("unicast") && typeOfMessage.equalsIgnoreCase("file") && partThree != null ){
						String FileKeyword =typeOfMessage; 
						String filePath = msg;	
						//skipping except keyword
						String clientNum = partThree;
						//System.out.println(" command " + command + " type " + typeOfMessage + " client no " + clientNum + " filePath " + msg);
						sendFile(command, clientNum, FileKeyword, filePath);
						System.out.println("File sent");
					}else if( command.equalsIgnoreCase("blockcast") && typeOfMessage.equalsIgnoreCase("message") && partThree != null){
						if(partThree.split(" ").length !=2){
							System.out.println(" Invalid command");
						}else{
							String clientNo = partThree.split(" ")[1];
							//System.out.println(" command " + command + " type " + typeOfMessage + " client no " + clientNo + " msg " + msg);
							sendMsg(command,clientNo,msg);
							System.out.println("Message sent");
						}
						
					}else if(command.equalsIgnoreCase("blockcast") && typeOfMessage.equalsIgnoreCase("file")){
						if(partThree.split(" ").length !=2){
							System.out.println(" Invalid command");
						}else{
							String FileKeyword = typeOfMessage; 
							String filePath = msg;	
							//skipping except keyword
							String clientNum = partThree.split(" ")[1];
							//System.out.println(" command " + command + " type " + typeOfMessage + " client no " + clientNum + " filePath " + filePath);
							sendFile(command, clientNum, FileKeyword, filePath);
							System.out.println("File sent");
						}
						
					}else{
						System.out.println(" Invalid command");
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(" error ");
				e.printStackTrace();
			} //send 
			
		}
		
		//Calling the sendMsg function to send the type message as per the command from the client to server
		
		public void sendMsg(String command, String clientNo, String msg){
			
			//printMsg.println(command); //sending 3 params to server blockcast-client3-client3 blocked
			if(clientNo != "-1"){
				String send = command+"-"+clientNo+"-"+msg;
				printMsg.println(send);
			}else{
				String send = command+"-"+msg;
				printMsg.println(send);
				
			}
			printMsg.flush();

		}
		
		//calling sendFile in case of a file transfer
		
		public void sendFile(String command, String clientNum,String FileKeyword, String filePath) throws IOException{
			DataOutputStream dos = new DataOutputStream(os); //created dos for file transfer
			
			File file = new File("client"+ID+"\\"+filePath);
			if(!file.exists()){
				System.out.println(" file doesnt exist");
				return;
			}
			InputStream ips = new FileInputStream(file);
			
			long fileLen = file.length();
			String fileName = file.getName();
			if(command.equalsIgnoreCase("broadcast")){
				String msg = command+"-file-"+fileName+"-"+fileLen;
				//System.out.println("message " + msg);
				printMsg.println(msg);
			}else{
				String msg = command+"-"+clientNum+"-file-"+fileName+"-"+fileLen;
				//System.out.println(" message " + msg);
				printMsg.println(msg);
			}
			printMsg.flush();
			byte br[] = new byte[(int)fileLen];
			int count = ips.read(br, 0, br.length);
			//System.out.println("file size " + count + " length " + fileLen);
			dos.write(br);
			dos.flush(); //flushing the dos
			ips.close();
			//System.out.println(" file sent ");
		}
		
	}

	private class Reciever implements Runnable{
		//Reciever thread of the client which is constantly listening for the messages and files recieved by the client
		
		InputStreamReader ir;
		BufferedReader in;
		InputStream is;
		String messageRecv;
		
		public Reciever(){
			try{
				//getting the input stream to fetch the data from server
				is = requestSocket.getInputStream();
			}
			catch(IOException e){
				e.getMessage();
			}
		}
		@Override
		public void run() {
				// TODO Auto-generated method stub
				try {
					//using the data stream to fetch the data or the message based on the keyword recieved
					DataInputStream dis = new DataInputStream(is);
					//InputStreamReader myis = new InputStreamReader(socIS);
					while(true)
					{
						//receive the message sent from the server
						//messageRecv = in.readLine();
						messageRecv = dis.readUTF();
			//			System.out.println(" msg recieved " + messageRecv);
						if(messageRecv==null){
							System.out.println(" null message from server");
							return;
						}
						//show the message to the user
						if(messageRecv != null){
						//Case where the message contains a file
						if(messageRecv.startsWith("file")){
						//	System.out.println(" message recieved " + messageRecv );
							String msgsArr[] = messageRecv.split("-");
							if(msgsArr.length != 5){
								System.out.println(" invalid msg " + messageRecv);
								
							}
							String client = msgsArr[1];//in.readLine(); //getting the client Number
							String fileName = msgsArr[2];//in.readLine(); //fileName
							long fileLen = Long.parseLong(msgsArr[3]);//(in.readLine());
							String fromClient = msgsArr[4]; 
							//System.out.println( msgsArr[0] + " "+client + " " + fileName + " " + fileLen);
							File file = new File(client + "\\"+ fileName);
							if(!file.exists()){
								file.getParentFile().mkdirs();
								file.createNewFile();
							}
							FileOutputStream fos = new FileOutputStream(file);
							
							byte br[] = new byte[(int)fileLen];
							int count = 0;
							long flen = fileLen;
							//System.out.println(dis.available());
							dis.readFully(br);
							fos.write(br);
							fos.flush();
							fos.close();
							System.out.println("Received file: " + fileName + " from "+fromClient);
						}else{
							//no file just output the message
							System.out.println("Received message: " + messageRecv );
						}
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //recieve
			
		}
		
	}
} 
