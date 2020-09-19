import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.net.*; 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;

public class Server extends WindowAdapter implements ActionListener {
	int nPort;
	Set<ClientHandler> clientHandlers;
	JButton saveBtn;
	JTable table;
	JScrollBar scrollBar;
	JFrame frame;
	DefaultTableModel model;
	ArrayList<JSONObject> jsonList;


	public Server(int nPort) {
		this.nPort = nPort;
		clientHandlers = new HashSet<>();
		jsonList = new ArrayList<JSONObject>();
		init();	
	}

	public void init () {
		frame = new JFrame("De La Salle Usap (DLSU) Server");
		frame.setResizable(false);
		saveBtn = new JButton("save log");
		//sendBtn.setSize(150, 80);
		
		frame.add(saveBtn, BorderLayout.SOUTH);

		String column[] = {"Timestamp", "Source", "Destination", "Activity"};
		model = new DefaultTableModel(column, 0);
		table = new JTable(model);

		JScrollPane scrollpane = new JScrollPane(table);
		scrollBar = scrollpane.getVerticalScrollBar();
		frame.add(scrollpane, BorderLayout.CENTER);

		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		northPanel.setBackground(new Color(71,105,153));
		JLabel label = new JLabel("Test: dfdf");
		label.setForeground(Color.WHITE);
		northPanel.add(label);
		frame.add(northPanel, BorderLayout.NORTH);
		
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addActionListeners();
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
				String source = serverEndpoint.getRemoteSocketAddress().toString();
				addToActivityLog(source, "", "Client connected");

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
	public void broadcastString(JSONObject obj, ClientHandler excludeUser) {
		String source = excludeUser.serverEndpoint.getRemoteSocketAddress().toString();

		if (clientHandlers.size() == 1) {
			addToActivityLog(source, "", "Message sending failed");
		} 
		else {
			for (ClientHandler client : clientHandlers) {
				if (client != excludeUser) {
					client.sendMessage(obj);
					String destination = client.serverEndpoint.getRemoteSocketAddress().toString();
					addToActivityLog(source, destination, "Client sent a message");
					addToActivityLog(source, destination, "Client received a message");
				}
			}
		}
	}
	
	public void broadcastFile(JSONObject obj, String filename, ClientHandler excludeUser) {
		String source = excludeUser.serverEndpoint.getRemoteSocketAddress().toString();
		int number = jsonList.size();
		jsonList.add(obj);

		if (clientHandlers.size() == 1) {
			addToActivityLog(source, "", "File sending failed");
			excludeUser.sendSenderButton(filename, number);
		}
		else {
			for (ClientHandler client : clientHandlers) {
				if (client != excludeUser) {
					client.sendButton(filename, number);
					String destination = client.serverEndpoint.getRemoteSocketAddress().toString();
					addToActivityLog(source, destination, "Client sent a file");
					addToActivityLog(source, destination, "Client received a file");
				}
			}
		}
	}

	public void removeClientHander(ClientHandler user) {
		String source = user.serverEndpoint.getRemoteSocketAddress().toString();
		clientHandlers.remove(user);
		addToActivityLog(source, "", "Client disconnected");
	}

	public void addToActivityLog(String source, String destination, String activity) {
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-dd-yyyy HH:mm:ss");
		String timestamp = dateTime.format(formatter);

		Object[] row = { timestamp, source , destination, activity };
		model.addRow(row);
	}

	public void actionPerformed (ActionEvent ae) {
		saveLogFile();
	}

	public void windowClosing(WindowEvent evt) {
		int result = JOptionPane.showConfirmDialog(frame,"Do you want to save log as 'server_log.txt'?", "Closing DLSUsap",
		JOptionPane.YES_NO_OPTION,
		JOptionPane.QUESTION_MESSAGE);

		if(result == JOptionPane.YES_OPTION){
			saveLogFile();
		}	
	}

	public void addActionListeners() {
		saveBtn.addActionListener((ActionListener) this);
		frame.addWindowListener(this);
	}

	public void saveLogFile() {
		File logFile = new File("server_log.txt");

		try {
			PrintWriter os = new PrintWriter(logFile);
			os.println("Server Log");
	
			for (int row = 0; row < table.getRowCount(); row++) {
				os.println("");
				os.println("Timestamp: " + table.getValueAt(row, 0));
				os.println("Source: " + table.getValueAt(row, 1));
				os.println("Destination: " + table.getValueAt(row, 2));
				os.println("Activity: " + table.getValueAt(row, 3));
			}
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}