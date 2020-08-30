import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 

public class ClientHandler extends Thread{
	DataInputStream disReader;
	DataOutputStream dosWriter;
	final Socket serverEndpoint;
	final Server server;

	public ClientHandler (Socket serverEndpoint, Server server) {
		this.serverEndpoint = serverEndpoint;
		this.server = server;
		try {
			disReader = new DataInputStream(serverEndpoint.getInputStream()); 
			dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void run() {

		while (true) {
			
			try {
				
				//broadcast message of handled client to other clients
				String messageType = disReader.readUTF();
				if (messageType.equals("STRING")) {
					String clientMessage = disReader.readUTF();
					server.broadcastString(clientMessage, this);
				}
				else if (messageType.equals("FILE")) {
					//server.broadcastString("file sent", null);
					String filename = disReader.readUTF();
					int fileSize = disReader.readInt();
					byte[] byteArray = new byte[fileSize];
					disReader.read(byteArray, 0, fileSize);
					server.broadcastFile(filename, fileSize, byteArray, this);
				} 

	
				if (messageType.equals("Exit")) {
					System.out.println("Server: Client " + this.serverEndpoint.getRemoteSocketAddress() + " sends exit...");
					System.out.println("Closing this connection."); 
					serverEndpoint.close();
					System.out.println("Connection closed"); 
					break;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			disReader.close();
			dosWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//sends a message to the client
	public void sendMessage (String message) {
		try {
			dosWriter.writeUTF("STRING");
			dosWriter.writeUTF(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFile (String filename, int fileSize, byte[] byteArray) {
		try {
			dosWriter.writeUTF("FILE");
			dosWriter.writeUTF(filename);
			dosWriter.writeInt(fileSize);
			dosWriter.write(byteArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}