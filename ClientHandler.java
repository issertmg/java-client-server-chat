import java.io.*; 
import java.text.*; 
import java.util.*;

import javax.swing.JFrame;

import java.net.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.nio.charset.StandardCharsets;

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
				JSONObject obj = receiveJSONObject();
				String type = obj.get("type").toString();

				if (type.equals("TEXT")) {
					server.broadcastString(obj, this);
				}
				else if (type.equals("FILE")) {
					String filename = obj.get("filename").toString();
					server.broadcastFile(obj, filename, this);
				}
				else if (type.equals("DOWNLOAD")) {
					int number = Integer.parseInt(obj.get("number").toString());
					sendFile(number);
				}

			} catch (Exception e) {
				System.out.println("Server: Client " + this.serverEndpoint.getRemoteSocketAddress() + " sends exit...");
				System.out.println("Closing this connection."); 
				try {
					serverEndpoint.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				System.out.println("Connection closed");
				server.removeClientHander(this);
				this.interrupt();
				break;
			}
		}
	}

	//sends a message to the client
	public void sendMessage (JSONObject obj) {
		try {
			writeJSON(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendButton (String filename, int number) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("type", "BUTTON");
			obj.put("filename", filename);
			obj.put("number", number);
			writeJSON(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendSenderButton (String filename, int number, Boolean isSuccessful) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("type", "SENDER_BUTTON");
			obj.put("filename", filename);
			obj.put("number", number);
			if (isSuccessful)
				obj.put("delivery_status", "SUCCESS");
			else
				obj.put("delivery_status", "FAILED");
			writeJSON(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFile(int number) {
		try {
			writeJSON(server.jsonList.get(number));
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public void sendMessageStatus (JSONObject obj, boolean isSuccessful) {
		try {
			obj.remove("type");
			obj.put("type", "MESSAGE_STATUS");
			if (isSuccessful)
				obj.put("delivery_status", "SUCCESS");
			else
				obj.put("delivery_status", "FAILED");
			writeJSON(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}