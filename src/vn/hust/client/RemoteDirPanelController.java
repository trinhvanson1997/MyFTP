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
							System.out.println("LOCAL Path: "+ local);
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
		});
	}
}
