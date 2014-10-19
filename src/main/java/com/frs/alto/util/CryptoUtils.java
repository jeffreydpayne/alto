package com.frs.alto.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class CryptoUtils {
	
	
	public static String generateKey() throws Exception {
		
	    KeyGenerator kg = KeyGenerator.getInstance("AES");
	    kg.init(128);
	    return Hex.encodeHexString(kg.generateKey().getEncoded());
		
	}
	
	public static SecretKey parseKey(String rawKey) throws Exception {
		
		byte[] rawBytes = Hex.decodeHex(rawKey.toCharArray());
	    return new SecretKeySpec(rawBytes, "AES");
		
	}
	
	public static String encrypt(String rawString, SecretKey key) throws Exception {
		
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    byte[] encryptedBytes = cipher.doFinal(rawString.getBytes());
	    
	    return Base64.encodeBase64String(encryptedBytes);

		
	}
	
	public static String decrypt(String cipherText, SecretKey key) throws Exception {
		
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    byte[] encryptedBytes = cipher.doFinal(Base64.decodeBase64(cipherText));
	    
	    return new String(encryptedBytes);
		

	}

}
