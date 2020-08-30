import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.net.*; 

public class Server {
	int nPort;
	Set<ClientHandler> clientHandlers;
	JButton saveBtn;
	JTable table;
	JScrollBar scrollBar;
	JFrame frame;
	DefaultTableModel model;

	public Server(int nPort) {
		this.nPort = nPort;
		clientHandlers = new HashSet<>();
		init();	
	}

	public void init () {
		frame = new JFrame();
		frame.setResizable(false);
		saveBtn = new JButton("save log");
		//sendBtn.setSize(150, 80);
		
		frame.add(saveBtn, BorderLayout.SOUTH);

		String column[] = {"Timestamp", "Source", "Destination", "Activity"};
		model = new DefaultTableModel(column, 0);
		table = new JTable(model);

		// Object[] row = {"data1", "data2" , "data3", "data4"};
		// model.addRow( row );
		JScrollPane scrollpane = new JScrollPane(table);
		scrollBar = scrollpane.getVerticalScrollBar();
		frame.add(scrollpane, BorderLayout.CENTER);
		
		frame.setSize(400, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//addActionListeners();
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
	
	public void broadcastFile(String filename, int fileSize, byte[] byteArray, ClientHandler excludeUser) {
        for (ClientHandler client : clientHandlers) {
            if (client != excludeUser) {
                client.sendFile(filename, fileSize, byteArray);
            }
        }
	}
	
}