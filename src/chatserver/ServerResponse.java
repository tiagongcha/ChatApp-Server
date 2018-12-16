package chatserver;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;



public class ServerResponse {
	
 public ClientRequest request;
	public File file;
	
	
	
	public ServerResponse(Socket clientSocket) {
		
		request = new ClientRequest(clientSocket);
		String header;
		
		try {
			OutputStream toClient = clientSocket.getOutputStream();
			
			if(request.isWS) {
						//handshake header:
						toClient.write(("HTTP/1.1 101 Switching Protocols\r\n"
			            + "Upgrade: websocket\r\n" + "Connection: Upgrade\r\n" 
			            + "Sec-WebSocket-Accept: ").getBytes());
												
							String wsValue = request.headerMap.get("Sec-WebSocket-Key")+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
							System.out.println("map "+request.headerMap.get("Sec-WebSocket-Key"));
							try {
								MessageDigest md = MessageDigest.getInstance("SHA-1");
								String encode = Base64.getEncoder().encodeToString(md.digest(wsValue.getBytes()));
								
								toClient.write((encode + "\r\n\r\n").getBytes());
								
							} catch (NoSuchAlgorithmException e) {
								
							    System.out.println("msg digest wrong");
							} 
						
							
						DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
						
						while(true) {
							byte[] msg = new byte[2];
							dataIn.read(msg,0,2);						
							int secondByte = msg[1]&0xff;
//							System.out.println("first byte unsign "+ (msg[0]&0xff));
//							System.out.println("second byte unsign "+ secondByte);
					
							int payloadLen = 0;							
							if((secondByte - 128) <= 125) {
								
								payloadLen = secondByte - 128;
								
							}else if((secondByte - 128) == 126 ) {
								//add stuff later!!!!!!
							}else if((secondByte - 128) == 127){
								//add stuff later!!!!!!
							}
//							System.out.println("length is "+payloadLen);
							
							byte[] key = new byte[4];
							dataIn.read(key,0,4);
							
							if(payloadLen>0) {
							byte[] decoded = new byte[payloadLen];
							byte[] encoded = new byte[payloadLen];
							dataIn.read(encoded);
							
							for(int i = 0; i < payloadLen; i++) {
								decoded[i] = (byte)(encoded[i] ^ key[i & 0x3]);			
							}
							String s = new String(decoded);
							System.out.println("s is" + s);
							
							//parse string s to user and message:
							int i = s.indexOf(" ");
							String name = s.substring(0, i);
							String message = s.substring(i, s.length());
							
							
	   					    System.out.println(name + "...." + message);

							
							byte[] firstTwo = new byte[2];		
							firstTwo[0] = msg[0];						
							firstTwo [1] = (byte) (msg[1]&(byte) 127);
			
							String jsonMsg = "{ \"user\" : \"" + name + "\", \"message\" : \"" + message + "\"}";
							System.out.println(jsonMsg);
							toClient.write(firstTwo);
//							toClient.write(decoded);
							toClient.write(jsonMsg.getBytes());
							
							toClient.flush();
							}
						}
				
			}else {
				file = new File("./resources" + request.fileName);
				
				if(file.exists()) {
					
					FileInputStream fis = new FileInputStream(file);
    				BufferedInputStream bin = new BufferedInputStream(fis);
					
					header = "HTTP/1.1 200 OK\r\n";
					toClient.write(header.getBytes());

					String contentLength = "Content-Length: " + file.length();
					toClient.write(contentLength.getBytes());
					toClient.write("\r\n\r\n".getBytes());
					
					byte[] content =  new byte[(int) file.length()];
					bin.read(content);
					toClient.write(content);			
					
				}
				
			}		
			
			
		} 
		
			catch (IOException e) {
			System.out.println("unable to write to client");
		}
	}
	
	
	

}
