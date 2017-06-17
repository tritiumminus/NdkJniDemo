package com.wobiancao.ndkjnidemo.ndk;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by xy on 16/1/4.
 */
public class JniUtils {
    public static native String getStringFormC();
    public static native byte[] getKeyValue();
    public static native byte[] getIv();

    private static byte[]keyValue;
    private static byte[]iv;

    private static SecretKey key;
    private static AlgorithmParameterSpec paramSpec;
    private static Cipher ecipher;

    static {
        System.loadLibrary("NdkJniDemo");	//defaultConfig.ndk.moduleName
        keyValue = getKeyValue();
        iv = getIv();
        if(null != keyValue && null !=iv) {
            KeyGenerator kgen;
            try {
                kgen = KeyGenerator.getInstance("AES");
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG","Crypto");
                random.setSeed(keyValue);
                kgen.init(128,random);
                key =kgen.generateKey();
                paramSpec =new IvParameterSpec(iv);
                ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            } catch (NoSuchAlgorithmException e) {
            } catch (NoSuchPaddingException e) {
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
    }


    /**encryption**/
    public static String encode(String msg) {
        String str ="";
        try {
            //Initialize this cipher with a key and a set of algorithm parameters
            ecipher.init(Cipher.ENCRYPT_MODE,key,paramSpec);
            //Encrypted and converted to a hexadecimal string
            str = asHex(ecipher.doFinal(msg.getBytes()));
        } catch (BadPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        }
        return str;
    }
    /**Decrypted**/
    public static String decode(String value) {
        try {
            ecipher.init(Cipher.DECRYPT_MODE,key,paramSpec);
            return new String(ecipher.doFinal(asBin(value)));
        } catch (BadPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        }
        return"";
    }
    /**Turn hexadecimal**/
    private static String asHex(byte buf[]) {
        StringBuffer strbuf =new StringBuffer(buf.length * 2);
        int i;
        for (i = 0;i <buf.length;i++) {
            if (((int)buf[i] & 0xff) < 0x10) //Less than ten in front of zero
                strbuf.append("0");
            strbuf.append(Long.toString((int)buf[i] & 0xff, 16));
        }
        return strbuf.toString();
    }
    /**Turn binary**/
    private static byte[] asBin(String src) {
        if (src.length() < 1)
            return null;
        byte[]encrypted =new byte[src.length() / 2];
        for (int i = 0;i <src.length() / 2;i++) {
            int high = Integer.parseInt(src.substring(i * 2, i * 2 + 1), 16);//取高位字节
            int low = Integer.parseInt(src.substring(i * 2 + 1, i * 2 + 2), 16);//取低位字节
            encrypted[i] = (byte) (high * 16 +low);
        }
        return encrypted;
    }

}
