package vn.hust.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

public class LocalDirPanelController {

	private LocalDirPanel localDirPanel;
	public String srcZip;
	public String nameFileZip;
	String des = "";
	public List<String> fileList = new ArrayList<>();

	public LocalDirPanelController(MainUI mainUI, LocalDirPanel localDirPanel, RemoteDirPanel remoteDirPanel,
			Client client, String username) {
		this.localDirPanel = localDirPanel;

		localDirPanel.getBtnUpload().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				String local = localDirPanel.getDetails().getText().replace("\\\\", "\\");
				String remote = "";

				File dir = new File(local);
				if (dir.isFile()) {
					local = local;
				}
				if (dir.isDirectory()) {
					srcZip = local;

					File f = new File(srcZip);
					des = f.getAbsolutePath() + ".zip";
					nameFileZip = f.getName();

					fileList.removeAll(fileList);

					mainUI.getLbStatus().setText("Status: Getting list files from folder " + nameFileZip);
				mainUI.getContentPane().validate();
				mainUI.getContentPane().repaint();
					
					getFileList(f);
					mainUI.getLbStatus().setText("Status: Zipping folder before uploading ");
					mainUI.getContentPane().validate();
					mainUI.getContentPane().repaint();
					
					zip();
					
					mainUI.getLbStatus().setText("Status: Ready to upload");
					mainUI.getContentPane().validate();
					mainUI.getContentPane().repaint();
					
					local = des;

				}
				String name = dir.getName(); // tên của file cần upload
				if (remoteDirPanel.getCurDir().getName().equals(username)) {
					if (dir.isFile())
						remote = "/" + name;
					if (dir.isDirectory())
						remote = "/" + name + ".zip";
				} else {
					if (dir.isFile())
						remote = "/" + remoteDirPanel.getCurDir().getName() + "/" + name;
					if (dir.isDirectory())
						remote = "/" + remoteDirPanel.getCurDir().getName() + "/" + name + ".zip";
				}
				File remoteFile = client.getFile(remote);

				if (dir.isDirectory())
					remoteFile = client.getFile(remote.substring(0, remote.lastIndexOf('.')));
			

				if (remoteFile.isDirectory() || remoteFile.isFile()) {
					int choice = JOptionPane.showConfirmDialog(null, "This file existed. Do you want to replace?",
							"Warning", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.YES_OPTION) {
						if (dir.isFile())
							upload(client, local, remote, remoteDirPanel);
						if (dir.isDirectory())
							uploadFolder(client, local, remote, remoteDirPanel);
					} else {
						return;
					}
				} else {
					if (dir.isFile())
						upload(client, local, remote, remoteDirPanel);
					if (dir.isDirectory())
						uploadFolder(client, local, remote, remoteDirPanel);
				}

			}
		});

		localDirPanel.getBtnAdd().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog("Enter directory name here");
				String path = getCurrentDirectoryPath();
				path = path + "\\" + name;
				File file = new File(path);
				if (file.isDirectory()) {
					JOptionPane.showMessageDialog(null, "This directory 's already existed, Please choose other name!");
					return;
				} else {
					file.mkdir();
					JOptionPane.showMessageDialog(null, "Created directory " + name);
					localDirPanel.listDirectory(localDirPanel.getCurPath());
				}
			}

		});
		localDirPanel.getBtnDelete().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String path = getCurrentFilePath();
				if (path == null)
					return;
				else {

					int choice = JOptionPane.showConfirmDialog(null, "Are you sure want to delete this file?",
							"Warning", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.YES_OPTION) {
						File file = new File(path);
						String name = file.getName();
						file.delete();
						JOptionPane.showMessageDialog(null, "Deleted file " + name);
						localDirPanel.listDirectory(localDirPanel.getCurPath());
					} else
						return;
				}
			}

		});

		localDirPanel.getBtnRename().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				String filePath = getCurrentFilePath();
				String dirPath = getCurrentDirectoryPath();

				if (filePath.equals(null))
					return;
				else {

					File oldFile = new File(filePath);

					String oldName = oldFile.getName().substring(0, oldFile.getName().lastIndexOf('.'));
					String extend = oldFile.getName().substring(oldFile.getName().lastIndexOf('.'));

					String newName = JOptionPane.showInputDialog("Enter new name here");
					if (oldName.equals(newName)) {
						JOptionPane.showMessageDialog(null, "No change");
						return;
					}
					{
						File newFile = new File(dirPath + "\\" + newName + extend);
						oldFile.renameTo(newFile);
						JOptionPane.showMessageDialog(null, "Renamed successfully");
						localDirPanel.listDirectory(localDirPanel.getCurPath());
					}
				}
			}
		});
	}

	private String getCurrentDirectoryPath() {
		String path = localDirPanel.getCurPath();
		return path;
	}

	private String getCurrentFilePath() {
		String local = localDirPanel.getDetails().getText();
		File file = new File(local);
		if (file.isFile()) {
			return file.getAbsolutePath();
		} else {
			JOptionPane.showMessageDialog(null, "This is not a file");

		}
		return null;
	}

	private void upload(Client client, String local, String remote, RemoteDirPanel remoteDirPanel) {
		System.out.println(remote);
		System.out.println("REMOTE: " + remote);
		UploadThread upload = new UploadThread(client, local, remote, remoteDirPanel);

		Thread upThread = new Thread(upload);
		upThread.start();
	}

	private void uploadFolder(Client client, String local, String remote, RemoteDirPanel remoteDirPanel) {
		System.out.println(remote);
		System.out.println("REMOTE: " + remote);
		UploadFolderThread upload = new UploadFolderThread(client, local, remote, remoteDirPanel);

		Thread upThread = new Thread(upload);
		upThread.start();
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
