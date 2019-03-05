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
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ClientClient {
	private static Key pubKC;
	private static Key pvtKC;
	private static Key pubKM;

	public static void main(String[] args) {
		try {
			ClientSocket clientToM = new ClientSocket("M", Integer.parseInt(args[0]));
			Socket clientSocketToMerchant = clientToM.getClientSocket();
			
//			ClientSocket clientToPG = new ClientSocket("PG", 5557);
//			Socket clientSocketToPaymentGateway = clientToPG.getClientSocket();

			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocketToMerchant.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocketToMerchant.getOutputStream()));
			
			
			
			// generate the RSA public and private keys for the customer
			generateClientRSAKeys();
			
			// generate the AES symmetric key
			generateAESKey();
			
			// read the RSA public key from the merchant 
			getPubKmFromMerchant();
			
			// encrypt the AES key using the merchant RSA public key
			
			
			// encrypt the client RSA public key using the newly encrypted AES key
			
			
//			writer.write("Client Florin connected\r\n");
//			writer.flush();
//			String serverMsg = reader.readLine().trim();
//			while ((serverMsg = reader.readLine()) != null) {
//			System.out.println("Client: " + serverMsg);
//			}
				
//			Base64.Encoder encoder = Base64.getEncoder();
//			System.out.println(encoder.encodeToString(pubKM.getEncoded()));
			
			writer.write("Client Florin connected\r\n");
			writer.flush();
			
//			StringBuilder pubKmString = new StringBuilder();
//			while ((serverMsg = reader.readLine()) != null) {
//				pubKmString.append(serverMsg);
//				System.out.println("Client: " + serverMsg);
//			}
//			System.out.println("\n" + pubKmString);
			clientSocketToMerchant.close();
//			clientSocketToPaymentGateway.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Key getPubKC() {
		return pubKC;
	}

	public static void setPubKC(Key pubKC) {
		ClientClient.pubKC = pubKC;
	}

	public static Key getPvtKC() {
		return pvtKC;
	}

	public static void setPvtKC(Key pvtKC) {
		ClientClient.pvtKC = pvtKC;
	}

	private static void getPubKmFromMerchant() {
		Path path = Paths.get("../../merchant/SocketMerchantServer/merchant.pub");
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(path);
			X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			setPubKM(kf.generatePublic(ks));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Key getPubKM() {
		return pubKM;
	}

	public static void setPubKM(Key pubKM) {
		ClientClient.pubKM = pubKM;
	}

	private static void generateClientRSAKeys() {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.generateKeyPair();			
			setPubKC(kp.getPublic());
			setPvtKC(kp.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	private static void generateAESKey() {
		
	}
}
