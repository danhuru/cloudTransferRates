import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {

	public final static int SOCKET_PORT = 13267; 
	static ServerSocket servsock = null;
	static Socket socket = null;
	static void send(ArrayList<File> files) {

		try {
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(socket.getOutputStream()));
			System.out.println(files.size());
			// write the number of files to the server
			dos.writeInt(files.size());
			dos.flush();

			// write file names
			for (int i = 0; i < files.size(); i++) {
				dos.writeUTF(files.get(i).getName());

				dos.flush();
			}

			int n = 0;
			byte[] buf = new byte[4092];
			// outer loop, executes one for each file
			for (int i = 0; i < files.size(); i++) {

				System.out.println(files.get(i).getName());
				// create new fileinputstream for each file
				FileInputStream fis = new FileInputStream(files.get(i));

				// write file to dos
				dos.writeLong(files.get(i).length());
				int bytes = 0;
				while ((n = fis.read(buf)) != -1) {
					dos.write(buf, 0, n);
					dos.flush();
					bytes += n;

				}
				System.out.println("[INFO] Transfer completed, " + bytes + " bytes sent");
				fis.close();
			}
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			servsock = new ServerSocket(SOCKET_PORT);
			while (true) {
				System.out.println("[INFO] Waiting...");
				try {

					socket = servsock.accept();
					System.out.println("[INFO] Accepted connection : " + socket);

					File srcFolder = new File(
							"C:\\Users\\ealexhu\\Desktop\\sockets\\server");

					File[] files = srcFolder.listFiles();

					ArrayList<File> fileList = new ArrayList<File>(
							Arrays.asList(files));

					send(fileList);

					System.out.println("[INFO] Done.");
				} finally {
					if (socket != null)
						socket.close();
				}
			}
		} finally {
			if (servsock != null)
				servsock.close();
		}
	}
}