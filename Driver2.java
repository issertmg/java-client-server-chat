public class Driver2 {
	public static void main(String[] args) {

		int nPort = 6000;
		String sServerAddress = "localhost";
		Client client1 = new Client (sServerAddress, nPort);
		Client client2 = new Client (sServerAddress, nPort);
	}
}