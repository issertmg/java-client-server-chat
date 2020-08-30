import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 

public class Server {
	int nPort;
	Set<ClientHandler> clientHandlers;

	public Server(int nPort) {
		this.nPort = nPort;
		clientHandlers = new HashSet<>();	
	}

	public void execute () {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(nPort);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			Socket serverEndpoint = null;
			try {
				System.out.println("Server: Listening on port " + nPort + "...");
				
				//socket object to receive incoming client requests
				serverEndpoint = serverSocket.accept();
				System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());

				ClientHandler t = new ClientHandler(serverEndpoint, this);
				clientHandlers.add(t);
				t.start();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	// public static void main (String[] args) {
	// 	int nPort = Integer.parseInt(args[0]);
	// 	Server server = new Server(nPort);
	// 	server.execute();
	// }

	//broadcast message to users
	public void broadcastString(String message, ClientHandler excludeUser) {
        for (ClientHandler client : clientHandlers) {
            if (client != excludeUser) {
                client.sendMessage(message);
            }
        }
	}
	
	public void broadcastFile(int fileSize, byte[] byteArray, ClientHandler excludeUser) {
        for (ClientHandler client : clientHandlers) {
            if (client != excludeUser) {
                client.sendFile(fileSize, byteArray);
            }
        }
	}
	
}