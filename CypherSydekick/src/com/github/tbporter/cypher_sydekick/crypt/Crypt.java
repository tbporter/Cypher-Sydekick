package com.github.tbporter.cypher_sydekick.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.content.Context;

public class Crypt {
	static final String ALGO = "RSA";	
	
	//IMPORTANT privacy concern, this should be kept private so that the private key stays private. private private
	static private KeyPair _keyPair;
	
	static final String PRIV_KEY_FILE = "priv.key";
	static final String PUB_KEY_FILE = "pub.key";

	/* Either generates or loads from file the keypair, 
	 * must be called once before any other Crypt methods are called.
	 * Generating can take a few seconds.
	 * 
	 * Param: Context - used for saving the file.
	 */
	static public void init(Context context) throws IOException{
		
		//If it's already initialize, just return
		if(_keyPair != null)
			return;
		
		try {
			Crypt.genKey();
			File file = context.getFileStreamPath(PUB_KEY_FILE);
			if(file.exists()){
				loadKeyFile(context);
			}
			else {
				genKey();
				saveKeyFile(context);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}
	/* Generates a Key. Stores it in _keyPair.
	 */
	
	static private void genKey() throws NoSuchAlgorithmException, NoSuchPaddingException{
		_keyPair = KeyPairGenerator.getInstance(ALGO).generateKeyPair();
	}
	
	/* Will encrypt data with a given public key and returns the results.
	 * 
	 * Param:	byte[] - data that will be encrypted
	 * 			byte[] - public key used to encrypt
	 * 
	 * Return: byte[] - encrypted result
	 */
	static public byte[] encrypt(byte[] data, byte[] pubKeyBytes){
		try {
			Cipher cipher = Cipher.getInstance(ALGO);
			PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBytes));
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			return cipher.doFinal(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/* Will decrypt data with our private key.
	 * 
	 * Param:	byte[] - data that will be decrypted
	 * 
	 * Return: byte[] - decrypted result
	 */
	static public byte[] decrypt(byte[] data){
		try {
			Cipher cipher = Cipher.getInstance(ALGO);
			cipher.init(Cipher.DECRYPT_MODE, _keyPair.getPrivate());
			return cipher.doFinal(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	//Read the keys from their files, store it in _keyPair
	static private void loadKeyFile(Context context) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
		File file = context.getFileStreamPath(PUB_KEY_FILE);
		FileInputStream in = context.openFileInput(PUB_KEY_FILE);
		byte[] pubBytes = new byte[(int)file.length()];
		in.read(pubBytes);
		in.close();
		
		file = context.getFileStreamPath(PRIV_KEY_FILE);
		in = context.openFileInput(PRIV_KEY_FILE);
		byte[] privBytes = new byte[(int)file.length()];
		in.read(privBytes);
		in.close();
		
		KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
		X509EncodedKeySpec pubEnc= new X509EncodedKeySpec(pubBytes);
		PublicKey publicKey = keyFactory.generatePublic(pubEnc);
		 
		PKCS8EncodedKeySpec privEnc = new PKCS8EncodedKeySpec(privBytes);
		PrivateKey privateKey = keyFactory.generatePrivate(privEnc);
		
		_keyPair = new KeyPair(publicKey, privateKey);
	}
	
	//Save the keys to a file
	static private void saveKeyFile(Context context) throws IOException{
		FileOutputStream out = context.openFileOutput(PUB_KEY_FILE, Context.MODE_PRIVATE);
		X509EncodedKeySpec pubEnc = new X509EncodedKeySpec(_keyPair.getPublic().getEncoded());
		out.write(pubEnc.getEncoded());
		out.close();
		
		out = context.openFileOutput(PRIV_KEY_FILE, Context.MODE_PRIVATE);
		PKCS8EncodedKeySpec privEnc = new PKCS8EncodedKeySpec(_keyPair.getPrivate().getEncoded());
		out.write(privEnc.getEncoded());
		out.close();
	}
	
	//Returns the public key
	static public byte[] getPublicKey(){
		return new X509EncodedKeySpec(_keyPair.getPublic().getEncoded()).getEncoded();
	}
}
