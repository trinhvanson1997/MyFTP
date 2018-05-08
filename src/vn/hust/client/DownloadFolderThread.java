package vn.hust.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sound.midi.Soundbank;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class DownloadFolderThread implements Runnable, ActionListener {
	public static final int DOWNLOAD_FOLDER = 19, CANCEL_DOWNLOAD = 11, CONTINUE_DOWNLOAD = 12;
	private Client client;

	// đường dẫn nguồn
	private String localPath;

	// đường dẫn đích
	private String remotePath;

	// flag để xác định pause hay resume
	private boolean paused = false;
	private boolean cancel = false;

	// các biến đo phần trăm file đã đc download
	private Timer t;
	private long count = 0;
	private long len;
	private int percent;

	// cac bien de download cac part
	private long sizeFile; // kích thước 1 part
	private int numberFile = 4; // số lượng part
	private long limit;

	// button pause, resume, cancel
	private JFrame frame;
	private JButton btnPause, btnResume, btnCancel;
	private LocalDirPanel localDirPanel;

	public DownloadFolderThread(Client client, String localPath, String remotePath, LocalDirPanel localDirPanel) {
		this.client = client;
		this.localPath = localPath;
		this.remotePath = remotePath;
		this.localDirPanel = localDirPanel;
		
		File file = client.getFile(remotePath);
		String fileName = client.getName(file.getAbsolutePath());

		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setTitle("Downloading " + fileName + " 0%");
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
					frame.setTitle("Downloading " + fileName + " " + percent + "%");
				}
			}
		});
		t.start();

		frame.setVisible(true);
	}

	@Override
	public void run() {
		try {
			client.out.writeInt(DOWNLOAD_FOLDER);
			client.out.flush();
			System.out.println("-------DOWNLOADING FOLDER---------");

			client.out.writeUTF(remotePath);
			client.out.flush();
			
			int numberFile = client.in.readInt();
			len = client.in.readLong();
System.out.println("LOCAL: "+localPath);
			int curNumber = 0; // lưu file thứ i đang được download
			for (int i = 1; i <= numberFile; i++) {
				
				System.out.println("recieving part " + i);
				OutputStream os2 = new BufferedOutputStream(new FileOutputStream(localPath + "-part" + i));

				int num = client.in.readInt();

				for (int j = 0; j < num; j++) {
					if (paused) {
						waitIfPaused();
					}

					if (cancel) {
						System.out.println("Cancel download file from server");
						// trước khi hủy download cần gửi lệnh đến server
						// đồng thời xóa các file đã được download
						client.out.writeInt(CANCEL_DOWNLOAD);
						client.out.flush();
						frame.dispose();
						// break two loop
						curNumber = i;
						i = numberFile + 1;
						j = num;
						
						break;
					} else {
						client.out.writeInt(CONTINUE_DOWNLOAD);
						client.out.flush();
					}
					count = client.in.readLong();

					int read = client.in.readInt();

					byte[] bytes = new byte[read];
					client.in.readFully(bytes, 0, read);
					os2.write(bytes, 0, read);

				}
				os2.close();
			}
			if(cancel) {
				for(int i = 1;i<=curNumber;i++) {
					File file = new File(localPath + "-part" + i);
					file.delete();
				}
			}
			else {
				joinFile(localPath, numberFile);
				unZip(localPath, localPath.substring(0,localPath.lastIndexOf('.')));
			}
			if(client.in.readUTF().equals("complete")) {
				localDirPanel.listDirectory(localDirPanel.getCurPath());
				File file = new File(localPath);
				file.delete();
				JOptionPane.showMessageDialog(null, "Downloaded folder");
			}
			
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void joinFile(String pathname, int numberFile) {
		try {
			OutputStream outputStream = new FileOutputStream(new File(pathname));
			InputStream inputStream;

			for (int i = 1; i <= numberFile; i++) {
				File file = new File(pathname + "-part" + i);
				inputStream = new FileInputStream(file);

				byte[] bytes = new byte[4096];
				int read = -1;
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				inputStream.close();
				file.delete();
			}
			outputStream.close();
			System.out.println("Joined File");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		// TODO Auto-generated method stub
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
			if (JOptionPane.showConfirmDialog(null, "Are you sure want to cancel?", "WARNING",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				resume();
				cancel = true;

			} else {
				return;
			}

		}

	}
	
	public void unZip(String zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];
		try {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);
				
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
