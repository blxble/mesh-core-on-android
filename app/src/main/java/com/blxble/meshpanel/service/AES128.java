package com.blxble.meshpanel.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES128 {
    public static byte[] encrypt(byte[] key, byte[] text) throws Exception {      
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");      
        Cipher cipher = Cipher.getInstance("AES");      
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);      
        byte[] encrypted = cipher.doFinal(text);      
        return encrypted;      
    }      
}
