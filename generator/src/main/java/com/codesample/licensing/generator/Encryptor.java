package com.codesample.licensing.generator;

import com.codesample.licensing.constants.SecurityConstants;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

public class Encryptor {
    private final PrivateKey privateKey;
    private final SecretKey secretKey;

    public Encryptor() throws GeneralSecurityException, IOException {
        this.secretKey = generateSecretKey();
        this.privateKey = readPrivateKey();
    }

    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(SecurityConstants.SECRET_KEY_ALGORITHM);
        generator.init(SecurityConstants.SECRET_KEY_SIZE);
        return generator.generateKey();
    }

    private PrivateKey readPrivateKey() throws GeneralSecurityException, IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(SecurityConstants.PRIVATE_KEY_FILENAME)) {
            final String keyString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            final String[] keyParts = keyString.split("\\|");
            return formKey(keyParts[0], keyParts[1]);
        }
    }

    private PrivateKey formKey(String modulus, String exponent) throws InvalidKeySpecException, NoSuchAlgorithmException {
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(new BigInteger(modulus), new BigInteger(exponent));
        return KeyFactory.getInstance(SecurityConstants.SIGNATURE_KEY_ALGORITHM).generatePrivate(spec);
    }

    public byte[] getEncryptedKey() throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(SecurityConstants.ENCRYPTED_KEY_ALGORITHM);
        cipher.init(Cipher.PUBLIC_KEY, privateKey); //usage of privateKey or publicKey is a question of semantics. Cipher.PUBLIC_KEY is encryption mode.
        return cipher.doFinal(secretKey.getEncoded());
    }

    public byte[] getEncryptedText(String data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(SecurityConstants.SECRET_KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
}
