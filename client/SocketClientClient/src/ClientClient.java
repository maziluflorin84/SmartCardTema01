import java.io.*;
import java.net.*;
import java.util.*;

public class ClientClient {

	public static void main(String[] args) {
		try {
			Socket socketClient = new Socket("localhost", 5557);
			System.out.println("Client: " + "Connection Established");

			BufferedReader reader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socketClient.getOutputStream()));
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
