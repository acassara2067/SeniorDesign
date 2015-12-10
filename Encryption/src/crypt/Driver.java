package crypt;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;

// test class
public class Driver {

	public static void main(String[] args) {
		
		Chat myEnd = new Chat("Gavin", "Ross");
		Chat theirEnd = new Chat("Ross", "Gavin");
		
		PrivateKey rossPrivateKey = theirEnd.generateMyKeys();
		PrivateKey myPrivateKey = myEnd.generateMyKeys();
		
		try {
			myEnd.setDecryptionKey(rossPrivateKey);
			theirEnd.setDecryptionKey(myPrivateKey);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String toEncrypt = "Hello Gavin, It's Ross";
		
		System.out.print("Ross sent (unencrypted): "+toEncrypt);
		byte[] encryptedMessage = theirEnd.encrypt(toEncrypt);
		System.out.print("\nRoss sent (encrypted): "+encryptedMessage.toString());
		System.out.print("\nGavin decrypted: "+myEnd.decrypt(encryptedMessage));
		
		String nextEncrypt = "Fuck off, prick";
		
		System.out.print("\n\nGavin sent (unencrypted: "+nextEncrypt);
		byte[] encryptedMessage2 = myEnd.encrypt(nextEncrypt);
		System.out.print("\nGavin sent (encrypted): "+encryptedMessage2.toString());
		System.out.print("\nRoss decrypted: "+theirEnd.decrypt(encryptedMessage2));

	}

}
