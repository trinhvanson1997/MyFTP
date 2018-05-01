package vn.hust.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class LocalDirPanel extends JPanel implements ActionListener {
	private JButton btnUpload, btnAdd, btnRename, btnDelete;

	private JTextField details;
	private File curDir;
	private File[] files;
	public String curPath;
	private JTable table;
	private JScrollPane scroll;
	private String[] columns = { "Name", "Type", "Size (kilobytes)", "Last Modified" };

	public LocalDirPanel(String path) {
		this.curPath = path;
		setPreferredSize(new Dimension(450, 400));
		setLayout(new BorderLayout(5, 5));

		add(createButtonPanel(), BorderLayout.NORTH);
		add(createListPanel(), BorderLayout.CENTER);

		
		
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				JTable tb = (JTable)mouseEvent.getSource();
				if(mouseEvent.getClickCount() == 1) {
					int i = table.getSelectedRow();
					if (i == 0)
						return;
					String filename = table.getValueAt(i, 0).toString();
					filename = curPath + "\\" + filename;
					details.setText(filename);
				}
				
				if(mouseEvent.getClickCount() == 2) {
					int i = table.getSelectedRow();
					if (i == 0) {
						String parent = curDir.getParent();
						if (parent == null)
							return;
						curPath = parent;
						listDirectory(curPath);
					} else {
						String filename = tb.getValueAt(i, 0).toString();
						File f = new File(curDir, filename);
						String fullname = f.getAbsolutePath();
						if (f.isDirectory()) {
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
		btnUpload = createButton("Upload");
		btnAdd = createButton("Add");
		btnDelete = createButton("Delete");
		btnRename = createButton("Rename");

		p.add(btnUpload);
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
		details.setFont(new Font("MonoSpaced", Font.PLAIN, 12));

		JPanel top = new JPanel(new FlowLayout());
		top.add(new JLabel("Local site"));
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
		File dir = new File(curPath);
		if (!dir.isDirectory()) {
			System.out.println("not a directory");
		} else {
			
			files = dir.listFiles();
			if (files != null) {
				Arrays.sort(files);
				
			}
			else {
				files=new File[0];
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

			data[i + 1][0] = files[i].getName();
			if(files[i].isDirectory()) {
				
				data[i + 1][2] = "";
			}
			else if(files[i].isFile()) {
				data[i + 1][1] = "file";
				data[i + 1][2] = String.valueOf(files[i].length()/1024);
			}
			else {
			data[i + 1][1] = "";}
			

			data[i + 1][3] = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(files[i].lastModified());
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





	public JButton getBtnUpload() {
		return btnUpload;
	}

	public void setBtnUpload(JButton btnUpload) {
		this.btnUpload = btnUpload;
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

	public String getCurPath() {
		return curPath;
	}

	public void setCurPath(String curPath) {
		this.curPath = curPath;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
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

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
