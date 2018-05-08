package vn.hust.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;

public class RemoteDirPanelController {
	public RemoteDirPanelController(LocalDirPanel localDirPanel,RemoteDirPanel remoteDirPanel,Client client, String username) {
	remoteDirPanel.getBtnDownload().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String remote = remoteDirPanel.getDetails().getText();
				String name = "";
				
				if(client.checkFile(remote)) {
				
					name = remote.substring(remote.lastIndexOf('/')+1);
				}
				if(client.checkDir(remote)) {
					
				}
				
				String local = localDirPanel.getCurDir().getAbsolutePath();
				local = local.replace('\\', '/');
				local =local +"/"+name;
				
				File f = new File(local);
				if(f.isFile()) {
					int choice = JOptionPane.showConfirmDialog(null, "This file existed. Do you want to replace?",
							"Warning", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.YES_OPTION) {
						DownloadThread download = new DownloadThread(client, local, remote,localDirPanel);
						Thread downloadThread = new Thread(download);
						downloadThread.start();
					} else
						return;
				}
				else {
					DownloadThread download = new DownloadThread(client, local, remote,localDirPanel);
					Thread downloadThread = new Thread(download);
					downloadThread.start();
				}
				
			}
		});
	}
}
