package com.example.Steganography;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	public static byte[] encrypt(byte [] key, byte[] input){
		Cipher aes = null;
		byte[] output = null;
		SecretKeySpec test = new SecretKeySpec(key, "AES");
		try {
			aes.init(0, test);
			output = aes.doFinal(input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
	
	public static byte[] decrypt(byte [] key, byte[] input){
		Cipher aes = null;
		byte[] output = null;
		SecretKeySpec test = new SecretKeySpec(key, "AES");
		try {
			aes.init(1, test);
			output = aes.doFinal(input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
}
