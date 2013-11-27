package com.github.tbporter.cypher_sydekick.crypt;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Crypt {
	static final String ALGO = "RSA";	
	static private KeyPair _keyPair;

	static public void init() throws NoSuchAlgorithmException, NoSuchPaddingException{
		Crypt.genKey();
	}
	static private void genKey() throws NoSuchAlgorithmException, NoSuchPaddingException{
		_keyPair = KeyPairGenerator.getInstance(ALGO).generateKeyPair();
	}
	static public byte[] encrypt(byte[] data, byte[] pubKeyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException{
		Cipher cipher = Cipher.getInstance(ALGO);
		PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBytes));
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		return cipher.doFinal(data);
	}
	static public byte[] decrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance(ALGO);
		cipher.init(Cipher.DECRYPT_MODE, _keyPair.getPrivate());
		return cipher.doFinal(data);
	}
	
	static private void loadKeyFile(){
		
	}
	static private void saveKeyFile(){
		
	}
	static public byte[] getPublicKey(){
		return _keyPair.getPublic().getEncoded();
	}
}
