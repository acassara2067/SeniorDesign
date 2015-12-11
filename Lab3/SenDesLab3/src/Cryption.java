

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

// created on both clients end and creates key pair for each client

import javax.crypto.Cipher;

public class Cryption { // chat class
	
	private static final String ALGORITHM = "RSA";
	
	private PrivateKey PrivateKey; //clients private key
	private PublicKey PublicKey; // clients public key
	private PublicKey encryptionKey; // OTHER client's public key used for encryption
	private PrivateKey decryptionKey;
	private KeyPair keySet;
	private KeyPairGenerator keyGen;
	Cryption(){
		try {
			keyGen = KeyPairGenerator.getInstance(ALGORITHM);
			keyGen.initialize(1024);
			keySet = keyGen.generateKeyPair();
			
			PrivateKey = keySet.getPrivate();
			PublicKey = keySet.getPublic();
		}
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setEncryptionKey(PublicKey othersPublicKey) throws GeneralSecurityException {
	    encryptionKey = othersPublicKey;
	}
	
	public byte[] encrypt(String message){
		
		 byte[] cipherText = null;
		    try {
		      // get an RSA cipher object and print the provider
		      final Cipher cipher = Cipher.getInstance(ALGORITHM);
		      // encrypt the plain text using the public key
		      cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
		      cipherText = cipher.doFinal(message.getBytes());
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    return cipherText;
	}

	public String decrypt(byte[] message){
		
		byte[] dectyptedText = null;
	    try{ 
	      // get an RSA cipher object and print the provider
	      final Cipher cipher = Cipher.getInstance(ALGORITHM);

	      // decrypt the text using the private key
	      cipher.init(Cipher.DECRYPT_MODE, decryptionKey);
	      dectyptedText = cipher.doFinal(message);

	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }

	    return new String(dectyptedText);
	}
	
	public PublicKey getPublicKey() {
		return PublicKey;
	}
	
	public PrivateKey getPrivateKey() {
		return PrivateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		PrivateKey = privateKey;
	}

	public PrivateKey getDecryptionKey() {
		return decryptionKey;
	}

	public void setDecryptionKey(PrivateKey decryptionKey) {
		this.decryptionKey = decryptionKey;
	}

	public PublicKey getEncryptionKey() {
		return encryptionKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		PublicKey = publicKey;
	}

}
