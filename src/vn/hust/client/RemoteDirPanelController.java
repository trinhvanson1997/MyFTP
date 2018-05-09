package vn.hust.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;

public class RemoteDirPanelController {
	public RemoteDirPanelController(LocalDirPanel localDirPanel, RemoteDirPanel remoteDirPanel, Client client,
			String username) {
		remoteDirPanel.getBtnDownload().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int row = remoteDirPanel.getTable().getSelectedRow();
				if (row == -1 || row == 0) {
					JOptionPane.showMessageDialog(null, "Please choose file or folder you want to download");
				}

				else {
				
				String remote = remoteDirPanel.getDetails().getText();

				File remoteFile = client.getFile(remote);

				String name = remoteFile.getName();

				String local = localDirPanel.getCurDir().getAbsolutePath();
				local = local.replace('\\', '/');
				local = local + "/" + name;

				File f = new File(local);
				if (f.isFile()) {
					int choice = JOptionPane.showConfirmDialog(null, "This file existed. Do you want to replace?",
							"Warning", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.YES_OPTION) {
						if (client.checkFile(remote)) {
							DownloadThread download = new DownloadThread(client, local, remote, localDirPanel);
							Thread downloadThread = new Thread(download);
							downloadThread.start();
						}
						if (client.checkDir(remoteFile.getAbsolutePath())) {
							local += ".zip";
							System.out.println("LOCAL Path: " + local);
							DownloadFolderThread download = new DownloadFolderThread(client, local, remote,
									localDirPanel);
							Thread downloadThread = new Thread(download);
							downloadThread.start();
						}
					} else
						return;
				} else {
					if (client.checkFile(remote)) {
						DownloadThread download = new DownloadThread(client, local, remote, localDirPanel);
						Thread downloadThread = new Thread(download);
						downloadThread.start();
					}
					if (client.checkDir(remoteFile.getAbsolutePath())) {
						local += ".zip";
						DownloadFolderThread download = new DownloadFolderThread(client, local, remote, localDirPanel);
						Thread downloadThread = new Thread(download);
						downloadThread.start();
					}
				}
				}
			}
		});

		remoteDirPanel.getBtnAdd().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog("Enter directory name here");

				if (name != null) {
					String path = remoteDirPanel.getCurPath();
					if (path.equals("/")) {
						path += name;
					} else {
						path += "/" + name;
					}
					System.out.println(path);
					String rs = client.makeDir(path);
					if (rs.equals("exist")) {
						JOptionPane.showMessageDialog(null,
								"This directory 's already existed, Please choose other name!");
						return;
					}
					if (rs.equals("success")) {
						JOptionPane.showMessageDialog(null, "Created directory " + name);
						remoteDirPanel.listDirectory(remoteDirPanel.getCurPath());
					}

				} else
					return;

			}
		});

		remoteDirPanel.getBtnDelete().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = remoteDirPanel.getTable().getSelectedRow();
				if (row == -1 || row == 0) {
					JOptionPane.showMessageDialog(null, "Please choose file or folder you want to delete");
				}

				else {
					String path = remoteDirPanel.getDetails().getText();

					int choice = JOptionPane.showConfirmDialog(null, "Are you sure want to delete this file?",
							"Warning", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.YES_OPTION) {

						if (client.delete(path).equals("success")) {
							JOptionPane.showMessageDialog(null, "Deleted file/folder ");
							remoteDirPanel.listDirectory(remoteDirPanel.getCurPath());
						} else
							return;

					}
				}
			}
		});
		
		remoteDirPanel.getBtnRename().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = remoteDirPanel.getTable().getSelectedRow();
				if(row == -1 || row == 0) {
					JOptionPane.showMessageDialog(null, "Please choose file/folder to rename");
					return;
				}
				else {
					
					
					String newName = JOptionPane.showInputDialog("Enter new name here");
					if (newName != null) {
						String path = remoteDirPanel.getDetails().getText();
						File file = client.getFile(path);
						
						String rs = client.rename(file.getAbsolutePath(), newName);
						if(rs.equals("nochange")) {
							JOptionPane.showMessageDialog(null, "No change");
						}
						else {
							JOptionPane.showMessageDialog(null, "Renamed successfully");
							remoteDirPanel.listDirectory(remoteDirPanel.getCurPath());
						}
					} else
						return;
					
				}
				
			}
		});
	}

}
