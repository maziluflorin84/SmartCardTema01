import java.io.*;
import java.net.*;
import java.util.*;

public class ClientClient {

	public static void main(String[] args) {
		try {
			Socket clientSocket = new Socket("localhost", 5555);
			System.out.println("Client: " + "Connection Established");

			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			String serverMsg;
			writer.write("8\r\n");
			writer.write("10\r\n");
			writer.flush();
			while ((serverMsg = reader.readLine()) != null) {
				System.out.println("Client: " + serverMsg);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
