package vn.hust.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
	public List<String> listUsername = new ArrayList<>();
	public ServerUI serverUI;
	public Server(DBConnect db,ServerUI serverUI) {
		try {
			ServerSocket serverSocket = new ServerSocket(21);
		
			System.out.println("Server is ready!");
			serverUI.getTextArea().append("Waiting for connection...\n");
			while(true) {
				Socket socket = serverSocket.accept();
				serverUI.getTextArea().append("Established connection to client "+socket.getRemoteSocketAddress() +"\n");
				
				ClientThread thread = new ClientThread(socket, db, listUsername,serverUI);
				thread.start();
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
			
}
