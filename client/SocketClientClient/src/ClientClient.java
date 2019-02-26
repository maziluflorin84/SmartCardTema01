import java.io.*;
import java.net.*;

public class ClientClient {

	public static void main(String[] args) {
		try {
			ClientSocket clientToM = new ClientSocket("M", 5555);
			Socket clientSocketToMerchant = clientToM.getClientSocket();
			
//			ClientSocket clientToPG = new ClientSocket("PG", 5557);
//			Socket clientSocketToPaymentGateway = clientToPG.getClientSocket();

			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocketToMerchant.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocketToMerchant.getOutputStream()));
			
			String serverMsg;
			writer.write("8\r\n");
			writer.write("10\r\n");
			writer.flush();
			while ((serverMsg = reader.readLine()) != null) {
				System.out.println("Client: " + serverMsg);
			}
			clientSocketToMerchant.close();
//			clientSocketToPaymentGateway.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
