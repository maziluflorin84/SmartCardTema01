import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

class MerchantServer {
	private static Key pubKM;
	private static Key pvtKM;
	private static Key pubKC;
	
	public static void main(String args[]) throws Exception {
		String publicKey = "merchant.pub";
		String privateKey = "merchant.pvt";
		
		File fPub = new File(publicKey);
		File fPvt = new File(privateKey);
		
		if (!fPub.exists() || fPub.isDirectory() || !fPvt.exists() || fPvt.isDirectory())
			generateMerchantRSAKeys(publicKey, privateKey);

		Path pathPub = Paths.get("merchant.pub");
		Path pathPvt = Paths.get("merchant.pvt");
		byte[] bytesPub = Files.readAllBytes(pathPub);
		byte[] bytesPvt = Files.readAllBytes(pathPvt);
		X509EncodedKeySpec ksPub = new X509EncodedKeySpec(bytesPub);
		PKCS8EncodedKeySpec ksPvt = new PKCS8EncodedKeySpec(bytesPvt);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		setPubKM(kf.generatePublic(ksPub));
		setPvtKM(kf.generatePrivate(ksPvt));

		System.out.println(" Merchant is available ");
		ServerSocket merchantSocket = new ServerSocket(Integer.parseInt(args[0]));

		try {
			while (true) {
				Socket connectionSocket = merchantSocket.accept();

				BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

				writer.write("*** Welcome to the merchant ***\r\n");
				writer.flush();
				
				String data1 = reader.readLine();
//				while ((data1 = reader.readLine()) != null) {
				System.out.println("Server: " + data1);
//				}
				
				data1 = reader.readLine();
				System.out.println("Server: The client RSA public key encrypted with AES");
				System.out.println("Server: " + data1);
				
					
//				Base64.Encoder encoder = Base64.getEncoder();
//				String pubKeyString = new String(getPubKM().getEncoded());
//				writer.write(encoder.encodeToString(getPubKM().getEncoded()) + "\r\n");
//				writer.flush();
				
//				writer.write("*** Merchant public key sent \n");
//				writer.flush();
//				String data1 = reader.readLine().trim();

//				writer.write("*** Please type in the second number and press Enter : \n");
//				writer.flush();
//				String data2 = reader.readLine().trim();

//				int num1 = Integer.parseInt(data1);
//				int num2 = Integer.parseInt(data2);

//				int result = num1 + num2;
//				System.out.println("Addition operation done ");

//				writer.write("\r\n=== Result is  : " + result);
//				writer.flush();
				
				connectionSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			merchantSocket.close();
		}
	}
	
	private static void generateMerchantRSAKeys(String publicKey, String privateKey) {
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

	public static Key getPubKM() {
		return pubKM;
	}

	public static void setPubKM(Key pubKey) {
		MerchantServer.pubKM = pubKey;
	}

	public static Key getPvtKM() {
		return pvtKM;
	}

	public static void setPvtKM(Key pvtKey) {
		MerchantServer.pvtKM = pvtKey;
	}

	public static Key getPubKC() {
		return pubKC;
	}

	public static void setPubKC(Key pubKC) {
		MerchantServer.pubKC = pubKC;
	}
}