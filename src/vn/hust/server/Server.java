package vn.hust.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public Server() {
		try {
			ServerSocket serverSocket = new ServerSocket(2221);
		
			System.out.println("Server is ready!");
			while(true) {
				Socket socket = serverSocket.accept();
				System.out.println("connected");
				
				ClientThread thread = new ClientThread(socket);
				thread.start();
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
			
}
