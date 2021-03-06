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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

public class ClientThread extends Thread {
	public static final int LOGIN = 1, GET_FILE = 2, GET_LIST_FILES = 3, UPLOAD = 4, CANCEL_UPLOAD = 5,
			CONTINUE_UPLOAD = 6, CLOSE = 7, REGISTER = 8, CHECK_DIRECTORY = 9, DOWNLOAD = 10, CANCEL_DOWNLOAD = 11,
			CONTINUE_DOWNLOAD = 12, CHECK_FILE = 13, GET_NAME = 14, UPLOAD_FOLDER = 15, GET_SIZE = 16,
			GET_LASTMODIFIED = 17, CHECK_FILE_BY_PATH = 18, DOWNLOAD_FOLDER = 19, MAKE_DIR = 20, DELETE = 21,
			RENAME = 22;
	public String username;
	public String homeDir = "C:\\Users\\sontrinh\\Desktop\\FTP\\";

	public String curPath;

	public Socket socket;
	public DataInputStream in;
	public DataOutputStream out;
	public ObjectOutputStream oos;
	public ObjectInputStream ois;

	public DBConnect db;
	public List<String> listUsername; // danh sách các user đang đăng nhập
	public ServerUI serverUI;

	// các biến nén nén, giải nén folder
	public String srcZip;
	public String nameFileZip;
	String des = "";
	public List<String> fileList = new ArrayList<>();

