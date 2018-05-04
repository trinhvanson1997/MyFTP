package vn.hust.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class Client {
	public static final int LOGIN = 1, GET_FILE = 2, GET_LIST_FILES = 3, CLOSE = 7, REGISTER = 8,CHECK_DIRECTORY = 9,CHECK_FILE=13, GET_NAME = 14;
	public DataInputStream in;
	public DataOutputStream out;
	public ObjectInputStream ois;
	public ObjectOutputStream oos;
	public Socket socket;

	public Client(String host, int port) {
		try {
			socket = new Socket(host, port);

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Error 404");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Cannot connect to server");
			e.printStackTrace();
		}

	}

	public String login(String username, String password) {
		try {
			out.writeInt(LOGIN);
			out.flush();

			out.writeUTF(username);
			out.writeUTF(password);

			String rs = in.readUTF();
			return rs;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "incorrect";
	}

	public boolean register(String username, String password) {
		try {
			out.writeInt(REGISTER);
			out.flush();

			out.writeUTF(username);
			out.flush();

			out.writeUTF(password);
			out.flush();
			
			boolean rs = in.readBoolean();
		
			return rs;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

		public boolean checkDir(String path) {
			try {
				out.writeInt(CHECK_DIRECTORY);
				out.flush();
				
				out.writeUTF(path);
				out.flush();
				boolean rs = in.readBoolean();

				return rs;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		public boolean checkFile(String path) {
			try {
				out.writeInt(CHECK_FILE);
				out.flush();
				
				out.writeUTF(path);
				out.flush();
				boolean rs = in.readBoolean();

				return rs;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
	public File getFile(String curPath) {
		try {
			out.writeInt(GET_FILE);
			out.flush();

			out.writeUTF(curPath);
			out.flush();

			File file = (File) ois.readObject();

			return file;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}
	public String getName(String curPath) {
		try {
			out.writeInt(GET_NAME);
			out.flush();

			out.writeUTF(curPath);
			out.flush();

		String name = in.readUTF();

			return name;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}
	public File[] listFiles(String curPath) {
		try {
			out.writeInt(GET_LIST_FILES);
			out.flush();

			out.writeUTF(curPath);
			out.flush();

			int numberFile = in.readInt();

			File[] files = new File[numberFile];

			for (int i = 0; i < numberFile; i++) {
				files[i] = (File) ois.readObject();

			}
			return files;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	public void sendCloseRequest() {
		try {
			out.writeInt(CLOSE);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
