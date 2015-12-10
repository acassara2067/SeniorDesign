package crypt;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

// created on both clients end and creates key pair for each client

import javax.crypto.Cipher;

public class Chat { // chat class
	
	private static final String ALGORITHM = "RSA";
	
	private String myName; // my name
	
	private String partnersName;
	
	private PrivateKey decryptionKey;
	
	private PublicKey encryptionKey;
	
	public Chat(String myName, String partnersName){
		
		this.myName = myName;
		this.partnersName = partnersName;
		
	}
	
	public String getMyName() {return myName;}
	public void setMyName(String myName) {this.myName = myName;}

	public String getPartnersName() {return partnersName;}
	public void setPartnersName(String partnersName) {this.partnersName = partnersName;}

	public PublicKey generateMyKeys(){
		
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(ALGORITHM);
		
			keyGen.initialize(1024);
			final KeyPair key = keyGen.generateKeyPair();
			      
			decryptionKey = key.getPrivate();
			
			return key.getPublic();
			
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void setEncryptionKey(PublicKey priv) throws GeneralSecurityException {
	    encryptionKey = priv;
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
	    try {
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
}