	public int request; // yêu cầu từ client
	public String filePath;
	public int curNumber; // mảnh hiện tại

	
	public Server server;
	public ClientThread(Socket socket, DBConnect db, List<String> listUsername, ServerUI serverUI,Server server) {
		this.socket = socket;
		this.db = db;
		this.listUsername = listUsername;
		this.serverUI = serverUI;
		this.server = server;

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
				request = in.readInt();

				if (request == LOGIN) {
					String username = in.readUTF();
					String password = in.readUTF();
					if (server.curNumThreads == server.MAX_THREADS) {
						out.writeUTF("full");

					} else {
						if (listUsername.contains(username)) {
							out.writeUTF("online");
						} else {
							if (db.login(username, password)) {
								server.curNumThreads++;
								this.username = username;
								// Lưu đường dẫn đến thư mục trên server ứng với tài khoản đăng nhập
								this.homeDir += this.username;

								// Lưu tài khoản vào danh sách đang online
								this.listUsername.add(username);
								this.serverUI.getTextArea().append(getDateNow() + " : Logged in successfully\n");
								out.writeUTF("correct");
							} else
								out.writeUTF("incorrect");
						}

					}
					out.flush();
				} else if (request == REGISTER) {
			
					String username = in.readUTF();
					String password = in.readUTF();
					server.curNumThreads++;
					
					this.serverUI.getTextArea().append(getDateNow() + " : Registered \n");
					if (db.register(username, password)) {
						String path = this.homeDir + username;
						File file = new File(path);
						file.mkdirs();

						out.writeBoolean(true);

					} else
						out.writeBoolean(false);
					out.flush();

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

					path = this.homeDir.replace('\\', '/') + path;
					File file = new File(path);

					if (file.isFile()) {
						out.writeBoolean(true);
						out.flush();
					} else {
						out.writeBoolean(false);
						out.flush();
					}

				} else if (request == CHECK_FILE_BY_PATH) {

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

					System.out.println("GET FILE: " + path);
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
				} else if (request == GET_SIZE) {
					String path = in.readUTF();
					File file = new File(path);

					out.writeLong(file.length());
					out.flush();
				} else if (request == GET_LASTMODIFIED) {
					String path = in.readUTF();
					File file = new File(path);

					out.writeLong(file.lastModified());
					out.flush();
				} else if (request == GET_LIST_FILES) {

					String path = in.readUTF();
					path = path.replace("/", "\\");
					path = homeDir + path;
					this.serverUI.getTextArea().append(getDateNow() + " : List directory " + path + "\n");
					File file = new File(path);
					File[] files = file.listFiles();

					out.writeInt(files.length);
					out.flush();

					for (int i = 0; i < files.length; i++) {
						oos.writeObject(files[i]);
						oos.flush();
					}

				} else if (request == UPLOAD) {
					System.out.println("-------UPLOADING---------");

					String remotePath = in.readUTF();
					remotePath = this.homeDir.replace('\\', '/') + remotePath;
					filePath = remotePath;

					this.serverUI.getTextArea()
							.append(getDateNow() + " : Recieving file " + remotePath.replace('/', '\\') + "\n");
					int numberFile = in.readInt();
					int resume = CONTINUE_UPLOAD;

					curNumber = 0; // lưu file thứ i đang được upload
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
								this.serverUI.getTextArea().append(getDateNow() + " : Cancel recieving  file\n");
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
						out.writeUTF("complete");
						out.flush();
						this.serverUI.getTextArea().append(getDateNow() + " : Recieved file successfully\n");
					} else if (resume == CANCEL_UPLOAD) {
						for (int t = 1; t <= curNumber; t++) {
							File file = new File(remotePath + "-part" + t);
							file.delete();
						}
						out.writeUTF("cancel");
						out.flush();
					}

				} else if (request == UPLOAD_FOLDER) {
					System.out.println("-------UPLOADING---------");

					String remotePath = in.readUTF();
					remotePath = this.homeDir.replace('\\', '/') + remotePath;
					this.serverUI.getTextArea()
							.append(getDateNow() + " : Recieving file " + remotePath.replace('/', '\\') + "\n");
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
								this.serverUI.getTextArea().append(getDateNow() + " : Cancel recieving  file\n");
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
						this.serverUI.getTextArea().append(getDateNow() + " : Recieved file successfully\n");

						unZip(remotePath, remotePath.substring(0, remotePath.lastIndexOf('.')));

					} else if (resume == CANCEL_UPLOAD) {
						for (int t = 1; t <= curNumber; t++) {
							File file = new File(remotePath + "-part" + t);
							file.delete();

						}

					}
					File f = new File(remotePath);
					f.delete();
					out.writeUTF("complete");
					out.flush();
				} else if (request == DOWNLOAD) {
					System.out.println("-------DOWNLOADING---------");

					String remote = in.readUTF();
					String path = this.homeDir.replace('\\', '/') + remote;
					this.serverUI.getTextArea()
							.append(getDateNow() + " : Sending file " + path.replace('/', '\\') + "\n");
					int numberFile1 = 4;
					out.writeInt(numberFile1);
					out.flush();

					File remoteFile = new File(path);
					long len = remoteFile.length();
					out.writeLong(len);
					out.flush();

					long sizeFile = len / numberFile1;

					InputStream is = new FileInputStream(remoteFile);
					System.out.println("Starting download");
					int resume1 = CONTINUE_DOWNLOAD;
					int read = -1;
					long count = 0, size, limit;
					int num;
					byte[] bytes = new byte[4096];

					for (int i = 1; i <= numberFile1; i++) {
						System.out.println("Sending part " + i);
						if (i < numberFile1) {
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
							resume1 = in.readInt();
							if (resume1 == CONTINUE_DOWNLOAD) {

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
							} else if (resume1 == CANCEL_DOWNLOAD) {
								this.serverUI.getTextArea().append(getDateNow() + " : Cancel downloading file ");
								i = numberFile1 + 1;
								j = num;
								break;
							}
						}
					}
					is.close();
					out.writeUTF("complete");
					out.flush();

				} else if (request == DOWNLOAD_FOLDER) {
					System.out.println("-------DOWNLOADING---------");

					String remote = in.readUTF();
					String path = this.homeDir.replace('\\', '/') + remote;

					srcZip = path;

					File f = new File(srcZip);
					des = f.getAbsolutePath() + ".zip";
					nameFileZip = f.getName();

					fileList.removeAll(fileList);

					getFileList(f);

					zip();
					path = des;
					this.serverUI.getTextArea()
							.append(getDateNow() + " : Sending file " + path.replace('/', '\\') + "\n");
					int numberFile1 = 4;
					out.writeInt(numberFile1);
					out.flush();

					File remoteFile = new File(path);
					long len = remoteFile.length();
					out.writeLong(len);
					out.flush();

					long sizeFile = len / numberFile1;

					InputStream is = new FileInputStream(remoteFile);
					System.out.println("Starting download");
					int resume1 = CONTINUE_DOWNLOAD;
					int read = -1;
					long count = 0, size, limit;
					int num;
					byte[] bytes = new byte[4096];

					for (int i = 1; i <= numberFile1; i++) {
						System.out.println("Sending part " + i);
						if (i < numberFile1) {
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
							resume1 = in.readInt();
							if (resume1 == CONTINUE_DOWNLOAD) {

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
							} else if (resume1 == CANCEL_DOWNLOAD) {
								this.serverUI.getTextArea().append(getDateNow() + " : Cancel downloading file ");
								i = numberFile1 + 1;
								j = num;
								break;
							}
						}
					}
					is.close();
					File file = new File(des);
					file.delete();

					out.writeUTF("complete");
					out.flush();

				}

				else if (request == MAKE_DIR) {
					String path = in.readUTF();
					path = this.homeDir + path;
					path = path.replace('/', '\\');
					System.out.println(path);

					File file = new File(path);
					if (file.isDirectory()) {
						out.writeUTF("exist");
						out.flush();
					} else {
						file.mkdir();
						out.writeUTF("success");
						out.flush();
					}
				} else if (request == DELETE) {
					String path = in.readUTF();
					path = this.homeDir + path;
					path = path.replace('/', '\\');
					System.out.println(path);

					File file = new File(path);

					if (file.isFile())
						file.delete();
					if (file.isDirectory())
						deleteFolder(file);

					out.writeUTF("success");
					out.flush();
				} else if (request == RENAME) {
					String path = in.readUTF();
					String newname = in.readUTF();

					File file = new File(path);
					if (file.isDirectory()) {
						String oldname = file.getName();
						if (oldname.equals(newname)) {
							out.writeUTF("nochange");
							out.flush();
						} else {
							File newFile = new File(file.getParent() + "\\" + newname);
							file.renameTo(newFile);
							out.writeUTF("change");
							out.flush();
						}
					}

					if (file.isFile()) {
						String oldname = file.getName();
						String extend = oldname.substring(oldname.lastIndexOf('.'));
						oldname = oldname.substring(0, oldname.lastIndexOf('.'));

						if (oldname.equals(newname)) {
							out.writeUTF("nochange");
							out.flush();
						} else {
							File newFile = new File(file.getParent() + "\\" + newname + extend);
							file.renameTo(newFile);
							out.writeUTF("change");
							out.flush();
						}
					}
				} else if (request == CLOSE) {
					server.curNumThreads--;
					System.out.println("Recieve close request from client");
					System.out.println("Closing this thread......");
					this.listUsername.remove(this.username);
					this.serverUI.getTextArea()
							.append(getDateNow() + " : Disconnected to client with username " + this.username + "\n");
					socket.close();
					openThread = false;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error to connect");
				if (request == UPLOAD) {
					for (int i = 1; i <= curNumber; i++) {
						System.out.println(filePath + "-part" + i);
						File file = new File(filePath + "-part" + i);
						System.out.println(file.delete());
					}

				}
				System.exit(0);
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
			System.out.println("Joined Files");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void unZip(String zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];
		try {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public String getDateNow() {
		String pattern = "HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		return simpleDateFormat.format(new Date());

	}

	public void getFileList(File file) {
		if (file.isFile())
			fileList.add(generateZipEntry(file.getAbsolutePath().toString()));
		if (file.isDirectory()) {
			for (String subFile : file.list()) {
				getFileList(new File(file, subFile));
			}
		}
	}

	private String generateZipEntry(String file) {
		return file.substring(srcZip.length() + 1, file.length());
	}

	public boolean deleteFolder(File folder) {

		if (folder == null)
			return false;

		if (!folder.exists())
			return true;

		if (!folder.isDirectory())
			return false;

		String[] list = folder.list();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File entry = new File(folder, list[i]);

				if (entry.isDirectory()) {
					if (!deleteFolder(entry))
						return false;
				} else {
					if (!entry.delete())
						return false;
				}
			}
		}

		return folder.delete();
	}

	public void zip() {
		byte[] bytes = new byte[1024];
		try {
			FileOutputStream fos = new FileOutputStream(des);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (String file : fileList) {
				System.out.println("File added: " + file);
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream fin = new FileInputStream(srcZip + File.separator + file);

				int read;

				while ((read = fin.read(bytes)) != -1) {
					zos.write(bytes, 0, read);
				}
				fin.close();
			}
			zos.closeEntry();
			zos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
