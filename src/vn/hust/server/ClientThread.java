package vn.hust.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
	public static final int LOGIN = 1, GET_FILE = 2, GET_LIST_FILES = 3, UPLOAD = 4, CANCEL_UPLOAD = 5,
			CONTINUE_UPLOAD = 6, CLOSE = 7, REGISTER = 8, CHECK_DIRECTORY = 9, DOWNLOAD = 10, CANCEL_DOWNLOAD = 11,
			CONTINUE_DOWNLOAD = 12, CHECK_FILE = 13, GET_NAME = 14;
	public String username;
	public String homeDir = "C:\\Users\\sontrinh\\Desktop\\FTP\\";
	// public String homeServer = "C:\\Users\\sontrinh\\Desktop\\FTP\\";
	public String curPath;

	public Socket socket;
	public DataInputStream in;
	public DataOutputStream out;
	public ObjectOutputStream oos;
	public ObjectInputStream ois;

	public ClientThread(Socket socket) {
		this.socket = socket;

		try {
			in = new DataInputStream(this.socket.getInputStream());
			out = new DataOutputStream(this.socket.getOutputStream());
			oos = new ObjectOutputStream(this.socket.getOutputStream());
			ois = new ObjectInputStream(this.socket.getInputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		boolean openThread = true;
		while (openThread) {
			try {
				int request = in.readInt();

				if (request == LOGIN) {
					String username = in.readUTF();
					String password = in.readUTF();
					if ((username.equals("son") && password.equals("son"))
							|| username.equals("hien") && password.equals("hien")
							|| username.equals("duy") && password.equals("duy")
							|| username.equals("phuc") && password.equals("phuc")) {
						this.username = username;
						this.homeDir += this.username;

						out.writeBoolean(true);
					} else
						out.writeBoolean(false);
				} else if (request == REGISTER) {
					String username = in.readUTF();
					String password = in.readUTF();

					String path = this.homeDir + username;
					File file = new File(path);
					file.mkdirs();
				} else if (request == CHECK_DIRECTORY) {

					String path = in.readUTF();

					File file = new File(path);

					if (file.isDirectory()) {
						out.writeBoolean(true);
						out.flush();
					} else {
						out.writeBoolean(false);
						out.flush();
					}
				} else if (request == CHECK_FILE) {

					String path = in.readUTF();

					File file = new File(path);

					if (file.isFile()) {
						out.writeBoolean(true);
						out.flush();
					} else {
						out.writeBoolean(false);
						out.flush();
					}
				} else if (request == GET_FILE) {
					String path = in.readUTF();

					path = path.replace('/', '\\');
					path = homeDir + path;
					System.out.println("Get file path: " + path);
					File file = new File(path);

					oos.writeObject(file);
					oos.flush();
				} else if (request == GET_NAME) {
					String path = in.readUTF();

					path = path.replace('/', '\\');
					path = homeDir + path;

					File file = new File(path);

					out.writeUTF(file.getName());
					out.flush();
				} else if (request == GET_LIST_FILES) {

					String path = in.readUTF();
					path = path.replace("/", "\\");
					path = homeDir + path;
					System.out.println("Get list  file path: " + path);
					File file = new File(path);
					File[] files = file.listFiles();

					out.writeInt(files.length);
					out.flush();

					for (File file2 : files) {
						oos.writeObject(file2);
						oos.flush();
					}

				} else if (request == UPLOAD) {
					System.out.println("-------UPLOADING---------");
					String remotePath = in.readUTF();
					remotePath = this.homeDir.replace('\\', '/') + remotePath;

					int numberFile = in.readInt();
					int resume = CONTINUE_UPLOAD;
					int curNumber = 0; // lưu file thứ i đang được upload
					for (int i = 1; i <= numberFile; i++) {

						System.out.println("recieving part " + i);
						OutputStream os2 = new BufferedOutputStream(new FileOutputStream(remotePath + "-part" + i));

						int num = in.readInt();

						for (int j = 0; j < num; j++) {
							resume = in.readInt();

							if (resume == CONTINUE_UPLOAD) {

								int read = in.readInt();

								byte[] bytes = new byte[read];
								in.readFully(bytes, 0, read);
								os2.write(bytes, 0, read);
							} else if (resume == CANCEL_UPLOAD) {
								// break out of 2 loops
								System.out.println("Cancel recieve file");
								curNumber = i;
								i = numberFile + 1;
								j = num;
								break;
							}

						}
						os2.close();

					}
					if (resume == CONTINUE_UPLOAD) {
						joinFile(remotePath, numberFile);
					} else if (resume == CANCEL_UPLOAD) {
						for (int t = 1; t <= curNumber; t++) {
							File file = new File(remotePath + "-part" + t);
							file.delete();
						}
					}

					out.writeUTF("complete");
					out.flush();
				} else if (request == DOWNLOAD) {
					System.out.println("-------DOWNLOADING---------");
					String remote = in.readUTF();
					String path = this.homeDir.replace('\\', '/') + remote;

					int numberFile = 4;
					out.writeInt(numberFile);
					out.flush();

					File remoteFile = new File(path);
					long len = remoteFile.length();
					out.writeLong(len);
					out.flush();

					long sizeFile = len / numberFile;

					InputStream is = new FileInputStream(remoteFile);
					System.out.println("Starting download");
					int resume = CONTINUE_DOWNLOAD;
					int read = -1;
					long count = 0, size, limit;
					int num;
					byte[] bytes = new byte[4096];

					for (int i = 1; i <= numberFile; i++) {
						System.out.println("Sending part " + i);
						if (i < numberFile) {
							size = sizeFile;
							limit = size * i;
						} else {
							size = len - (sizeFile * 3);
							limit = len;
						}

						if ((int) (size % 4096) == 0) {
							num = (int) (size / 4096);
						} else {
							num = (int) (size / 4096) + 1;
						}
						out.writeInt(num);
						out.flush();

						for (int j = 0; j < num; j++) {
							resume = in.readInt();
							if (resume == CONTINUE_DOWNLOAD) {

								if ((limit - count) < 4096)
									bytes = new byte[(int) (limit - count)];
								else
									bytes = new byte[4096];

								read = is.read(bytes);

								count += read;
								out.writeLong(count);
								out.flush();

								// send size of arr
								out.writeInt(read);
								out.flush();

								// send arr
								out.write(bytes);
								out.flush();
							} else if (resume == CANCEL_DOWNLOAD) {
								i = numberFile + 1;
								j = num;
								break;
							}
						}
					}
					is.close();
					out.writeUTF("complete");
					out.flush();
					
				} else if (request == CLOSE) {
					System.out.println("Recieve close request from client");
					System.out.println("Closing this thread......");
					socket.close();
					openThread = false;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void joinFile(String pathname, int numberFile) {
		try {
			OutputStream outputStream = new FileOutputStream(new File(pathname));
			InputStream inputStream;

			for (int i = 1; i <= numberFile; i++) {
				File file = new File(pathname + "-part" + i);
				inputStream = new FileInputStream(file);

				byte[] bytes = new byte[4096];
				int read = -1;
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				inputStream.close();
				file.delete();
			}
			outputStream.close();
			System.out.println("Joined File");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
