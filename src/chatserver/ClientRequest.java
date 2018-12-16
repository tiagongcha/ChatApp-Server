package chatserver;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class ClientRequest {
	public boolean isWS = false;
	public HashMap<String,String > headerMap = new HashMap<String,String>();
	public String fileName;
	public String method;
	public String protocol;
	public boolean fileExist = false;
	
	
	 public ClientRequest(Socket clientSocket) {
		
		Scanner readClient;
		
		try {
			 readClient = new Scanner(clientSocket.getInputStream());
			 method = readClient.next();
			 fileName = readClient.next();
			 protocol = readClient.next();
			 readClient.nextLine();  
			          		
			while(true) {
				String line = readClient.nextLine();
				if(line.isEmpty()) {
					break;
				}
				
				String[] parsedLine = line.split(": ");
				
				if(parsedLine.length > 1) {
					headerMap.put(parsedLine[0],parsedLine[1]);	
				}
					
			}
						
			if(headerMap.containsKey("Sec-WebSocket-Key")) {
				System.out.println("isws "+ isWS);
				isWS = true;
			}
				 			
		} catch (IOException e) {
			System.out.println("unable to get Client Socket inputstream");
		}			
			
	}

}
