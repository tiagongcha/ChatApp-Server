package chatserver;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HTTPResponse {
//	public ClientRequest request;
	public File file;
	
	public HTTPResponse(ClientRequest request, Socket clientSocket) {
//		request = new ClientRequest(clientSocket);
		String header;
		
		try {
			OutputStream toClient = clientSocket.getOutputStream();
			
			
				file = new File("./resources" + request.fileName);
				
				if(file.exists()) {
//					System.out.println("file exist");
					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bin = new BufferedInputStream(fis);
					
					header = "HTTP/1.1 200 OK\r\n";
					toClient.write(header.getBytes());
//					System.out.println(header.getBytes());
					String contentLength = "Content-Length: " + file.length();
					toClient.write(contentLength.getBytes());
					toClient.write("\r\n\r\n".getBytes());
					
					byte[] content =  new byte[(int) file.length()];
					bin.read(content);
					toClient.write(content);	
//					System.out.println(content);
				}
				
				else {
					 header = "HTTP/1.1 404 Not Found";
					 toClient.write(header.getBytes());
				}
			
		} 
		
		catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}
}
	
