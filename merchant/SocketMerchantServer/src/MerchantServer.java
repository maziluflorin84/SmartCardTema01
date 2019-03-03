import java.io.*;
import java.net.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

class MerchantServer {
	public static void main(String argv[]) throws Exception {
		String publicKey = "merchant.pub";
		String privateKey = "merchant.pvt";
		
		File fPub = new File(publicKey);
		File fPvt = new File(privateKey);
		
		if (!fPub.exists() || fPub.isDirectory() || !fPvt.exists() || fPvt.isDirectory())
			generateKeys(publicKey, privateKey);

		System.out.println(" Merchant is available ");
		ServerSocket merchantSocket = new ServerSocket(5555);

		try {
			while (true) {
				Socket connectionSocket = merchantSocket.accept();

				BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

				writer.write("*** Welcome to the merchant ***\r\n");
				
//				byte[] pubKMinBytes = pubKM.getEncoded();
//				writer.write(pubKM);
				
				writer.write("*** Merchant public key sent \n");
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
			merchantSocket.close();
		}
	}
	
	private static void generateKeys(String publicKey, String privateKey) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.generateKeyPair();
			
			FileOutputStream fosPub = new FileOutputStream(publicKey);
			Key pubKM = kp.getPublic();
			fosPub.write(pubKM.getEncoded());
			fosPub.close();
			
			FileOutputStream fosPvt = new FileOutputStream(privateKey);
			Key pvtKM = kp.getPrivate();
			fosPvt.write(pvtKM.getEncoded());
			fosPvt.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}