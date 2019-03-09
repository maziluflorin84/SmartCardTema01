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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ClientClient {
	private static Key pubKC;
	private static Key pvtKC;
	private static Key symK;
	private static Key pubKM;

	public static void main(String[] args) {
		try {
			ClientSocket clientToM = new ClientSocket("M", Integer.parseInt(args[0]));
			Socket clientSocketToMerchant = clientToM.getClientSocket();
			
//			ClientSocket clientToPG = new ClientSocket("PG", 5557);
//			Socket clientSocketToPaymentGateway = clientToPG.getClientSocket();

			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocketToMerchant.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocketToMerchant.getOutputStream()));
			
			
			writer.write("Client Florin connected\r\n");
			writer.flush();
			
			
			// generate the RSA public and private keys for the customer
			generateClientRSAKeys();
			
			// generate the AES symmetric key
			generateAESKey();
			
			// read the RSA public key from the merchant 
			getPubKmFromMerchant();			
			
			// encrypt the client RSA public key using the AES key
			Cipher cAES = Cipher.getInstance("AES");
			SecretKeySpec k = new SecretKeySpec(getSymK().getEncoded(), "AES");
			cAES.init(Cipher.ENCRYPT_MODE, k);
			byte[] encPubKC = cAES.doFinal(getPubKC().getEncoded());
			Base64.Encoder encoder = Base64.getEncoder();
			System.out.println(encoder.encodeToString(encPubKC));
			writer.write(encoder.encodeToString(encPubKC) + "\r\n");
			writer.flush();
			
			// encrypt the AES key using the merchant RSA public key
			Cipher cRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cRSA.init(Cipher.ENCRYPT_MODE, getPubKM());
			byte[] encSymK = cRSA.doFinal(getSymK().getEncoded());
			
//			writer.write("Client Florin connected\r\n");
//			writer.flush();
//			String serverMsg = reader.readLine().trim();
//			while ((serverMsg = reader.readLine()) != null) {
//			System.out.println("Client: " + serverMsg);
//			}
				
//			Base64.Encoder encoder = Base64.getEncoder();
//			System.out.println(encoder.encodeToString(pubKM.getEncoded()));
			
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
	
	private static void generateClientRSAKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg;
		kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();			
		setPubKC(kp.getPublic());
		setPvtKC(kp.getPrivate());
	}

	public static Key getSymK() {
		return symK;
	}

	public static void setSymK(Key symK) {
		ClientClient.symK = symK;
	}
	
	private static void generateAESKey() throws NoSuchAlgorithmException {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		SecretKey key = generator.generateKey();
		setSymK(key);
	}

	public static Key getPubKM() {
		return pubKM;
	}

	public static void setPubKM(Key pubKM) {
		ClientClient.pubKM = pubKM;
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
}
