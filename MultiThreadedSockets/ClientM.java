import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientM {

	public final static int SOCKET_PORT = 13268; // you may change this
	// public final static String SERVER = //
	// "ec2-54-148-236-202.us-west-2.compute.amazonaws.com"; // ubuntu aws
	public final static String SERVER = "127.0.0.1"; // localhost
	// public final static String SERVER = "ubuntudanhuru.cloudapp.net"; //
	// azure
	public final static String DEST_DIR = "C:\\Users\\ealexhu\\Desktop\\sockets\\client\\"; // localhost
	public static double totalBytes;

	static class TransferRate implements Runnable {

		public void run() {
			long startTime = System.currentTimeMillis();
			totalBytes = 0;
			System.out.println("start time: " + startTime);
			System.out.println("Thread started.");
			try {
				while (true) {
					long start = System.currentTimeMillis();
					double initialBytes = totalBytes;
					Thread.sleep(1000);
					double currentBytes = totalBytes;
					long end = System.currentTimeMillis();
					System.out
							.printf("[FR] Read %,f MBytes, speed: %,f MB/s%n",
									(double) (currentBytes - initialBytes) / 1000 / 1000,
									(double) (currentBytes - initialBytes)
											/ (end - start) / 1000);
					long currentTime = System.currentTimeMillis();
					System.out
							.printf("[TR] Total read: %s Mbytes, average speed: %,f MB/s%n",
									new DecimalFormat("#.000")
											.format(totalBytes / 1000 / 1000),
									(double) totalBytes
											/ (currentTime - startTime) / 1000);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println("Thread Finished.");
		}
	}

	static FileOutputStream fos = null;
	static Socket socket = null;

	static void send(String fileName) {
		try {
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(socket.getOutputStream()));
			dos.writeUTF(fileName);
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void receive() {
		try {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					socket.getInputStream()));

			// read the number of files from the client
			int number = dis.readInt();
			ArrayList<File> files = new ArrayList<File>(number);
			System.out.println("[INFO] Number of Files to be received: "
					+ number);
			// read file names, add files to arraylist
			for (int i = 0; i < number; i++) {
				File file = new File(dis.readUTF());
				files.add(file);
			}
			int n = 0;
			byte[] buf = new byte[4096];

			// outer loop, executes one for each file
			for (int i = 0; i < files.size(); i++) {

				System.out.println("[INFO] Receiving file: "
						+ files.get(i).getName());
				// create a new fileoutputstream for each new file

				FileOutputStream fos = new FileOutputStream(DEST_DIR
						+ files.get(i).getName());

				int bytes = 0;

				long fileSize = dis.readLong();
				while (fileSize > 0
						&& (n = dis.read(buf, 0,
								(int) Math.min(buf.length, fileSize))) != -1) {
					fos.write(buf, 0, n);
					bytes += n;
					totalBytes += n;
					// fos.flush();
					fileSize -= n;
				}
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			socket = new Socket(SERVER, SOCKET_PORT);
			System.out.println(new Date());
			System.out.println("Connecting...");

			send(args[0]);

			Thread t1 = new Thread(new TransferRate());
			t1.start();
			receive();
			t1.stop();
			System.out.println(new Date());
		} finally {
			if (socket != null)
				socket.close();
		}
	}

}