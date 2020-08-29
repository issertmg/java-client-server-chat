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
				String clientMessage = disReader.readUTF();
				server.broadcast(clientMessage, this);
	
				if (clientMessage.equals("Exit")) {
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
			dosWriter.writeUTF(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}