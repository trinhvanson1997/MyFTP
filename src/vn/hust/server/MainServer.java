package vn.hust.server;

public class MainServer {
	public static void main(String[] args) {
		DBConnect db = new DBConnect();
		Server server = new Server(db); 
	}
}
