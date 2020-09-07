String messageType;

				try {
					messageType = disReader.readUTF();
				} catch (Exception e) {
					System.out.println("Closing this connection : " + clientEndpoint); 
                    clientEndpoint.close(); 
                    System.out.println("Connection closed"); 
                    break;
				}