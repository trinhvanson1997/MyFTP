package vn.hust.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
	public List<String> listUsername = new ArrayList<>();
	
	public Server(DBConnect db) {
		try {
			ServerSocket serverSocket = new ServerSocket(2221);
		
			System.out.println("Server is ready!");
			while(true) {
				Socket socket = serverSocket.accept();
				
				
				ClientThread thread = new ClientThread(socket, db, listUsername);
				thread.start();
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
			
}
