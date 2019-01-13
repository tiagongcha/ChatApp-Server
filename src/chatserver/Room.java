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

/**This class is designed for each individual chatroom. It takes advantages of NIO Java so that
 * each room has a selector that can listen to a large number of client sockets connection
 * 
 * @author gongtia
 *
 */
public class Room {
	/**the selector variable allows single thread to handle multipul socket channels
	 */
	private Selector selector;
	/**this instance variable is the Room's name**/
	public String name;
	/**this is a list of existing clients in the room, whose channels the selector listen for **/
	private ArrayList<SocketChannel> allClient = new ArrayList<SocketChannel>();
	/**this is a list of pending clients' request**/
	private ArrayList<SocketChannel> pendingClient = new ArrayList<SocketChannel>();
	/**this list store each chat message in a byte[] array**/
	private ArrayList<byte[]> msgHistory = new ArrayList<byte[]>();

	/**Constructor
	 * 
	 * @param name - chat room's name
	 */
	public Room(String name) {
		this.name = name;

		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**This method is used to keep listening for socketchannel that's ready to 
	 * send data using a selector. The selector allows us to use single thread to listen
	 * for connections from different clients. When a socket channel no longs sends data, selector
	 * can switch to non-blocking mode to listen to other channels that has been registered with it.
	 * 
	 * This method reads data from registered sockets, add it to chat history of the room, and then post 
	 * history messages to all clients in the room
	 * 
	 * Lastly, this method sends history to newly joined clients, and then add them to existing clients list
	 * and allows selector to listen to their channels
	 * @throws IOException
	 */
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
			addPendingClient();
		}
	}
	
	/**This methods sends chat message history to newly joined clients in the room, register them with the
	 * selector, and then add them to existing client list
	 * 
	 * @throws IOException
	 */
	public void addPendingClient() throws IOException {
		for (SocketChannel sc : pendingClient) {
			sc.configureBlocking(true);		 
			sendAll(sc);
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_READ);			
			//add this newly joined client to existing client
			allClient.add(sc);
		}
		pendingClient.clear();
	}
	
	/**This method write all message history to a socket channel data stream
	 * It is also synchronized to prevent concurrent modification
	 * @param sc -> a client's socketChannel
	 * @throws IOException
	 */
	public synchronized void sendAll(SocketChannel sc) throws IOException {
		for (byte[] m : msgHistory) {
			System.out.println("in the for loop " + m.toString());
			sc.socket().getOutputStream().write(m);
			sc.socket().getOutputStream().flush();
		}
	}
	
	/**This method add the client who made a join room request into pending client list
	 * method is declared as synchronized to solve the situation when two clients made the
	 * join request at the same time
	 * @param clientSocketChannel
	 * @throws IOException
	 */
	public synchronized void addClient(SocketChannel clientSocketChannel) throws IOException {
		pendingClient.add(clientSocketChannel);
		selector.wakeup();
	}

	/**This method post the data read from a readable channel to all clients existed in the same room
	 * method is declared as synchronized to solve the situation when two clients' messages both ready to
	 * send out
	 * @param response - a websocket response object instance from WSResponse class
	 * @throws IOException
	 */
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
