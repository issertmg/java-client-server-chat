import java.awt.*;
import java.io.*; 
import java.net.*; 
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.nio.charset.StandardCharsets;

public class Client extends WindowAdapter implements ActionListener {
	File file;
	
	Socket clientEndpoint;
	DataInputStream disReader;
	DataOutputStream dosWriter;

	JFrame frame;
	JButton sendBtn;
	JButton uploadBtn;
	JTextArea messageTa;
	JPanel centerPanel;
	JScrollBar centerPanelScrollBar;
	JButton leaveBtn;

	public Client(String sServerAddress, int nPort) {
		try
        { 
            clientEndpoint = new Socket(sServerAddress, nPort);
			System.out.println("Client: Connecting to server at " + clientEndpoint.getRemoteSocketAddress());
			System.out.println("Client: Connected to server at " + clientEndpoint.getRemoteSocketAddress()); 

            disReader = new DataInputStream(clientEndpoint.getInputStream());
			dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
			
			initGUI();
			listenToServer();
		} catch(Exception e){ 
            e.printStackTrace(); 
        }
	}

	public void listenToServer() {
		while (true) { 
			try {
				JSONObject obj = receiveJSONObject();
				String type = obj.get("type").toString();

				if (type.equals("TEXT")) {
					String message = obj.get("content").toString();
					displayReceivedMessage(message);
				}
				else if (type.equals("FILE")) {
					FileOutputStream fos = new FileOutputStream(file);
					byte[] buffer = decode(obj.get("content").toString());
					fos.write(buffer);
					fos.close();
				}
				else if (type.equals("BUTTON")) {
					String filename = obj.get("filename").toString();
					String numberStr = obj.get("number").toString();
					displayReceivedFile(filename, numberStr);
				}
				else if (type.equals("SENDER_BUTTON")) {
					String filename = obj.get("filename").toString();
					String numberStr = obj.get("number").toString();
					String deliveryStatus = obj.get("delivery_status").toString();
					Boolean isSuccessful = deliveryStatus.equals("SUCCESS");
					displaySentFile(filename, numberStr, isSuccessful);
				}
				else if (type.equals("MESSAGE_STATUS")) {
					String message = obj.get("content").toString();
					String deliveryStatus = obj.get("delivery_status").toString();
					Boolean isSuccessful = deliveryStatus.equals("SUCCESS");
					displaySentMessage(message, isSuccessful);
				}
			} catch (Exception e) {
				break;
			}
        } 
	}

