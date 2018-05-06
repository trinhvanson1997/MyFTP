package vn.hust.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;

public class LocalDirPanelController {
	
	
	public LocalDirPanelController(LocalDirPanel localDirPanel,RemoteDirPanel remoteDirPanel,Client client, String username) {
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
				String name = dir.getName(); //tên của file cần upload
				if(remoteDirPanel.getCurDir().getName().equals(username)) {
					remote =   "/" + name;
				}
				else {
					remote =  "/"+remoteDirPanel.getCurDir().getName()+ "/" + name;
				}
				System.out.println(remote);
				System.out.println("REMOTE: "+remote);
				UploadThread upload = new UploadThread(client, local, remote, remoteDirPanel);

				Thread upThread = new Thread(upload);
				upThread.start();

			}
		});
		
		localDirPanel.getBtnAdd().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

			}

		});
		localDirPanel.getBtnDelete().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

			}

		});
	}
	
	
}
