package vn.hust.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class RemoteDirPanel extends JPanel implements ActionListener, ItemListener {
	private JButton btnDownload, btnAdd, btnRename, btnDelete;
	private JTable table; // danh sach ten cac thu muc va file trong thu muc hien tai
	private JTextField details; // duong dan den thu muc hien tai tu thu muc goc
	private File curDir; // thu muc hien tai
	private File[] files; // danh sach cac file va thu muc htai
	private String[] fileNames;
	private String curDirectory;
	private String curPath; // duong dan tuyet doi den thu muc htai

	private JScrollPane scroll;
	private String[] columns = { "Name", "Type", "Size (kilobytes)", "Last Modified" };

	private Client client;

	public RemoteDirPanel(String path, Client client) {
		this.curPath = path;
		this.client = client;

		setPreferredSize(new Dimension(360, 400));
		setLayout(new BorderLayout(5, 5));

		add(createButtonPanel(), BorderLayout.NORTH);
		add(createListPanel(), BorderLayout.CENTER);

		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				JTable tb = (JTable) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 1) {
					int i = table.getSelectedRow();
					if (i == 0)
						return;
					String filename = table.getValueAt(table.getSelectedRow(), 0).toString();

					if (curPath.equals("/")) {
						filename = curPath + filename;
					} else
						filename = curPath + "/" + filename;
					details.setText(filename);
				}

				if (mouseEvent.getClickCount() == 2) {
					int i = table.getSelectedRow();
					if (i == 0) {

						if (curPath.equals("/"))
							return;
						else if ((curPath.split("/").length - 1) == 1) { // vd /htdoc /abc
							curPath = "/";
						} else {
							curPath = curPath.substring(0, curPath.lastIndexOf('/'));
						}
						listDirectory(curPath);
					} else {
						String filename = table.getValueAt(table.getSelectedRow(), 0).toString();
						String fullname;
						if (curPath.equals("/")) {
							fullname = curPath + filename;
						} else
							fullname = curPath + "/" + filename;

						File f;
						f = client.getFile(fullname);
						if (client.checkDir(f.getAbsolutePath())) {
							curPath = fullname;
							listDirectory(curPath);
						}

					}
				}
			}
		});
	}

	private JPanel createButtonPanel() {
		JPanel p = new JPanel(new FlowLayout(10, 10, 10));
		Icon icon = new ImageIcon("icons/download.png");
		btnDownload = createButton("Download");
		btnDownload.setIcon(icon);

		icon = new ImageIcon("icons/add.png");
		btnAdd = createButton("Add Folder");
		btnAdd.setIcon(icon);
		icon = new ImageIcon("icons/delete.png");
		btnDelete = createButton("Delete");
		btnDelete.setIcon(icon);
		icon = new ImageIcon("icons/rename.png");
		btnRename = createButton("Rename");
		btnRename.setIcon(icon);

		p.add(btnDownload);
		p.add(btnAdd);
		p.add(btnDelete);
		p.add(btnRename);

		return p;
	}

	private JPanel createListPanel() {
		// TODO Auto-generated method stub
		JPanel p = new JPanel(new BorderLayout(10, 10));

		table = new JTable();
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowGrid(false);

		scroll = new JScrollPane();
		scroll.setViewportView(table);

		details = new JTextField(40);
		details.setEditable(false);
		details.setFont(new Font("MonoSpaced", Font.PLAIN, 13));

		JPanel top = new JPanel(new FlowLayout());
		top.add(new JLabel("Remote site"));
		top.add(details);
		listDirectory(curPath);

		JPanel center = new JPanel(new BorderLayout());
		center.add(scroll, BorderLayout.CENTER);

		p.add(top, BorderLayout.NORTH);
		p.add(center, BorderLayout.CENTER);
		return p;
	}

	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}

	public void listDirectory(String curPath) {
		File dir;
		dir = client.getFile(curPath);

		if (!client.checkDir(dir.getAbsolutePath())) {
			System.out.println("not a directory");
		} else {
			File[] files = client.listFiles(curPath); // lay danh sach file trong thu muc curPath

		
			if (files != null) {
				Arrays.sort(files);

			} else {
				files = new File[0];
			}
			updateTable(files);
			details.setText(curPath);
			curDir = dir;
		}

	}

	public void updateTable(File[] files) {
		String[][] data = new String[files.length + 1][columns.length];
		data[0][0] = "..";

		for (int i = 0; i < files.length; i++) {
			String path = files[i].getAbsolutePath();
			
			data[i + 1][0] = files[i].getName();
			if (client.checkDir(path)) {
				
				data[i + 1][2] = "";
			} else if (client.checkFileByPath(path)) {
				data[i + 1][1] = "file";
				data[i + 1][2] = String.valueOf(client.getSize(path) / 1024);
			} else {
				data[i + 1][1] = "";
			}

			data[i + 1][3] = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(client.getLastModified(path));
		}

		DefaultTableModel tableModel = new DefaultTableModel(data, columns) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// TODO Auto-generated method stub
				return false;
			}
		};

		table.setModel(tableModel);
		tableModel.fireTableDataChanged();
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	public JButton getBtnDownload() {
		return btnDownload;
	}

	public void setBtnDownload(JButton btnDownload) {
		this.btnDownload = btnDownload;
	}

	public JButton getBtnAdd() {
		return btnAdd;
	}

	public void setBtnAdd(JButton btnAdd) {
		this.btnAdd = btnAdd;
	}

	public JButton getBtnRename() {
		return btnRename;
	}

	public void setBtnRename(JButton btnRename) {
		this.btnRename = btnRename;
	}

	public JButton getBtnDelete() {
		return btnDelete;
	}

	public void setBtnDelete(JButton btnDelete) {
		this.btnDelete = btnDelete;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	public JTextField getDetails() {
		return details;
	}

	public void setDetails(JTextField details) {
		this.details = details;
	}

	public File getCurDir() {
		return curDir;
	}

	public void setCurDir(File curDir) {
		this.curDir = curDir;
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

	public String getCurDirectory() {
		return curDirectory;
	}

	public void setCurDirectory(String curDirectory) {
		this.curDirectory = curDirectory;
	}

	public String getCurPath() {
		return curPath;
	}

	public void setCurPath(String curPath) {
		this.curPath = curPath;
	}

	public JScrollPane getScroll() {
		return scroll;
	}

	public void setScroll(JScrollPane scroll) {
		this.scroll = scroll;
	}

	public String[] getColumns() {
		return columns;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

}
