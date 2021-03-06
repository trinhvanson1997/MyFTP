package vn.hust.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MainUI extends JFrame {
	private String hostname, username; // hien thi host, user va port tren giao dien
	private int port;
	private JButton btnLogOut;
	private JLabel lbStatus;
	private JPanel statusPanel;
	

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

		new LocalDirPanelController(this, localDirPanel, remoteDirPanel, client, username);
		new RemoteDirPanelController(localDirPanel, remoteDirPanel, client, username);

		setTitle("FTP CLient");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 600);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(5, 5));

		add(createInfoPanel(), BorderLayout.NORTH);
		add(createCenterPanel(), BorderLayout.CENTER);
		
		
		add(createBottomPanel(), BorderLayout.SOUTH);
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

	private JPanel createBottomPanel() {
		statusPanel = new JPanel(new BorderLayout(10, 10));
		lbStatus = new JLabel("Status: ");
		statusPanel.add(lbStatus, BorderLayout.CENTER);
		return statusPanel;
	}

	private JPanel createInfoPanel() {
		JPanel p = new JPanel(new FlowLayout(10, 10, 30));
		p.setBorder(new EmptyBorder(5, 5, 5, 5));
		p.add(new JLabel("Host:  " + this.hostname));
		p.add(new JLabel("Username:  " + this.username));
		p.add(new JLabel("Port: " + this.port));

		btnLogOut = new JButton("Log out");
		Icon icon = new ImageIcon("icons/logout.png");
		btnLogOut.setIcon(icon);
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

	}

	public JButton getBtnLogOut() {
		return btnLogOut;
	}

	public void setBtnLogOut(JButton btnLogOut) {
		this.btnLogOut = btnLogOut;
	}

	public JLabel getLbStatus() {
		return lbStatus;
	}

	public void setLbStatus(JLabel lbStatus) {
		this.lbStatus = lbStatus;
	}

	public LocalDirPanel getLocalDirPanel() {
		return localDirPanel;
	}

	public void setLocalDirPanel(LocalDirPanel localDirPanel) {
		this.localDirPanel = localDirPanel;
	}

	public RemoteDirPanel getRemoteDirPanel() {
		return remoteDirPanel;
	}

	public void setRemoteDirPanel(RemoteDirPanel remoteDirPanel) {
		this.remoteDirPanel = remoteDirPanel;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public JPanel getStatusPanel() {
		return statusPanel;
	}

	public void setStatusPanel(JPanel statusPanel) {
		this.statusPanel = statusPanel;
	}
	
	

}
