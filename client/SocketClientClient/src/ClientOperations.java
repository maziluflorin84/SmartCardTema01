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

public class ClientOperations {
	private Key pubKC;
	private Key pvtKC;
	private SecretKey symK;
	private Key pubKM;

	public ClientOperations(int port) {
		try {
			ClientSocket clientToM = new ClientSocket("M", port);
			Socket clientSocketToMerchant = clientToM.getClientSocket();
			
//			ClientSocket clientToPG = new ClientSocket("PG", 5557);
//			Socket clientSocketToPaymentGateway = clientToPG.getClientSocket();

			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocketToMerchant.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocketToMerchant.getOutputStream()));
			
			
			writer.write("Client Florin connected\r\n");
			writer.flush();
			
			
//			generate the RSA public and private keys for the customer
			generateClientRSAKeys();
			
//			generate the AES symmetric key
			generateAESKey();
			
//			read the RSA public key from the merchant 
			getPubKmFromMerchant();			
			
//			encrypt the client RSA public key using the AES key
			Cipher cAES = Cipher.getInstance("AES");
//			SecretKeySpec k = new SecretKeySpec(getSymK().getEncoded(), "AES");
			cAES.init(Cipher.ENCRYPT_MODE, getSymK());
			byte[] encPubKC = cAES.doFinal(getPubKC().getEncoded());
			
//			encrypt the AES key using the merchant RSA public key
			Cipher cRSA = Cipher.getInstance("RSA");
			cRSA.init(Cipher.ENCRYPT_MODE, getPubKM());
			byte[] encSymK = cRSA.doFinal(getSymK().getEncoded());
			
			Base64.Encoder encoder = Base64.getEncoder();
			
			System.out.println("Client: The AES symetric key");
			System.out.println("Client: \t" + encoder.encodeToString(getSymK().getEncoded()));
			
			System.out.println("Client: The RSA public key");
			System.out.println("Client: \t" + encoder.encodeToString(getPubKC().getEncoded()));
			
			writer.write(encoder.encodeToString(encSymK) + "\r\n");
			writer.flush();
			writer.write(encoder.encodeToString(encPubKC) + "\r\n");
			writer.flush();
			
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
	
	private Key getPubKC() {
		return pubKC;
	}

	private void setPubKC(Key pubKC) {
		this.pubKC = pubKC;
	}

	private Key getPvtKC() {
		return pvtKC;
	}

	private void setPvtKC(Key pvtKC) {
		this.pvtKC = pvtKC;
	}
	
	private void generateClientRSAKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg;
		kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		setPubKC(kp.getPublic());
		setPvtKC(kp.getPrivate());
	}

	private SecretKey getSymK() {
		return symK;
	}

	private void setSymK(SecretKey symK) {
		this.symK = symK;
	}
	
	private void generateAESKey() throws NoSuchAlgorithmException {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		SecretKey key = generator.generateKey();
		setSymK(key);
	}

	private Key getPubKM() {
		return pubKM;
	}

	private void setPubKM(Key pubKM) {
		this.pubKM = pubKM;
	}
	
	private void getPubKmFromMerchant() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Path path = Paths.get("../../merchant/SocketMerchantServer/merchant.pub");
		byte[] bytes;
		
		bytes = Files.readAllBytes(path);
		X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		setPubKM(kf.generatePublic(ks));
		
	}
}
