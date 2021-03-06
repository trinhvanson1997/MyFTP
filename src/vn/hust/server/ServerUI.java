package vn.hust.server;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.soap.Text;

public class ServerUI extends JFrame {
	private JTextArea textArea;
	private JScrollPane scroll;
	
	public ServerUI() {
		setTitle("FTP Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 400);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(5, 5));
		
		textArea = new JTextArea();
		scroll = new JScrollPane();
		scroll.setViewportView(textArea);
		add(scroll, BorderLayout.CENTER);
	
		setResizable(false);
		setVisible(true);
	}

	public JTextArea getTextArea() {
		return textArea;
	}


	

}
