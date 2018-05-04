package vn.hust.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnect {
	private Connection conn;
	private Statement stm;
	private java.sql.ResultSet rs;

	private String url = "jdbc:mysql://localhost:3306/ftp_account";
	private String user = "root";
	private String password = "sontrinh";

	public DBConnect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, password);
			if (conn != null) {
				System.out.println("Connected to database");
				stm = conn.createStatement();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean login(String username, String password) {
		String sql = "SELECT count(*) FROM account WHERE username = '"+username+"' AND password = '"+password+"'; ";
		try {
			rs = stm.executeQuery(sql);
			if(rs.next() && rs.getInt(1)==0) {
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	// return true if account existed
	public boolean checkAccount(String username) {

		String sql = "SELECT count(*) FROM account WHERE username = '" + username + "'";
		try {

			rs = stm.executeQuery(sql);

			if (rs.next() && rs.getInt(1) == 0) {

				return false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean register(String username, String password) {

		try {

			if (!checkAccount(username)) {
				String sql = "INSERT INTO account VALUES ('" + username + "','" + password + "')";
				stm.executeUpdate(sql);

				return true;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
	
	public void close() {
		try {
			stm.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
