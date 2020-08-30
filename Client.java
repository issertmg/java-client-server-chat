import java.awt.*;
import java.io.*; 
import java.net.*; 
import javax.swing.*;
import java.awt.event.*;

public class Client implements ActionListener {
	DataInputStream disReader;
	DataOutputStream dosWriter;

	JFrame frame;
	JButton sendBtn;
	JButton uploadBtn;
	JTextArea messageTa;
	JPanel centerPanel;
	JScrollBar centerPanelScrollBar;

	public Client(String sServerAddress, int nPort) {
		try
        { 
            Socket clientEndpoint = new Socket(sServerAddress, nPort);
			System.out.println("Client: Connecting to server at " + clientEndpoint.getRemoteSocketAddress());
			System.out.println("Client: Connected to server at " + clientEndpoint.getRemoteSocketAddress()); 
      
            // obtaining input and out streams 
            disReader = new DataInputStream(clientEndpoint.getInputStream());
            dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
	  
			init();
            // the following loop performs the exchange of 
            // information between client and client handler 
            while (true)  
            { 
					// String message = disReader.readUTF();
					// displayReceivedMessage(message);

				String messageType = disReader.readUTF();
				if (messageType.equals("STRING")) {
					String message = disReader.readUTF();
					displayReceivedMessage(message);
				}
				else if (messageType.equals("FILE")) {
					int fileSize = disReader.readInt();
					byte[] byteArray = new byte[fileSize];

					FileOutputStream fos = new FileOutputStream("Received.docx");
					disReader.read(byteArray, 0, fileSize);

					fos.write(byteArray);
					fos.close();
				}
					
				
                  
                // If client sends exit,close this connection  
                // and then break from the while loop 
                if(messageType.equals("Exit")) 
                { 
                    System.out.println("Closing this connection : " + clientEndpoint); 
                    clientEndpoint.close(); 
                    System.out.println("Connection closed"); 
                    break; 
                } 
                  
            } 
              
            // closing resources 
            disReader.close(); 
            dosWriter.close(); 
        }catch(Exception e){ 
            e.printStackTrace(); 
        } 
	}

	public void init () {
		frame = new JFrame();
		frame.setResizable(false);
		sendBtn = new JButton("send");
		sendBtn.setSize(150, 80);
		messageTa = new JTextArea(3, 1);
		messageTa.setSize(250, 80);
		messageTa.setLineWrap(true);
		JScrollPane messageTaScroll = new JScrollPane(messageTa);
		uploadBtn = new JButton("upload");
		uploadBtn.setSize(150, 80);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(uploadBtn, BorderLayout.WEST);
		southPanel.add(messageTaScroll, BorderLayout.CENTER);
		southPanel.add(sendBtn, BorderLayout.EAST);
		frame.add(southPanel, BorderLayout.SOUTH);

		//centerPanel = new JPanel(new GridLayout(0,1));
		centerPanel = new JPanel(new WrapLayout());
		centerPanel.setBackground(new Color(219,226,237));
	
		//centerPanel.setMinimumSize(new Dimension (375, 0));
		//centerPanel.setMaximumSize(new Dimension (375, 0));
		//centerPanel.setSize(375, 0);
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
	}

	public void actionPerformed (ActionEvent ae) {
		//JButton o = ((JButton) e.getSource());
		if (ae.getActionCommand() == "send") {
			try {
				dosWriter.writeUTF("STRING");
				dosWriter.writeUTF(messageTa.getText());
				displaySentMessage(messageTa.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
			messageTa.setText("");
		}
		if (ae.getActionCommand() == "upload") {
			try {
				JFileChooser jfc = new JFileChooser();
				int r = jfc.showSaveDialog(null);

				if (r == JFileChooser.APPROVE_OPTION) {
					File file = jfc.getSelectedFile();
					byte[] byteArray = new byte [(int)file.length()];

					FileInputStream fis = new FileInputStream(file);
					fis.read(byteArray);
					
					int fileSize = Math.toIntExact(file.length());

					//System.out.println("Server: Sending File \"Download.txt\" (" + fileSize + " bytes)");

					dosWriter.writeUTF("FILE");
					dosWriter.writeInt(fileSize);
					dosWriter.write(byteArray);
					fis.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

	}

	public void displaySentMessage (String msg) {
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		//messagePanel.setPreferredSize(new Dimension (200, 1));
		messagePanel.setOpaque(false);
		//messagePanel.setMinimumSize(new Dimension(400, 1));
		JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT));
		container.setBackground(new Color(149,215,73));
		//container.setPreferredSize(new Dimension(200, 1));
		JTextArea message = new JTextArea(msg);
		message.setColumns(16);
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		message.setEditable(false);
		message.setOpaque(false);
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

	// public static void main (String[] args) {

	// 	String sServerAddress = args[0];
	// 	int nPort = Integer.parseInt(args[1]);
	// 	Client client = new Client (sServerAddress, nPort);
	// 	// Client client = new Client()
		
		
	// }
}