package chatserver;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.lang.*;

public class MyServer {

	private static HashMap<String, Room> chatRooms = new HashMap<String, Room>();

	public static void main(String[] args) throws IOException {
		
		//create the server socket connection:
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(8080));
		System.out.println("socket created on port 8080");
		serverSocketChannel.configureBlocking(true);

		while (true) {
			//create the client socket connection:
			SocketChannel clientSocketChannel = serverSocketChannel.accept();

			//make a new thread every time 
			Thread th = new Thread(new Runnable() {

				public void run() {
					ClientRequest request = new ClientRequest(clientSocketChannel.socket());

					if (!request.isWS) {
						HTTPResponse http = new HTTPResponse(request, clientSocketChannel.socket());

						try {
							clientSocketChannel.socket().close();
						} catch (IOException e) {

							e.printStackTrace();
						}
					} else {

						WSResponse.handshakeBack(request, clientSocketChannel.socket());
						System.out.println("after handshake....");

						// read the first join...room message
						WSResponse response = new WSResponse();
						response.readMessage(clientSocketChannel.socket());

						if (chatRooms.containsKey(response.getRoomName())) {
							System.out.println("ENTER EXISTING");
							try {
								chatRooms.get(response.getRoomName()).addClient(clientSocketChannel);
								System.out.println("room existed name is" + response.getRoomName());

							} catch (IOException e) {
								System.out.println("map cant get key");
							}
						}

						else {
							Room chatroom = new Room(response.getRoomName());
							try {
								chatRooms.put(response.getRoomName(), chatroom);
								chatroom.addClient(clientSocketChannel);
								chatroom.listenClient();
							}

							catch (IOException e) {
								e.printStackTrace();
							}
						}

					}

				}

			});
			th.start();
		}
	}

}
