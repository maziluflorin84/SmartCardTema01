import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

class MerchantOperations {
	private static final String PUBLICKEY = "merchant.pub";
	private static final String PRIVATEKEY = "merchant.pvt";
	
	private PublicKey pubKM;
	private PrivateKey pvtKM;
	private PublicKey pubKC;
	private SecretKey symK;
	private int sessionID;
	private byte[] signature;
	
	public MerchantOperations(int port) {		
		File fPub = new File(PUBLICKEY);
		File fPvt = new File(PRIVATEKEY);

		System.out.println(" Merchant is available ");
		ServerSocket merchantSocket = null;
		
		try {			
			merchantSocket = new ServerSocket(port);
			
//		Generate Merchant RSA key files if missing 
			if (!fPub.exists() || fPub.isDirectory() || !fPvt.exists() || fPvt.isDirectory())
				generateMerchantRSAKeys();
			
//		Read Merchant RSA keys from files
			readMerchantRSAKeys();

			while (true) {
				Socket connectionSocket = merchantSocket.accept();

				BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

				writer.write("*** Welcome to the merchant ***\r\n");
				writer.flush();
				
				String clientName = reader.readLine().trim();
				System.out.println("\nClient " + clientName + " connected!");

				Base64.Encoder encoder = Base64.getEncoder();
				Base64.Decoder decoder = Base64.getDecoder();
				
//			Receive data from first step
//				Reading and decrypting the client AES symmetric key
				String encSymK = reader.readLine().trim();				
				byte[] byteSymK = decryptEncSymK(encSymK, decoder);
				System.out.println(clientName + ": My AES symmetric key is");
				System.out.println(clientName + ": \t" + encoder.encodeToString(byteSymK));

//				Reading and decrypting the client RSA public key
				String encPubKC = reader.readLine().trim();
				byte[] bytePubKC = decryptEncPubKC(encPubKC, decoder);
				System.out.println(clientName + ": The client RSA public key");
				System.out.println(clientName + ": \t" + encoder.encodeToString(bytePubKC));
				
//				Check if the first step was completed
				if (byteSymK == null || bytePubKC == null) {
					writer.write("error\r\n");
					writer.flush();
					System.out.println(clientName + " disconnected!");
					continue;
				}
				
//			preparing and sending data for the second step
				writer.write("ok\r\n");
				writer.flush();
				
//				generate Session ID
				genSessionID();
				System.out.println("\nSession ID: " + getSessionID());
				
//				generate signature
				genSignature(String.valueOf(getSessionID()));
				System.out.println("Digital signature: " + Base64.getEncoder().encodeToString(getSignature()));
				
//				encrypt sessionID and its signature using the client RSA public key
				Cipher mRSA = Cipher.getInstance("RSA");
				mRSA.init(Cipher.ENCRYPT_MODE, getPubKC());
				byte[] encSessionID = mRSA.doFinal(String.valueOf(getSessionID()).getBytes());
//				System.out.println(getSignature().length);
//				byte[] encSignature = mRSA.doFinal(getSignature());
				
//				send encrypted values to client
				writer.write(encoder.encodeToString(encSessionID) + "\r\n");
				writer.flush();
//				writer.write(encoder.encodeToString(encSignature) + "\r\n");
//				writer.flush();
				
					
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
				
				System.out.println("Connection to " + clientName + " finished!");
				connectionSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				merchantSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void generateMerchantRSAKeys() throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator kpg;
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.generateKeyPair();
			
			FileOutputStream fosPub = new FileOutputStream(PUBLICKEY);
			Key pubKM = kp.getPublic();
			fosPub.write(pubKM.getEncoded());
			fosPub.close();
			
			FileOutputStream fosPvt = new FileOutputStream(PRIVATEKEY);
			Key pvtKM = kp.getPrivate();
			fosPvt.write(pvtKM.getEncoded());
			fosPvt.close();
	}
	
	private void readMerchantRSAKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Path pathPub = Paths.get(PUBLICKEY);
		Path pathPvt = Paths.get(PRIVATEKEY);
		byte[] bytesPub = Files.readAllBytes(pathPub);
		byte[] bytesPvt = Files.readAllBytes(pathPvt);
		X509EncodedKeySpec ksPub = new X509EncodedKeySpec(bytesPub);
		PKCS8EncodedKeySpec ksPvt = new PKCS8EncodedKeySpec(bytesPvt);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		setPubKM(kf.generatePublic(ksPub));
		setPvtKM(kf.generatePrivate(ksPvt));		
	}

	private PublicKey getPubKM() {
		return pubKM;
	}

	private void setPubKM(PublicKey pubKey) {
		this.pubKM = pubKey;
	}

	private PrivateKey getPvtKM() {
		return pvtKM;
	}

	private void setPvtKM(PrivateKey pvtKey) {
		this.pvtKM = pvtKey;
	}

	private PublicKey getPubKC() {
		return pubKC;
	}

	private void setPubKC(PublicKey pubKC) {
		this.pubKC = pubKC;
	}
	
	private SecretKey getSymK() {
		return symK;
	}

	private void setSymK(SecretKey symK) {
		this.symK = symK;
	}

	private byte[] decryptEncSymK(String encSymK, Decoder decoder) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipherRSA = Cipher.getInstance("RSA");
		cipherRSA.init(Cipher.DECRYPT_MODE, getPvtKM());
		byte[] byteSymK = cipherRSA.doFinal(decoder.decode(encSymK));
		setSymK(new SecretKeySpec(byteSymK, 0, byteSymK.length, "AES"));
		return byteSymK;
	}
	
	private byte[] decryptEncPubKC(String encPubKC, Decoder decoder) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
		Cipher cipherAES = Cipher.getInstance("AES");
		cipherAES.init(Cipher.DECRYPT_MODE, symK);
		byte[] bytePubKC = cipherAES.doFinal(decoder.decode(encPubKC));
		setPubKC(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytePubKC)));
		return bytePubKC;
	}

	private int getSessionID() {
		return sessionID;
	}

	private void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	private byte[] getSignature() {
		return signature;
	}

	private void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
	private void genSessionID() {
		Random rand = new Random();
		int max = 999;
		int min = 100;
		setSessionID(rand.nextInt((max - min) + 1) + min);
	}
	
	private void genSignature(String sId) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature privateSignature = Signature.getInstance("SHA256withRSA");
		privateSignature.initSign(getPvtKM());
		privateSignature.update(sId.getBytes());
		setSignature(privateSignature.sign());
	}
}