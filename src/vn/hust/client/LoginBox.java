package vn.hust.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginBox extends JFrame {

	private JTextField tfHostname, tfUsername, tfPassword, tfPort;
	private JButton btnConnect, btnRegister;

	private String hostname, username, password;
	private int port;
	private Socket socket;

	public LoginBox() {

		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 500);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(5, 5));

		add(createLabelsPanel(), BorderLayout.WEST);
		add(createTextfieldsPanel(), BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);

		createActions();
		pack();
		setVisible(true);
	}

	private void createActions() {

		btnConnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				hostname = tfHostname.getText();
				username = tfUsername.getText();
				password = tfPassword.getText();
				port = Integer.parseInt(tfPort.getText());
				Client client = new Client(hostname, port);
				if (tfHostname.getText().equals(null) || tfHostname.getText().equals("")
						|| tfUsername.getText().equals(null) || tfUsername.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "Please fill out hostname and username!");
				} else {
					String response;
					try {

						response = client.login(username, password);
						if (response.equals("correct")) {

							System.out.println("Connected and Logged in to server !");
							JOptionPane.showMessageDialog(null, "Logged in successfully");
							dispose();
							MainUI mainUI = new MainUI(client, hostname, username, port);

							mainUI.addWindowListener(new java.awt.event.WindowAdapter() {
								@Override
								public void windowClosing(java.awt.event.WindowEvent windowEvent) {
									client.sendCloseRequest();
									mainUI.dispose();
								}
							});

						} else if (response.equals("online")) {
							JOptionPane.showMessageDialog(null,
									"This account is being used on other device! Please try again later!");
						} else if (response.equals("full")) {
							JOptionPane.showMessageDialog(null, "Server is full now. Please log in later!");
						} else {
							JOptionPane.showMessageDialog(null, "Username or password is incorrect. Please try again!");
						}
					
					}

					catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		btnRegister.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				hostname = tfHostname.getText();
				username = tfUsername.getText();
				password = tfPassword.getText();
				port = Integer.parseInt(tfPort.getText());

				if (tfHostname.getText().equals(null) || tfHostname.getText().equals("")
						|| tfUsername.getText().equals(null) || tfUsername.getText().equals("")
						|| tfUsername.getText().equals(null) || tfUsername.getText().equals("")
						|| tfPort.getText().equals(null) || tfPort.getText().equals("")) {

					JOptionPane.showMessageDialog(null, "Please fill out all fields!");
				} else {

					Client client = new Client(hostname, port);

					// TODO Auto-generated method stub
					if (client.register(username, password)) {
						JOptionPane.showMessageDialog(null, "Registered successfully");

					} else {
						JOptionPane.showMessageDialog(null, "Username existed! Please try again");
					}
					try {
						client.sendCloseRequest();
						client.socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

		});
	}

	private JPanel createLabelsPanel() {
		JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
		p.setBorder(new EmptyBorder(10, 10, 10, 10));
		p.add(new JLabel("Host Name"));
		p.add(new JLabel("Username"));
		p.add(new JLabel("Password"));
		p.add(new JLabel("Port"));

		return p;
	}

	private JPanel createTextfieldsPanel() {
		JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
		p.setBorder(new EmptyBorder(10, 10, 10, 10));
		tfHostname = new JTextField();
		/*
		 * tfHostname.setText("ftp.freevnn.com"); tfUsername = new JTextField();
		 * tfUsername.setText("freev_21943005"); tfPassword = new JTextField();
		 * tfPassword.setText("hslove");
		 */
		// tfHostname.setText("localhost");
		tfHostname.setText("192.168.1.100");
		tfUsername = new JTextField();
		tfUsername.setText("son");
		tfPassword = new JTextField();
		tfPassword.setText("son");
		tfPort = new JTextField();
		tfPort.setText("21");

		p.add(tfHostname);
		p.add(tfUsername);
		p.add(tfPassword);
		p.add(tfPort);

		return p;
	}

	private JPanel createButtonPanel() {
		JPanel p = new JPanel(new GridLayout(1, 2));
		p.setBorder(new EmptyBorder(10, 20, 10, 20));

		btnConnect = new JButton("Connect to server");
		Icon icon = new ImageIcon("icons/connect.png");
		btnConnect.setIcon(icon);
		btnRegister = new JButton("Register");

		p.add(btnConnect);
		p.add(btnRegister);
		return p;

	}

	public JTextField getTfHostname() {
		return tfHostname;
	}

	public void setTfHostname(JTextField tfHostname) {
		this.tfHostname = tfHostname;
	}

	public JTextField getTfUsername() {
		return tfUsername;
	}

	public void setTfUsername(JTextField tfUsername) {
		this.tfUsername = tfUsername;
	}

	public JTextField getTfPassword() {
		return tfPassword;
	}

	public void setTfPassword(JTextField tfPassword) {
		this.tfPassword = tfPassword;
	}

	public JTextField getTfPort() {
		return tfPort;
	}

	public void setTfPort(JTextField tfPort) {
		this.tfPort = tfPort;
	}

	public JButton getBtnConnect() {
		return btnConnect;
	}

	public void setBtnConnect(JButton btnConnect) {
		this.btnConnect = btnConnect;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
