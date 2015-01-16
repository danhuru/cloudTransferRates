import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerM {

	public final static int SOCKET_PORT = 13268; // you may change this
	public static String srcFolder = "C:\\Users\\ealexhu\\Desktop\\sockets\\server\\";

	class Server2Connection implements Runnable {
		Socket clientSocket;
		int id;
		ServerM server;

		public Server2Connection(Socket clientSocket, int id, ServerM server) {
			this.clientSocket = clientSocket;
			this.id = id;
			this.server = server;
			System.out.println("Connection " + id + " established with: "
					+ clientSocket);
		}

		public void run() {
			try {
				ArrayList<File> fileList = new ArrayList<File>();
				DataInputStream dis = new DataInputStream(
						new BufferedInputStream(clientSocket.getInputStream()));
				File fileToDownload = new File(srcFolder.concat(dis.readUTF()));
				System.out.println(fileToDownload.getPath());

				fileList.add(fileToDownload);

				DataOutputStream dos = new DataOutputStream(
						new BufferedOutputStream(clientSocket.getOutputStream()));

				System.out.println(fileList.size());
				// write the number of files to the server
				dos.writeInt(fileList.size());
				dos.flush();

				// write file names
				for (int i = 0; i < fileList.size(); i++) {
					dos.writeUTF(fileList.get(i).getName());

					dos.flush();
				}

				int n = 0;
				byte[] buf = new byte[4092];
				// outer loop, executes one for each file
				for (int i = 0; i < fileList.size(); i++) {

					System.out.println(fileList.get(i).getName());
					// create new fileinputstream for each file
					FileInputStream fis = new FileInputStream(fileList.get(i));

					// write file to dos
					dos.writeLong(fileList.get(i).length());
					int bytes = 0;
					while ((n = fis.read(buf)) != -1) {
						dos.write(buf, 0, n);
						dos.flush();
						bytes += n;

					}
					System.out.println("[INFO] Transfer completed, " + bytes
							+ " bytes sent");
					fis.close();
				}
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// declare a server socket and a client socket for the server;
	// declare the number of connections

	ServerSocket echoServer = null;
	Socket clientSocket = null;
	int numConnections = 0;
	int port;

	public ServerM(int port) {
		this.port = port;
	}

	public void startServer() {
		try {
			echoServer = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println(e);
		}
		System.out.println("Server is started and is waiting for connections.");
		while (true) {
			try {
				clientSocket = echoServer.accept();
				System.out.println("[INFO] Accepted connection : "
						+ clientSocket.getPort());
				numConnections++;
				Server2Connection oneconnection = new Server2Connection(
						clientSocket, numConnections, this);
				new Thread(oneconnection).start();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
	public static void main(String args[]) {
		ServerM server = new ServerM(SOCKET_PORT);
		server.startServer();
	}
}
