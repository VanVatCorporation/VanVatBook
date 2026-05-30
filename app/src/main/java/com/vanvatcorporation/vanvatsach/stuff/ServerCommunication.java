package com.vanvatcorporation.vanvatsach.stuff;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

public class ServerCommunication {























//    public class KeyManagement {
//
//        private static final String AES_KEY_ALIAS = "aesKeyAlias";
//        private static final String RSA_KEY_ALIAS = "rsaKeyAlias";
//        private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
//
//        // Method to handle key generation based on API level
//        public void generateKey(Context context) throws Exception {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                generateAESKey(); // For API 23 and above
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                generateRSAKeyPair(context); // For API 21 and 22
//            } else {
//                throw new UnsupportedOperationException("API level below 21 is not supported.");
//            }
//        }
//
//        // Generate AES key for API 23 and above
//        private void generateAESKey() throws Exception {
//            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
//            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
//                    AES_KEY_ALIAS,
//                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
//            )
//                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                    .build();
//            keyGenerator.init(keyGenParameterSpec);
//            keyGenerator.generateKey();
//        }
//
//        // Generate RSA key pair for API 21 and 22
//        private void generateRSAKeyPair(Context context) throws Exception {
//            Calendar start = Calendar.getInstance();
//            Calendar end = Calendar.getInstance();
//            end.add(Calendar.YEAR, 30); // Key valid for 30 years
//
//            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
//                    .setAlias(RSA_KEY_ALIAS)
//                    .setSubject(new X500Principal("CN=" + RSA_KEY_ALIAS))
//                    .setSerialNumber(BigInteger.ONE)
//                    .setStartDate(start.getTime())
//                    .setEndDate(end.getTime())
//                    .build();
//
//            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEYSTORE);
//            keyPairGenerator.initialize(spec);
//            keyPairGenerator.generateKeyPair();
//        }
//
//        // Retrieve the AES key (API 23 and above)
//        public SecretKey getAESKey() throws Exception {
//            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
//            keyStore.load(null);
//            return (SecretKey) keyStore.getKey(AES_KEY_ALIAS, null);
//        }
//
//        // Retrieve the RSA key pair (API 21 and 22)
//        public KeyPair getRSAKeyPair() throws Exception {
//            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
//            keyStore.load(null);
//            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(RSA_KEY_ALIAS, null);
//            if (entry != null) {
//                return new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
//            }
//            return null;
//        }
//
//        public void useKey()
//        {
//            KeyManagement keyManagement = new KeyManagement();
//            keyManagement.generateKey(context);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                SecretKey aesKey = keyManagement.getAESKey();
//                // Use AES key for encryption/decryption
//            } else {
//                KeyPair rsaKeyPair = keyManagement.getRSAKeyPair();
//                // Use RSA key pair for encryption/decryption
//            }
//
//        }
//
//    }














//
//
//
//
//    private static final String KEY_ALIAS = "serverKeyAlias";
//    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
//
//    // Save the fetched key in the Keystore
//    public void saveKeyToKeystore(String keyFromServer) throws Exception {
//        byte[] decodedKey = Base64.decode(keyFromServer, Base64.DEFAULT); // Decode Base64 key from server
//        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//
//        // Store the key in the Keystore
//        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
//        KeyGenParameterSpec spec = new KeyGenParameterSpec().Builder(KEY_ALIAS,
//                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                .build();
//        keyGenerator.init(spec);
//        keyGenerator.generateKey();
//    }
//
//    // Retrieve the key from Keystore
//    public SecretKey getKeyFromKeystore() throws Exception {
//        KeyManagement keyManagement = new KeyManagement();
//        keyManagement.generateKey(context);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            SecretKey aesKey = keyManagement.getAESKey();
//            // Use AES key for encryption/decryption
//        } else {
//            KeyPair rsaKeyPair = keyManagement.getRSAKeyPair();
//            // Use RSA key pair for encryption/decryption
//        }
//
//    }
//
//
//
//
//    public static String encrypt(String plainText, byte[] key, byte[] iv) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
//        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
//
//        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
//        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
//        return Base64.encodeToString(encrypted, Base64.DEFAULT);
//    }
//    public static String decrypt(String encryptedText, byte[] key, byte[] iv) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
//        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
//
//        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
//        byte[] decodedEncryptedText = Base64.decode(encryptedText, Base64.DEFAULT);
//        byte[] original = cipher.doFinal(decodedEncryptedText);
//        return new String(original, StandardCharsets.UTF_8);
//    }


}
