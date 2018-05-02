package vn.hust.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class MainUI extends JFrame {
	private String hostname, username; // hien thi host, user va port tren giao dien
	private int port;
	private JButton btnLogOut;
	
	private FTPClient ftpClient;
	// private UploadThread upload;
	private LocalDirPanel localDirPanel;
	private RemoteDirPanel remoteDirPanel;

	public Object lock = new Object();
	private Client client;

	public MainUI(Client client, String hostname, String username, int port) {
		this.client = client;
		this.hostname = hostname;
		this.username = username;
		this.port = port;

		localDirPanel = new LocalDirPanel("D:\\");
		remoteDirPanel = new RemoteDirPanel("/", client);

		setTitle("FTP CLient");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 600);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(5, 5));

		add(createInfoPanel(), BorderLayout.NORTH);
		add(createCenterPanel(), BorderLayout.CENTER);

		setResizable(false);
		setVisible(true);
createActions();
	}

	private JPanel createCenterPanel() {
		JPanel p = new JPanel(new BorderLayout(10, 10));
		p.add(localDirPanel, BorderLayout.WEST);
		p.add(remoteDirPanel, BorderLayout.CENTER);
		return p;
	}

	private JPanel createInfoPanel() {
		JPanel p = new JPanel(new FlowLayout(10, 10, 30));
		p.setBorder(new EmptyBorder(5, 5, 5, 5));
		p.add(new JLabel("Host:  " + this.hostname));
		p.add(new JLabel("Username:  " + this.username));
		p.add(new JLabel("Port: " + this.port));
		
		btnLogOut = new  JButton("Log out");
		p.add(btnLogOut);
		return p;

	}

	private void createActions() {
		btnLogOut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (JOptionPane.showConfirmDialog(null, "Are you sure want to log out?", "WARNING",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					client.sendCloseRequest();
					MainUI.this.dispose();
					LoginBox loginBox = new LoginBox();

				} else {
					return;
				}
				
			}
		});
		
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
