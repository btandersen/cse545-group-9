package com.security;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypt
{
    Cipher ecipher;
    AlgorithmParameterSpec paramSpec;

    public AESEncrypt()
    {
        byte[] iv = new byte[]
        {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
        };
        
        paramSpec = new IvParameterSpec(iv);

        try
        {
            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception e)
        {
            //
        }
    }

    public InputStream encryptfile(InputStream input, String key)
    {
        InputStream is = input;

        try
        {
            String password = key;
            byte[] key1 = (password).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key1 = sha.digest(key1);
            key1 = Arrays.copyOf(key1, 16); // use only first 128 bit
            SecretKeySpec secretKeySpec = new SecretKeySpec(key1, "AES");
            ecipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
            is = (InputStream) new CipherInputStream(is, ecipher);
        }
        catch (Exception e)
        {
            //
        }
        
        return is;
    }
}
