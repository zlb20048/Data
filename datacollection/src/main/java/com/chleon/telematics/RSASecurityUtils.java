package com.chleon.telematics;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class RSASecurityUtils {
    private static final String DEFAULT_KEY = "9fb4a912c6e8422f";

    public static byte[] Encrypt(byte[] data, int offset, int len) {
        return EncryptInternal(data, offset, len, DEFAULT_KEY);
    }

    // 加密
    private static byte[] EncryptInternal(byte[] data, int offset, int len, String sKey) {
        if (sKey == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }

        byte[] encrypted = null;

        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr;
            if(android.os.Build.VERSION.SDK_INT >= 17) {
                sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            } else {
                sr = SecureRandom.getInstance("SHA1PRNG");
            }
            sr.setSeed(sKey.getBytes("utf-8"));
            kgen.init(128, sr);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            enCodeFormat = CodecUtils.hexStringToBytes("473F2FBB300E4B05BD8F179ADBF07AF5");
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// "算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            encrypted = cipher.doFinal(data, offset, len);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return encrypted;
    }

    public static byte[] Decrypt(byte[] data, int offset, int len) {
        return DecryptInternal(data, offset, len, DEFAULT_KEY);
    }

    // 解密
    private static byte[] DecryptInternal(byte[] data, int offset, int len, String sKey) {
        // 判断Key是否正确
        if (sKey == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }

        byte[] original = null;

        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr;
            if(android.os.Build.VERSION.SDK_INT >= 17) {
                sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            } else {
                sr = SecureRandom.getInstance("SHA1PRNG");
            }
            sr.setSeed(sKey.getBytes("utf-8"));
            kgen.init(128, sr);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            enCodeFormat = CodecUtils.hexStringToBytes("473F2FBB300E4B05BD8F179ADBF07AF5");
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            original = cipher.doFinal(data, offset, len);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return original;
    }
}
