import java.io.*;
import java.net.*;

class PaymentGatewayServer {
	public static void main(String argv[]) throws Exception {

		System.out.println(" Server is Running  ");
		ServerSocket paymentGatewaySocket = new ServerSocket(5557);

		try {
			while (true) {
				Socket connectionSocket = paymentGatewaySocket.accept();

				BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

				writer.write("*** Welcome to the payment gateway ***\r\n");
				writer.write("*** Please type in the first number and press Enter : \n");
				writer.flush();
				String data1 = reader.readLine().trim();

				writer.write("*** Please type in the second number and press Enter : \n");
				writer.flush();
				String data2 = reader.readLine().trim();

				int num1 = Integer.parseInt(data1);
				int num2 = Integer.parseInt(data2);

				int result = num1 + num2;
				System.out.println("Addition operation done ");

				writer.write("\r\n=== Result is  : " + result);
				writer.flush();
				connectionSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			paymentGatewaySocket.close();
		}
	}
}