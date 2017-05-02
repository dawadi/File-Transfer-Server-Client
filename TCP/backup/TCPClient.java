import java.io.*;
import java.net.*;
//import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Scanner;

class TCPClient extends JFrame implements ActionListener, MouseListener {
	JPanel panel;
	JLabel title, subT, msg, error, servFiles, clientFiles;
	Font font,labelfont;
	JTextField txt;
	JButton up, down;
	String dirName;
	Socket clientSocket;
	InputStream inFromServer;
	OutputStream outToServer;
	BufferedInputStream bis;
	PrintWriter pw;
	String name, file, path;
	String hostAddr;
	int portNumber;
	int c;
	int size = 20857600; 	//for almost 20.8 MB file
	JList<String> filelist, c_filelist;
	String[] names = new String[10000];
	String[] c_names = new String[10000];
	int len, c_len; // number of files on the server retrieved

	public TCPClient(String dir, String host, int port) {
		
		dirName = dir; 		// set dirName to the one that's entered by the user
		hostAddr = host;	// set hostAddr to the one that's passed by the user
		portNumber = port;	// set portNumber to the one that's passed by the user

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel(null);

		font = new Font("Chalkboard", Font.BOLD, 30);
		title = new JLabel("CLIENT");
		title.setFont(font);
		title.setBounds(50, 20, 400, 50);
		panel.add(title);

		labelfont = new Font("Roboto", Font.PLAIN, 15);
		subT = new JLabel("File Name :");
		subT.setFont(labelfont);
		subT.setBounds(50, 320, 200, 50);
		panel.add(subT);

		txt = new JTextField();
		txt.setBounds(150, 330, 250, 30);
		panel.add(txt);

		up = new JButton("Upload");
		up.setBounds(50, 400, 100, 30);
		panel.add(up);

		down = new JButton("Download");
		down.setBounds(200, 400, 100, 30);
		panel.add(down);

		error = new JLabel("");
		error.setFont(labelfont);
		error.setBounds(50, 450, 600, 50);
		panel.add(error);

		up.addActionListener(this);
		down.addActionListener(this);
      
      
		try {
			clientSocket = new Socket(hostAddr, portNumber); //socket creation
			inFromServer = clientSocket.getInputStream();	 //inputstream - read byte from stream
			pw = new PrintWriter(clientSocket.getOutputStream(), true);
			outToServer = clientSocket.getOutputStream();
			ObjectInputStream oin = new ObjectInputStream(inFromServer);
			String s = (String) oin.readObject();
			System.out.println(s);

			len = Integer.parseInt((String) oin.readObject());
			System.out.println("Total Number of files in Server: "+len);

			String[] temp_names = new String[len];

			for(int i = 0; i < len; i++) {
				String filename = (String) oin.readObject();
				System.out.println(filename);
				names[i] = filename;
				temp_names[i] = filename;
			}

			// sort the array of strings that's going to get displayed in the scrollpane
			Arrays.sort(temp_names);

			servFiles = new JLabel("Files in the Server Directory :");
			servFiles.setBounds(50, 65, 400, 50);
			panel.add(servFiles);

			filelist = new JList<>(temp_names);
			JScrollPane scroll = new JScrollPane(filelist);
			scroll.setBounds(50, 100, 400, 200);

			panel.add(scroll);
			filelist.addMouseListener(this);

			
			File ff = new File(dirName);
			ArrayList<String> names = new ArrayList<String>(Arrays.asList(ff.list()));
			c_len = names.size();
			
			String[] temp_client_files = new String[c_len];
			temp_client_files = names.toArray(temp_client_files);
			System.out.println("Client files: ");
			int j=0;
			for(String client_files: temp_client_files) {
				c_names[j]=client_files;
				System.out.println(client_files);
				j++;
			}
			
			Arrays.sort(temp_client_files);
			
						
			clientFiles = new JLabel("Files in the Client Directory :");
			clientFiles.setBounds(500, 65, 400, 50);
			panel.add(clientFiles);

			c_filelist = new JList<>(temp_client_files);
			JScrollPane scroll1 = new JScrollPane(c_filelist);
			scroll1.setBounds(500, 100, 400, 200);

			panel.add(scroll1);
			c_filelist.addMouseListener(this);
			
			
			
		} 
		catch (Exception exc) {
			System.out.println("Exception: " + exc.getMessage());
			error.setText("Exception:" + exc.getMessage());
			//error.setBounds(50,450,600,50);
			panel.revalidate();
		}

		getContentPane().add(panel);
	  
   }

    public void mouseClicked(MouseEvent click) {
        if (click.getClickCount() == 2) {
           String selectedItem_server = (String) filelist.getSelectedValue();
           txt.setText(selectedItem_server);
           panel.revalidate();
           
         } else if(click.getClickCount() == 1)
        	 
         {
        	 String selectedItem_client = (String) c_filelist.getSelectedValue();
             txt.setText(selectedItem_client);
             panel.revalidate();         	 
         }
    }

