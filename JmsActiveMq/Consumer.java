package com.home;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.BlobMessage;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Consumer {

	public static double totalBytes;
	private MessageConsumer consumer;
	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private BufferedOutputStream bos;

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

	private void init() throws Exception {
		// ConnectionFactory connectionFactory = new
		// ActiveMQConnectionFactory("tcp://ec2-54-148-236-202.us-west-2.compute.amazonaws.com:61616");
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		connection = connectionFactory.createConnection();
		connection.start();
		System.out.println("here");
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		destination = session.createQueue("File.Transport");
		consumer = session.createConsumer(destination);
	}

	public void receiveFile(String targetFilePath) throws Exception {
		init();
		while (true) {
			Message message = consumer.receive(1000);
			if (message == null) {
				break;
			}
			if (message instanceof BlobMessage) {
				byte[] buffer = new byte[2048];
				int length = 0;
				BlobMessage blobMessage = (BlobMessage) message;
				String fileName = blobMessage.getStringProperty("FILE.NAME");

				File file = new File(targetFilePath + File.separator + fileName);
				OutputStream os = new FileOutputStream(file);
				bos = new BufferedOutputStream(os);

				InputStream inputStream = blobMessage.getInputStream();
				while ((length = inputStream.read(buffer)) > 0) {
					bos.write(buffer, 0, length);

					totalBytes += (double) length;
				}

				System.out.println("file received");
			}
		}
	}

	private void close() {
		try {
			if (bos != null) {
				bos.close();
			}
			if (connection != null) {
				connection.close();
			}

		} catch (IOException e) {
		} catch (JMSException e) {
		}
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		String targetFileFolder = "C:\\Users\\ealexhu\\Desktop\\sockets\\client\\";
		// String targetFileFolder = "/home/ubuntu/JMSAMQ/client/";
		System.out.println("receiving file");
		Thread t1 = new Thread(new TransferRate());
		t1.start();
		new Consumer().receiveFile(targetFileFolder);
		t1.stop();
	}
}