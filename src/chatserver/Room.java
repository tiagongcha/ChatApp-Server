package chatserver;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Room {

	private Selector selector;
	public String name;
	private ArrayList<SocketChannel> allClient = new ArrayList<SocketChannel>();
	private ArrayList<SocketChannel> pendingClient = new ArrayList<SocketChannel>();
	private ArrayList<byte[]> msgHistory = new ArrayList<byte[]>();

	public Room() {

		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Room(String name) {
		this.name = name;

		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void listenClient() throws IOException {

		while (true) {
			selector.select();
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();

				if (key.isReadable()) {
					keyIterator.remove();
					SocketChannel clientSocketChannel = (SocketChannel) key.channel();
					key.cancel();
					clientSocketChannel.configureBlocking(true);
					
					//read the message this client send and add the json bytes arr (with header) to history
					WSResponse response = new WSResponse();
					response.readMessage(clientSocketChannel.socket());
					msgHistory.add(response.getJsonBytes());

					clientSocketChannel.configureBlocking(false);
					selector.selectNow();
					clientSocketChannel.register(selector, SelectionKey.OP_READ);

					//post this msg to all clients in the room
					post(response);			
				}
			}


			for (SocketChannel sc : pendingClient) {
				sc.configureBlocking(true);
				
				//send the msg history of this room to newly joined clients 
				sendAll(sc);
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
				
				//add this newly joined client to existing client
				allClient.add(sc);
			}
			pendingClient.clear();
		}
	}
	
	
	public synchronized void sendAll(SocketChannel sc) throws IOException {
		for (byte[] m : msgHistory) {
			System.out.println("in the for loop " + m.toString());
			sc.socket().getOutputStream().write(m);
			sc.socket().getOutputStream().flush();
		}
	}

	
	public synchronized void addClient(SocketChannel clientSocketChannel) throws IOException {
		pendingClient.add(clientSocketChannel);
		selector.wakeup();
	}

	
	public synchronized void post(WSResponse response) throws IOException {

		for (SocketChannel sc : allClient) {
			SelectionKey key = sc.keyFor(selector);
			key.cancel();
			sc.configureBlocking(true);

			// send the coming client's msg to existing clients
			response.sendMessage(sc.socket().getOutputStream());

			sc.configureBlocking(false);
			selector.selectNow();
			sc.register(selector, SelectionKey.OP_READ);
		}
	}

}
