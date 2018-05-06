package vn.hust.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;

public class LocalDirPanelController {

	private LocalDirPanel localDirPanel;

	public LocalDirPanelController(LocalDirPanel localDirPanel, RemoteDirPanel remoteDirPanel, Client client,
			String username) {
		this.localDirPanel = localDirPanel;

		localDirPanel.getBtnUpload().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				String local = localDirPanel.getDetails().getText();
				String remote;

				File dir = new File(local);
				if (dir.isFile()) {
					local = local.replace('\\', '/');
				} else {
					JOptionPane.showMessageDialog(null, "Choose file to upload");
					return;
				}
				String name = dir.getName(); // tên của file cần upload
				if (remoteDirPanel.getCurDir().getName().equals(username)) {
					remote = "/" + name;
				} else {
					remote = "/" + remoteDirPanel.getCurDir().getName() + "/" + name;
				}
				System.out.println(remote);
				System.out.println("REMOTE: " + remote);
				UploadThread upload = new UploadThread(client, local, remote, remoteDirPanel);

				Thread upThread = new Thread(upload);
				upThread.start();

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
				if (path==null)
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
					String extend = oldFile.getName().substring(oldFile.getName().lastIndexOf('.') );
					
					String newName = JOptionPane.showInputDialog("Enter new name here");
					if(oldName.equals(newName)) {
						JOptionPane.showMessageDialog(null, "No change");
						return;
					}
					{
						File newFile = new File(dirPath+"\\"+newName+extend);
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

}
