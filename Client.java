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
	JTextArea messageTa;
	JPanel centerPanel;

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
				
				String message = disReader.readUTF();
				displayReceivedMessage(message);
				
                  
                // If client sends exit,close this connection  
                // and then break from the while loop 
                if(message.equals("Exit")) 
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
		messageTa = new JTextArea(3, 30);
		messageTa.setSize(250, 80);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(messageTa, BorderLayout.CENTER);
		southPanel.add(sendBtn, BorderLayout.EAST);
		frame.add(southPanel, BorderLayout.SOUTH);

		centerPanel = new JPanel(new GridLayout(0,1));
		centerPanel.setBackground(new Color(219,226,237));
		JScrollPane scrollCenterPanel = new JScrollPane(centerPanel);
		frame.add(scrollCenterPanel, BorderLayout.CENTER);
		
		frame.setSize(400, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addActionListeners();
	}

	public void addActionListeners() {
		sendBtn.addActionListener((ActionListener) this);
	}

	public void actionPerformed (ActionEvent ae) {
		//JButton o = ((JButton) e.getSource());
		if (ae.getActionCommand() == "send") {
			try {
				dosWriter.writeUTF(messageTa.getText());
				displaySentMessage(messageTa.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
			messageTa.setText("");
		}

	}

	public void displaySentMessage (String msg) {
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		messagePanel.setOpaque(false);
		JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT));
		container.setBackground(new Color(149,215,73));
		JTextArea message = new JTextArea(msg);
		message.setEditable(false);
		message.setOpaque(false);
		container.add(message);
		messagePanel.add(container);
		centerPanel.add(messagePanel);
		frame.invalidate();
		frame.validate();
		frame.repaint();
	}

	public void displayReceivedMessage(String msg) {
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		messagePanel.setOpaque(false);
		JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT));
		container.setBackground(new Color(210,210,210));
		// messagePanel.setMinimumSize(new Dimension(400, 10));
		// messagePanel.setMaximumSize(new Dimension(400, 500));
		// messagePanel.setPreferredSize(new Dimension(400, 100));
		JTextArea message = new JTextArea(msg);
		message.setOpaque(false);
		message.setEditable(false);
		message.setBackground(new Color(210,210,210));
		container.add(message);
		messagePanel.add(container);
		centerPanel.add(messagePanel);
		frame.invalidate();
		frame.validate();
		frame.repaint();
	}

	public static void main (String[] args) {

		String sServerAddress = args[0];
		int nPort = Integer.parseInt(args[1]);
		Client client = new Client (sServerAddress, nPort);
		// Client client = new Client()
		
		
	}
}