package chatserver;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

/**The purpose of this class is to parse client's HTTP request, storing requested file
 * and also check whether the request has websocket upgrade, storing the status in a boolean
 * instance variable
 * 
 * @author gongtia
 *
 */
public class ClientRequest {
	/** variable indicating if its a websocket requests **/
	public boolean isWS = false;
	/**
	 * this HashMap is used for storing request's header in order to check if its
	 * websocket request
	 **/
	public HashMap<String, String> headerMap = new HashMap<String, String>();
	/** HTTP request filename **/
	public String fileName;
	/** HTTP request method, here we only deal with GET request **/
	public String method;
	/** HTTP request status **/
	public String protocol;

	/**
	 * This method parse the client http request, sending back file if it's http,
	 * upgrate the protocol if it's websocket requests
	 * 
	 * @param clientSocket -> client's socket connection
	 */
	public ClientRequest(Socket clientSocket) {

		Scanner readClient;

		try {
			readClient = new Scanner(clientSocket.getInputStream());
			method = readClient.next();
			System.out.println(method);
			fileName = readClient.next();
			System.out.println(fileName);
			protocol = readClient.next();
			System.out.println(protocol);
			readClient.nextLine();

			while (true) {
				String line = readClient.nextLine();
				if (line.isEmpty()) {
					break;
				}

				String[] parsedLine = line.split(": ");

				if (parsedLine.length > 1) {
					headerMap.put(parsedLine[0], parsedLine[1]);
				}

			}

			if (headerMap.containsKey("Sec-WebSocket-Key")) {
				System.out.println("isws " + isWS);
				isWS = true;
			}

		} catch (IOException e) {
			System.out.println("unable to get Client Socket inputstream");
		}

	}

}
