package com.codesample.licensing.reader;

import com.codesample.licensing.constants.SecurityConstants;
import com.codesample.licensing.entity.LicenseInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class LicenseReader {

    public static LicenseInfo readLicense(File licenseFile) throws Exception {
        try {
            final PublicKey publicKey = readPublicKey();
            return parseLicenseFile(licenseFile, publicKey);
        } catch (Exception e) {
            throw new Exception("Failed to parse license", e);
        }
    }

    private static PublicKey readPublicKey() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        try (InputStream inputStream = LicenseReader.class.getClassLoader().getResourceAsStream(SecurityConstants.PUBLIC_KEY_FILENAME)) {
            final String keyString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            final String[] keyParts = keyString.split("\\|");
            return formKey(keyParts[0], keyParts[1]);
        }
    }

    private static PublicKey formKey(String modulus, String exponent) throws InvalidKeySpecException, NoSuchAlgorithmException {
        RSAPublicKeySpec Spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
        return KeyFactory.getInstance(SecurityConstants.SIGNATURE_KEY_ALGORITHM).generatePublic(Spec);
    }

    private static LicenseInfo parseLicenseFile(File licenseFile, PublicKey publicKey) throws IOException, GeneralSecurityException {
        String[] keyAndLicense = FileUtils.readFileToString(licenseFile, StandardCharsets.UTF_8).split("\\|");
        SecretKey secretKey = decryptSecretKey(publicKey, Base64.getDecoder().decode(keyAndLicense[0]));
        String json = decryptLicense(secretKey, Base64.getDecoder().decode(keyAndLicense[1]));
        return new Gson().fromJson(json, new TypeToken<LicenseInfo>() {}.getType());
    }

    private static SecretKey decryptSecretKey(PublicKey publicKey, byte[] encryptedSecretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(SecurityConstants.SIGNATURE_KEY_ALGORITHM);
        cipher.init(Cipher.PRIVATE_KEY, publicKey); //usage of privateKey or publicKey is a question of semantics. Cipher.PRIVATE_KEY is decryption mode.
        byte[] decryptedKey = cipher.doFinal(encryptedSecretKey);
        return new SecretKeySpec(decryptedKey, 0, decryptedKey.length, SecurityConstants.SECRET_KEY_ALGORITHM);
    }

    private static String decryptLicense(SecretKey secretKey, byte[] encryptedLicense) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(SecurityConstants.SECRET_KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(encryptedLicense), StandardCharsets.UTF_8);
    }
}
