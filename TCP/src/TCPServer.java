/*
Server file to receive and send file to client connected
Server file manually passed in string server_dir
PORT Number assigned is 5901

Author: Deepen Dawadi
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer {
	public static void main(String args[]) throws Exception {

 			String server_dir = "C:\\Users\\dawadi\\Desktop\\FTP\\Server_files\\";
			System.out.println("Server is Running...");
			System.out.println("Waiting for connections...");
         int thread_id = 1;
			ServerSocket socket;

				socket = new ServerSocket(5901);
            
            while (true) {
				Socket connectionSocket = socket.accept(); //server waits until the connection is accepted
            System.out.println("Congratulation!!! Connection has Established!");
            System.out.println("-------------------------------------------------------");
				System.out.println("Client Information: ");
            System.out.println("IP Address: "+connectionSocket.getInetAddress().getHostName());
            System.out.println("-------------------------------------------------------");
           	Thread server = new ThreadedServer(connectionSocket, thread_id, server_dir);
				thread_id++;
				server.start();
            
			}
		}
      
	}

class ThreadedServer extends Thread {
	int n, m;
	String name, f, ch, fileData;
	String filename;
	Socket connectionSocket;
	int counter;
	String dirName;

	public ThreadedServer(Socket s, int c, String dir) {
		connectionSocket = s;
		counter = c;
		dirName = dir;  // set directory name
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

         InputStream inFromClient = connectionSocket.getInputStream();
			OutputStream output = connectionSocket.getOutputStream();
			//while(true){
			ObjectOutputStream oout = new ObjectOutputStream(output);
			oout.writeObject("Server connection done"); //send text to client
         
   		File ff = new File(dirName);
			ArrayList<String> names = new ArrayList<String>(Arrays.asList(ff.list()));
         
         //send no of files in the server
			oout.writeObject(String.valueOf(names.size()));
         
         //sends the list of file name to client
			for(String name: names) {
				oout.writeObject(name);
			}
         
         //read from client which file is to be downloaded "pw.println(file);"
		   name = in.readLine();
         ch = name.substring(0, 1);
         
         //SENDING FILE TO CLIENT
			if (ch.equals("*")) {
				n = name.lastIndexOf("*"); //index of last character of filename
            
				filename = name.substring(1, n); //extracts the exact filename
				FileInputStream file = null;
				BufferedInputStream bis = null;
				boolean fileExists = true;
				System.out.println("Request to download file " + filename + " recieved from " + connectionSocket.getInetAddress().getHostName() + "...");
				filename = dirName + filename; //add the server directory to filename
				//System.out.println(filename);
				
            try {
					file = new FileInputStream(filename);
					bis = new BufferedInputStream(file);
				} //catch if file doesnot exists
				catch (FileNotFoundException excep) {
					fileExists = false;
					System.out.println("FileNotFoundException:" + excep.getMessage());
				}
            
				if (fileExists) {
					
					//output - outputstream  oout--objectoutputstream
					oout = new ObjectOutputStream(output);  
					oout.writeObject("Success"); //send signal of success to client
					
					System.out.println("Download begins...");
					sendBytes(bis, output);
					System.out.println("Completed");
					bis.close();
					file.close();
					oout.close();
					output.close();
				}
				else {
					//oout = new ObjectOutputStream(output);
					//oout.writeObject("FileNotFound");
					bis.close();
					file.close();
					oout.close();
				   output.close();
				}
			} 
			else{ //UPLOADING THE FILE FROM CLIENT
				try {
					boolean complete = true;
					System.out.println("Request to upload file " + name + " recieved from " + connectionSocket.getInetAddress().getHostName() + "...");
 					File directory = new File(dirName);
					int size = 20857600;
					byte[] data = new byte[size];
               
               //creating new file with the same filename
					File fc = new File(directory, name);
					FileOutputStream fileOut = new FileOutputStream(fc);
					DataOutputStream dataOut = new DataOutputStream(fileOut);

					while (complete) {
                  // read bytes from stream, and store them in buffer
						m = inFromClient.read(data, 0, data.length);
						if (m == -1) {     //read() will return -1 if the end of stream has been reached
							complete = false;
							System.out.println("Completed");
						} else {
                  // Writes bytes from byte array (buffer) into output stream.
							dataOut.write(data, 0, m);
							dataOut.flush(); //Flushes the output stream and forces any buffered output bytes to be written out.
						}
					}
					fileOut.close();  //close the fileOutputStream
             
				} catch (Exception exc) {
					System.out.println(exc.getMessage());
               
				}
			}
        //}
		} 
		catch (Exception ex) {
			System.out.println(ex.getMessage());
         System.out.println(ex);
		}
     
	}

   //method to send bytes to client
	private static void sendBytes(BufferedInputStream in , OutputStream out) throws Exception {
		int size = 20857600; //for almost 20.8 MB file
		byte[] data = new byte[size];
		int c = in.read(data, 0, data.length);  // read bytes from stream, and store them in buffer
		out.write(data, 0, c);  // Writes bytes from data array (buffer) into output stream.
		out.flush(); //Flushes the output stream and forces any buffered output bytes to be written out.
	}
}
