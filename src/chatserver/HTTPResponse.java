package chatserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**This class is designed to handle HTTP Request, sending back the file client has been requested
 * 
 * @author gongtia
 *
 */
public class HTTPResponse {
	public File file;

	/**Constructor
	 * 
	 * @param request -> instance object of Client Request class, containing parsed info of a client' HTTP request
	 * @param clientSocket -> the client socket to write data to
	 */
	public HTTPResponse(ClientRequest request, Socket clientSocket) {
		String header;

		try {
			OutputStream toClient = clientSocket.getOutputStream();

			file = new File("./resources" + request.fileName);
			System.out.println(file);
			if (file.exists()) {
				System.out.println("file found");
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bin = new BufferedInputStream(fis);

				header = "HTTP/1.1 200 OK\r\n";
				toClient.write(header.getBytes());
				String contentLength = "Content-Length: " + file.length();
				toClient.write(contentLength.getBytes());
				toClient.write("\r\n\r\n".getBytes());

				byte[] content = new byte[(int) file.length()];
				bin.read(content);
				toClient.write(content);
			}
			else {
				System.out.println("file not found");
				header = "HTTP/1.1 404 Not Found";
				toClient.write(header.getBytes());
			}
		}
		catch (IOException e) {

			e.printStackTrace();
		}

	}
}