	public void initGUI () {
		frame = new JFrame("De La Salle Usap (DLSU) client");
		frame.setResizable(false);
		sendBtn = new JButton("send");
		sendBtn.setSize(150, 80);
		sendBtn.setFocusable(false);
		messageTa = new JTextArea(3, 1);
		messageTa.setSize(250, 80);
		messageTa.setLineWrap(true);
		JScrollPane messageTaScroll = new JScrollPane(messageTa);

		uploadBtn = new JButton("upload");
		uploadBtn.setSize(150, 80);
		uploadBtn.setFocusable(false);
		leaveBtn = new JButton("disconnect");
		leaveBtn.setSize(150, 80);

		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		northPanel.setBackground(new Color(165,179,197));
		leaveBtn.setBackground(new Color(71,105,153));
		leaveBtn.setForeground(Color.WHITE);
		leaveBtn.setFocusable(false);
		northPanel.add(leaveBtn);
		frame.add(northPanel, BorderLayout.NORTH);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(uploadBtn, BorderLayout.WEST);
		southPanel.add(messageTaScroll, BorderLayout.CENTER);
		southPanel.add(sendBtn, BorderLayout.EAST);
		frame.add(southPanel, BorderLayout.SOUTH);

		centerPanel = new JPanel(new WrapLayout());
		centerPanel.setBackground(new Color(219,226,237));
	
		JScrollPane scrollCenterPanel = new JScrollPane(centerPanel);
		centerPanelScrollBar = scrollCenterPanel.getVerticalScrollBar();
		frame.add(scrollCenterPanel, BorderLayout.CENTER);
		
		frame.setSize(400, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addActionListeners();
	}

	public void addActionListeners() {
		sendBtn.addActionListener((ActionListener) this);
		uploadBtn.addActionListener((ActionListener) this);
		leaveBtn.addActionListener((ActionListener) this);
		frame.addWindowListener(this);
	}

	public void actionPerformed (ActionEvent ae) {
		if (ae.getActionCommand() == "send") {
			try {
				JSONObject obj = new JSONObject();
				obj.put("type", "TEXT");
				obj.put("content", messageTa.getText());
				writeJSON(obj);
				messageTa.setText("");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (ae.getActionCommand() == "disconnect") {
			System.exit(0);
		}
		else if (ae.getActionCommand() == "upload") {
			try {
				JFileChooser jfc = new JFileChooser();
				int r = jfc.showSaveDialog(null);

				if (r == JFileChooser.APPROVE_OPTION) {
					File uploadFile = jfc.getSelectedFile();
					FileInputStream fis = new FileInputStream(uploadFile);
					byte[] byteArray = new byte [(int)uploadFile.length()];
					fis.read(byteArray);
					String dataString = encode(byteArray);
					fis.close();

					JSONObject obj = new JSONObject();
					obj.put("type", "FILE");
					obj.put("content", dataString);
					obj.put("filename", uploadFile.getName());

					writeJSON(obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			String[] actionCommand = ae.getActionCommand().split("\\?");
			
			int filenumber = Integer.parseInt(actionCommand[0]);
			try {
				JFileChooser jfc = new JFileChooser();
				File newfile = new File(actionCommand[1]);
				jfc.setSelectedFile(newfile);

				int r = jfc.showSaveDialog(null);

				if (r == JFileChooser.APPROVE_OPTION) {
					file = jfc.getSelectedFile();

					JSONObject obj = new JSONObject();
					obj.put("type", "DOWNLOAD");
					obj.put("number", filenumber);
					
					writeJSON(obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void displaySentFile(String filename, String numberStr, Boolean isSuccessful) {
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		messagePanel.setOpaque(false);
		JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT));
		container.setBackground(new Color(149,215,73));
		JLabel fileLabel = new JLabel(filename);
		JButton downloadBtn = new JButton("download");
		downloadBtn.setActionCommand(numberStr + "?" + filename);
		downloadBtn.addActionListener((ActionListener) this);
		container.add(fileLabel);
		container.add(downloadBtn);

		if (isSuccessful == false) {
			container.setBackground(new Color(168,168,168));
			container.remove(downloadBtn);
			container.add(new JLabel(new ImageIcon("error.png")));
			updateGUI();
		}

		messagePanel.add(container);
		centerPanel.add(messagePanel);
		messagePanel.setPreferredSize(new Dimension (350, messagePanel.getPreferredSize().height));
		updateGUI();
		messagePanel.setPreferredSize(new Dimension (350, messagePanel.getPreferredSize().height));
		updateGUI();
		centerPanelScrollBar.setValue(centerPanelScrollBar.getMaximum());
		updateGUI();
	}

	public void displayReceivedFile(String filename, String numberStr) {
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		messagePanel.setOpaque(false);
		JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT));
		container.setBackground(new Color(210,210,210));
		JLabel fileLabel = new JLabel(filename);
		JButton downloadBtn = new JButton("download");
		downloadBtn.setActionCommand(numberStr + "?" + filename);
		downloadBtn.addActionListener((ActionListener) this);

		container.add(fileLabel);
		container.add(downloadBtn);
		messagePanel.add(container);
		centerPanel.add(messagePanel);
		messagePanel.setPreferredSize(new Dimension (350, messagePanel.getPreferredSize().height));
		updateGUI();
		messagePanel.setPreferredSize(new Dimension (350, messagePanel.getPreferredSize().height));
		updateGUI();
		centerPanelScrollBar.setValue(centerPanelScrollBar.getMaximum());
		updateGUI();
	}

	public void displaySentMessage (String msg, boolean isSuccessful) {
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		messagePanel.setOpaque(false);
		JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT));
		container.setBackground(new Color(149,215,73));
		JTextArea message = new JTextArea(msg);
		message.setColumns(16);
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		message.setEditable(false);
		message.setOpaque(false);
		container.add(message);

		if (isSuccessful == false) {
			container.setBackground(new Color(168,168,168));
			messagePanel.add(new JLabel(new ImageIcon("error.png")));
			updateGUI();
		}
			
		messagePanel.add(container);
		centerPanel.add(messagePanel);
		updateGUI();
		messagePanel.setPreferredSize(new Dimension (350, messagePanel.getPreferredSize().height));
		updateGUI();
		container.setPreferredSize(new Dimension (175, container.getPreferredSize().height));
		centerPanelScrollBar.setValue(centerPanelScrollBar.getMaximum());
		updateGUI();
	}

	public void updateGUI() {
		frame.revalidate();
		frame.repaint();
	}

	public void displayReceivedMessage(String msg) {
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		messagePanel.setOpaque(false);
		JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT));
		container.setBackground(new Color(210,210,210));
		JTextArea message = new JTextArea(msg);
		message.setColumns(16);
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		message.setOpaque(false);
		message.setEditable(false);
		message.setBackground(new Color(210,210,210));
		container.add(message);
		messagePanel.add(container);
		centerPanel.add(messagePanel);		
		updateGUI();
		messagePanel.setPreferredSize(new Dimension (350, messagePanel.getPreferredSize().height));
		updateGUI();
		container.setPreferredSize(new Dimension (175, container.getPreferredSize().height));
		centerPanelScrollBar.setValue(centerPanelScrollBar.getMaximum());
		updateGUI();
	}

	public void windowClosing(WindowEvent evt) {
		try {
			System.out.println("Closing this connection : " + clientEndpoint); 
            clientEndpoint.close(); 
            System.out.println("Connection closed"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public byte[] decode (String dataString) {
		return Base64.getDecoder().decode(dataString);
	}

	public String encode (byte[] byteArray) {
		return Base64.getEncoder().encodeToString(byteArray);
	}

	public void writeJSON(JSONObject obj) throws IOException {
		byte[] buffer = obj.toJSONString().getBytes(StandardCharsets.UTF_8);
		dosWriter.writeInt(buffer.length);
		dosWriter.write(buffer);
	}

	public JSONObject receiveJSONObject() throws IOException {
		int nsize = disReader.readInt();
		byte[] buffer = new byte[nsize];
		disReader.readFully(buffer, 0, nsize);
		String objString = new String(buffer, StandardCharsets. UTF_8);
		return (JSONObject) JSONValue.parse(objString);
	}

	public static void main (String[] args) {
		
		JTextField serverAddressTF = new JTextField(5);
		JTextField portTF = new JTextField(5);
		
		JPanel centerPanel = new JPanel(new GridLayout(2,2));
		centerPanel.add(new JLabel("Server address: "));
		centerPanel.add(serverAddressTF);
		centerPanel.add(new JLabel("Port number: "));
		centerPanel.add(portTF);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(centerPanel, BorderLayout.CENTER);

		do {
			int result = JOptionPane.showConfirmDialog(null, panel, 
			"Configure", JOptionPane.OK_CANCEL_OPTION);

			if (result != JOptionPane.OK_OPTION) {
				System.exit(0);
			}

		}
		while (serverAddressTF.getText().isEmpty() || !portTF.getText().matches("[0-9]+"));

		String sServerAddress = serverAddressTF.getText();
		int nPort = Integer.parseInt(portTF.getText());
	
		Client client = new Client (sServerAddress, nPort);
	}
}