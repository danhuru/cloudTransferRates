package com.home;

import java.io.File;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.BlobMessage;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Producer {

	private Connection connection = null;
	private ActiveMQSession session = null;
	private Destination destination = null;
	private MessageProducer producer = null;

	private void init() throws Exception {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616?jms.blobTransferPolicy.defaultUploadUrl=http://localhost:8161/fileserver/");

		connection = connectionFactory.createConnection();
		session = (ActiveMQSession) connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		destination = session.createQueue("File.Transport");

		producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		connection.start();
	}

	public void sendFile(String fileName) throws Exception {
		System.out.println("Send File Start >>");
		init();
		File file = new File(fileName);

		BlobMessage blobMessage = session.createBlobMessage(file);
		blobMessage.setStringProperty("FILE.NAME", file.getName());
		blobMessage.setLongProperty("FILE.SIZE", file.length());
		System.out.println("File size: " + file.length());
		producer.send(blobMessage);
		System.out.println("Send File End>>");
	}

	private void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (JMSException e) {
		}
		System.exit(0);
	}

	public static void main(String argv[]) throws Exception {
		String fileName = "C:\\Users\\ealexhu\\Desktop\\sockets\\server\\Norvegia.mp4";
		// String fileName = "/home/ubuntu/JMSAMQ/server/file1";
		System.out.println("sending file..." + fileName);
		new Producer().sendFile(fileName);
	}
}