    public void mousePressed(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

//action when upload/download button is pressed
   public void actionPerformed(ActionEvent event) {
		//for upload
      if (event.getSource() == up) {
			try {
				name = txt.getText();

				FileInputStream file = null;
				BufferedInputStream bis = null;

				boolean fileExists = true;
				path = dirName + name;

				try {
					file = new FileInputStream(path);
					bis = new BufferedInputStream(file);
				} catch (FileNotFoundException excep) {
					fileExists = false;
					System.out.println("FileNotFoundException:" + excep.getMessage());
					error.setText("FileNotFoundException:" + excep.getMessage());
					panel.revalidate();
				}

				if (fileExists) {
					// send file name to server for upload
					pw.println(name);

					System.out.println("Upload begins");
					error.setText("Upload begins");
					panel.revalidate();

					// send file data to server
					sendBytes(bis, outToServer);
					System.out.println("Completed");
					error.setText("Completed");
					panel.revalidate();

					boolean exists = false;
					for(int i = 0; i < len; i++){
						if(names[i].equals(name)){
							exists = true;
							break;
						}
					}

					if(!exists){
						names[len] = name;
						len++;
					}

					String[] temp_names = new String[len];
					for(int i = 0; i < len; i++){
						temp_names[i] = names[i];
					}

					// sort the array of strings that's going to get displayed in the scrollpane
					Arrays.sort(temp_names);

					// update the contents of the list in scroll pane
					filelist.setListData(temp_names);

					// close all file buffers
					bis.close();
					file.close();
					outToServer.close();
				}
			} 
			catch (Exception exc) {
				System.out.println("Exception: " + exc.getMessage());
				error.setText("Exception:" + exc.getMessage());
				panel.revalidate();
			}
		}
      	//*******************************************************************
      	//*******************************************************************
      	//if download button is pressed
		else if (event.getSource() == down) {
			try {
				boolean complete = true;
				byte[] data = new byte[size];
				name = txt.getText();	//gets name from textbox in UI
            
				//add * to the filename to be downloaded
				file = new String("*" + name + "*");
				pw.println(file); //send the server which file is to be downloaded

				//to catch the server message
				ObjectInputStream oin = new ObjectInputStream(inFromServer);
				String s = (String) oin.readObject();
            
				//requested file found and ready for download i.e. s contains "Success" sent from server
				if(s.equals("Success")) {
				
				//creates new file with the same filename as in server in the specified directory
                File f = new File(dirName, name);
					FileOutputStream fileOut = new FileOutputStream(f);
					DataOutputStream dataOut = new DataOutputStream(fileOut);

					
					//run until end of file is detected
					while (complete) {
						c = inFromServer.read(data, 0, data.length); //read() will return -1 when the InputStream is depleted
						if (c == -1) {
							complete = false;
							System.out.println("Completed");
							error.setText("Completed");
							panel.revalidate();

						} else {
							dataOut.write(data, 0, c);
							dataOut.flush();
						}
					}
					
					
					boolean exists = false;
					for(int i = 0; i < c_len; i++){
						//System.out.println("Checking: "+c_names[i]);
						if(c_names[i].equals(name)){
							exists = true;
							break;
						}
					}

					if(!exists){
						c_names[c_len] = name;
						c_len++;
					}

					String[] temp_client_files = new String[c_len];
					for(int i = 0; i < c_len; i++){
						temp_client_files[i] = c_names[i];
					}

					// sort the array of strings that's going to get displayed in the scrollpane
					Arrays.sort(temp_client_files);

					// update the contents of the list in scroll pane
					c_filelist.setListData(temp_client_files);					
					
					
					fileOut.close();
				}
				else {
					System.out.println("Requested file not found on the server.");
					error.setText("Requested file not found on the server.");
					panel.revalidate();
				}
			} 
			catch (Exception exc) {
				System.out.println("Exception: " + exc.getMessage()+ "  "+ exc);
				error.setText("Exception:" + exc.getMessage());
				panel.revalidate();
			}
		}
	} //end of file download and upload
  
   

	private static void sendBytes(BufferedInputStream in , OutputStream out) throws Exception {
		int size = 20857600; //for almost 20.8 MB file
		byte[] data = new byte[size];
		int c = in.read(data, 0, data.length);
		out.write(data, 0, c);
		out.flush();
	}

	public static void main(String args[]) {


		 String filenamepath="C:\\Users\\dawadi\\Desktop\\FTP\\Client_files\\";
         //TCPClient tcp = new TCPClient(filenamepath, "uxb4.wiu.edu", 5901);
         TCPClient tcp = new TCPClient(filenamepath, "localhost", 5901);
         tcp.setSize(1000, 600);
			tcp.setVisible(true);

	}
   
}
