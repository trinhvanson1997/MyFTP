# MyFTP using Socket Java
## HƯỚNG DẪN CHẠY CHƯƠNG TRÌNH
- tải project về và import vào Eclipse hoặc Netbeans
- import file ftp_account.sql vào MySQL Workbench
- để kết nối với database, cần Build Path cho Project tới file mysql-connector.jar
- Chỉnh các thông số trên máy cá nhân:
	+ mở project , vào file DBConnect.java thay đổi username và pass theo cấu hình Mysql trên máy
	+ Tạo 1 thư mục ngoài desktop với tên FTP và
	vào file ClientThread.java sửa đường dẫn theo máy cá nhân tại dòng
  <pre>
  public String homeDir = "C:\\Users\\sontrinh\\Desktop\\FTP\\";
  </pre> 

	
