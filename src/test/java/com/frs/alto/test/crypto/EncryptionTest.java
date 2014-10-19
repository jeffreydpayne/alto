package com.frs.alto.test.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.frs.alto.util.CryptoUtils;

@Test
public class EncryptionTest extends Assert{

	
	@Test
	public void testCipher() throws Exception {
		
		 String key = CryptoUtils.generateKey();
		 
		 assertNotNull(key);
		 
		 System.out.println("Key: " + key);
		 
		 SecretKey realKey = CryptoUtils.parseKey(key);
		 
		 String parsedKey = Hex.encodeHexString(realKey.getEncoded());
		 
		 System.out.println("Parsed Key: " + parsedKey);
		 
		 assertEquals(parsedKey, key);
		 
		 
		 String text = "The quick brown fox jumped over the lazy dog.";
		 
		 System.out.println("Text: " + text);
		 
		 String cipherText = CryptoUtils.encrypt(text, realKey);
		 
		 System.out.println("Cipher Text: " + cipherText);
		 
		 String outputText = CryptoUtils.decrypt(cipherText, realKey);
		 
		 System.out.println("Decrypted: " + outputText);
		 
		 assertEquals(outputText, text);
		 
		
	}
	
}
