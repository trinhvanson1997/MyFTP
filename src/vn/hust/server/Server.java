package vn.hust.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
	public List<String> listUsername = new ArrayList<>();
	public ServerUI serverUI;
	public static final int MAX_THREADS = 3;
	public int curNumThreads = 0;
	
	public Server(DBConnect db,ServerUI serverUI) {
		try {
			ServerSocket serverSocket = new ServerSocket(21);
		
			System.out.println("Server is ready!");
			serverUI.getTextArea().append("Waiting for connection...\n");
			while(true) {
				Socket socket = serverSocket.accept();
				System.out.println("Connected to client port: "+ socket.getPort());
				serverUI.getTextArea().append("Established connection to client "+socket.getRemoteSocketAddress() +"\n");
				
				ClientThread thread = new ClientThread(socket, db, listUsername,serverUI,this);
				thread.start();
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
			
}
