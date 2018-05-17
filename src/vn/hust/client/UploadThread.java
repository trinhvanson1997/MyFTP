package vn.hust.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class UploadThread implements Runnable, ActionListener {
	public static final int UPLOAD = 4,CANCEL_UPLOAD = 5,CONTINUE_UPLOAD=6;
	private Client client;

	// đường dẫn nguồn
	private String localPath;

	// đường dẫn đích
	private String remotePath;

	// flag để xác định pause hay resume
	private boolean paused = false;
	private boolean cancel = false;

	// các biến đo phần trăm file đã đc upload
	private Timer t;
	private long count = 0;
	private long len;
	private int percent;

	// cac bien de upload cac part
	private long sizeFile; // kích thước 1 part
	private int numberFile = 4; // số lượng part
	private long limit;

	// button pause, resume, cancel
	private JFrame frame;
	private JButton btnPause, btnResume, btnCancel;

	private RemoteDirPanel remoteDirPanel;

	public UploadThread(Client client, String localPath, String remotePath, RemoteDirPanel remoteDirPanel) {
		this.client = client;
		this.localPath = localPath;
		this.remotePath = remotePath;
		this.remoteDirPanel = remoteDirPanel;

		File file = new File(localPath);
		String fileName = file.getName();

		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setTitle("Uploading " + fileName + " 0%");
		frame.setSize(400, 150);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setSize(400, 20);

		JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.setBorder(new EmptyBorder(20, 10, 0, 20));
		progressPanel.add(progressBar, BorderLayout.CENTER);

		frame.add(progressPanel, BorderLayout.NORTH);
		frame.add(createButtonsPanel(), BorderLayout.CENTER);

		t = new Timer(100, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (percent == 100) {
					t.stop();
					frame.dispose();
				} else {
					Double temp = ((double) count / len) * 100;
					percent = temp.intValue();
					progressBar.setValue(percent);
					frame.setTitle("Uploading " + fileName + " " + percent + "%");
				}
			}
		});
		t.start();

		frame.setVisible(true);
	}

	@Override
	public void run() {
		try {
			client.out.writeInt(UPLOAD);
			client.out.flush();

			client.out.writeUTF(remotePath);

			client.out.writeInt(numberFile);
			client.out.flush();

			File localFile = new File(this.localPath);
			len = localFile.length();
			
			sizeFile = len / numberFile;

			InputStream is = new FileInputStream(localFile);

			System.out.println("Starting upload file");

			int read = -1;
			// biến count để đếm lưu lượng file đã upload
			count = 0;

			// kich thuoc tung part
			long size, limit;
			int num;
			byte[] bytes = new byte[4096];

			for (int i = 1; i <= numberFile; i++) {
				System.out.println("Sending part " + i);
				if (i < numberFile) {
					size = sizeFile;
					limit = size * i;

				} else {
					size = len - (sizeFile * 3);
					limit = len;
				}

				if ((int) (size % 4096) == 0) {
					num = (int) (size / 4096);
				} else {
					num = (int) (size / 4096) + 1;
				}
				client.out.writeInt(num);
				client.out.flush();

				for (int j = 0; j < num; j++) {
					if (paused) {
						waitIfPaused();
					}

					if (cancel) {
						System.out.println("Cancel upload file to server");
						//trước khi hủy upload cần gửi lệnh đến server
						//đồng thời xóa các file đã được upload
						client.out.writeInt(CANCEL_UPLOAD);
						client.out.flush();
						frame.dispose();
						//break two loop
						i=numberFile+1;
						j = num;
						
						break;
					}
					else {
						client.out.writeInt(CONTINUE_UPLOAD);
						client.out.flush();
					}

					if ((limit - count) < 4096)
						bytes = new byte[(int) (limit - count)];
					else
						bytes = new byte[4096];

					read = is.read(bytes);

					count += read;
					// send size of arr
					client.out.writeInt(read);
					client.out.flush();

					// send arr
					client.out.write(bytes);
					client.out.flush();
				}
			}
			is.close();
			
			String rs = client.in.readUTF();
			if(rs.equals("complete")) {
				this.remoteDirPanel.listDirectory(this.remoteDirPanel.getCurPath());
				JOptionPane.showMessageDialog(null, "Uploaded file");
			}
			else if(rs.equals("cancel")) {
				this.remoteDirPanel.listDirectory(this.remoteDirPanel.getCurPath());
				JOptionPane.showMessageDialog(null, "Canceled to upload file");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to upload");
			System.exit(0);
		}

	}

	public synchronized void pause() {
		paused = true;
		System.out.println("paused");
	}

	public synchronized void resume() {
		paused = false;
		notifyAll();

		System.out.println("resume");
	}

	public synchronized void waitIfPaused() {
		while (paused) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public JPanel createButtonsPanel() {
		JPanel p = new JPanel(new FlowLayout(0, 10, 30));
		btnPause = createButton("Pause");
		btnResume = createButton("Resume");
		btnResume.setEnabled(false);
		btnCancel = createButton("Cancel");

		p.add(btnPause);
		p.add(btnResume);
		p.add(btnCancel);

		return p;

	}

	public JButton createButton(String name) {
		JButton button = new JButton(name);
		button.addActionListener(this);
		return button;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnPause) {
			pause();
			btnPause.setEnabled(false);
			btnResume.setEnabled(true);

		}

		if (e.getSource() == btnResume) {
			resume();
			btnPause.setEnabled(true);
			btnResume.setEnabled(false);
		}

		if (e.getSource() == btnCancel) {
	/*		if (JOptionPane.showConfirmDialog(null, "Are you sure want to cancel?", "WARNING",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				resume();
				cancel = true;

			} else {
				return;
			}*/
			resume();
			cancel = true;
		}

	}

}

