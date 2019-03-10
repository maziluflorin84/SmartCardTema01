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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

class MerchantOperations {
	private static final String PUBLICKEY = "merchant.pub";
	private static final String PRIVATEKEY = "merchant.pvt";
	
	private Key pubKM;
	private Key pvtKM;
	private Key pubKC;
	private SecretKey symK;
	
	public MerchantOperations(int port) {		
		File fPub = new File(PUBLICKEY);
		File fPvt = new File(PRIVATEKEY);

		System.out.println(" Merchant is available ");
		ServerSocket merchantSocket = null;
		
		try {			
			merchantSocket = new ServerSocket(port);
			
//			Generate Merchant RSA key files if missing 
			if (!fPub.exists() || fPub.isDirectory() || !fPvt.exists() || fPvt.isDirectory())
				generateMerchantRSAKeys();
			
//			Read Merchant RSA keys from files
			readMerchantRSAKeys();

			while (true) {
				Socket connectionSocket = merchantSocket.accept();

				BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));

				writer.write("*** Welcome to the merchant ***\r\n");
				writer.flush();
				
				String data1 = reader.readLine();
				System.out.println("Server: " + data1);

				Base64.Encoder encoder = Base64.getEncoder();
				Base64.Decoder decoder = Base64.getDecoder();
				
//				Reading and decrypting the client AES symmetric key
				String encSymK = reader.readLine().trim();				
				System.out.println("Server: The client AES symetric key");
				System.out.println("Server: \t" + encoder.encodeToString(decryptEncSymK(encSymK, decoder)));

//				Reading and decrypting the client RSA public key
				String encPubKC = reader.readLine().trim();
				System.out.println("Server: The client RSA public key");
				System.out.println("Server: \t" + encoder.encodeToString(decryptEncPubKC(encPubKC, decoder)));
				
					
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

	private Key getPubKM() {
		return pubKM;
	}

	private void setPubKM(Key pubKey) {
		this.pubKM = pubKey;
	}

	private Key getPvtKM() {
		return pvtKM;
	}

	private void setPvtKM(Key pvtKey) {
		this.pvtKM = pvtKey;
	}

	private Key getPubKC() {
		return pubKC;
	}

	private void setPubKC(Key pubKC) {
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
	
	private byte[] decryptEncPubKC(String encPubKC, Decoder decoder) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipherAES = Cipher.getInstance("AES");
		cipherAES.init(Cipher.DECRYPT_MODE, symK);
		byte[] bytePubKC = cipherAES.doFinal(decoder.decode(encPubKC));
		setPubKC(new SecretKeySpec(bytePubKC, 0, bytePubKC.length, "RSA"));
		return bytePubKC;
	}
}