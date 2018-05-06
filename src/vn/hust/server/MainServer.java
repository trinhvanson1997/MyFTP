package vn.hust.server;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MainServer {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DBConnect db = new DBConnect();
		ServerUI serverUI = new ServerUI();
		Server server = new Server(db,serverUI); 
	}
}
