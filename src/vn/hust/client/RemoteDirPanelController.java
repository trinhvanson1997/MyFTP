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
				File dir = client.getFile(remote);
				String name = client.getName(dir.getAbsolutePath());
				
				if(client.checkFile(dir.getAbsolutePath())) {
					remote = remote;
				}
				else {
					JOptionPane.showMessageDialog(null, "choose file to download");
					return;
				}
				
				String local = localDirPanel.getCurDir().getAbsolutePath();
				local = local.replace('\\', '/');
				local =local +"/"+name;
				DownloadThread download = new DownloadThread(client, local, remote,localDirPanel);
				Thread downloadThread = new Thread(download);
				downloadThread.start();
			}
		});
	}
}
