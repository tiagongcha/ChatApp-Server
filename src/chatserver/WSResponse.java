package chatserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * This class is designed to handle low-level websocket request: send back
 * websocket handshake, extract websocket data frame, and then echo message back
 * to client in JSON format
 * 
 * @author gongtia
 *
 */
public class WSResponse {
	
	/**This variable is byte array of message after being decoded**/
	private byte[] decoded;
	/**header message of websocket message data frame**/
	private byte[] headerMsg;
	/**This variable stores client's name they picked**/
	private String name;
	/**This variable stores client's chat message**/
	private String message;
	/**binary data after adding json format**/
	private byte[] jsonBytes;

	/**get instance variable jsonBytes**/
	public byte[] getJsonBytes() {
		return jsonBytes;
	}
	
	/**get instance variable decoded byte array**/
	public byte[] getDecoded() {
		return decoded;
	}

	/**Parse room name from decoded message**/
	public String getRoomName() {
		String s = new String(decoded);
		System.out.println(s);
		int i = s.indexOf(" ");
		if (s.substring(0, i).equals("join"))
			;
		String roomName = s.substring(i, s.length());
		return roomName;
	}

	public static void handshakeBack(ClientRequest request, Socket clientSocket) {

		try {
			OutputStream toClient = clientSocket.getOutputStream();
			toClient.write(("HTTP/1.1 101 Switching Protocols\r\n" + "Upgrade: websocket\r\n"
					+ "Connection: Upgrade\r\n" + "Sec-WebSocket-Accept: ").getBytes());

			String wsValue = request.headerMap.get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			String encode = Base64.getEncoder().encodeToString(md.digest(wsValue.getBytes()));
			toClient.write((encode + "\r\n\r\n").getBytes());

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("unable to write handshake header to client");
		}
	}

	public void readMessage(Socket clientSocket) {
		try {
			DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());

			headerMsg = new byte[2];
			dataIn.read(headerMsg, 0, 2);

			byte secondByte = (byte) (headerMsg[1] & (byte) 127);
			int payloadLen = 0;

			byte[] extraLen;
			if (secondByte <= 125) {
				payloadLen = secondByte;
			} else if (secondByte == 126) {
				extraLen = new byte[2];
				dataIn.read(extraLen, 0, 2);
				payloadLen = (extraLen[0] << 8) | extraLen[1];
			} else if (secondByte == 127) {
				extraLen = new byte[8];
				for (int i = 0; i < extraLen.length; i++) {
					payloadLen = (payloadLen << 8) + (extraLen[i] & 0xff);
				}
			}

			byte[] key = new byte[4];
			dataIn.read(key, 0, 4);

			byte[] encoded = new byte[payloadLen];
			dataIn.read(encoded);

			decoded = new byte[payloadLen];
			for (int i = 0; i < payloadLen; i++) {
				decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
			}

			String s = new String(decoded);
			String[] split = s.split("\\s+", 2);
			if (split.length > 1) {
				name = split[0];
				System.out.println(name + ":");
				message = split[1];
				System.out.println(message);
			}
			byte[] temp = getJson().getBytes();
			jsonBytes = new byte[2 + temp.length];
			jsonBytes[0] = headerMsg[0];
			jsonBytes[1] = (byte) temp.length;
			for (int i = 0; i < temp.length; i++) {
				jsonBytes[i + 2] = temp[i];
			}

		} catch (IOException e) {
			System.out.println("unable to read client websocket msg");
		}
	}

	public void sendMessage(OutputStream toClient) throws IOException {
		toClient.write(jsonBytes);
		toClient.flush();
	}

	public String getJson() {
		return "{ \"user\" : \"" + this.name + "\", \"message\" : \"" + this.message + "\" }";
	}

}
