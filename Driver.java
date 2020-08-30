public class Driver {
	public static void main(String[] args) {

		int nPort = 6000;
		Server server = new Server(nPort);
		server.execute();

		String sServerAddress = "localhost";
		Client client1 = new Client (sServerAddress, nPort);
		Client client2 = new Client (sServerAddress, nPort);
	}
}