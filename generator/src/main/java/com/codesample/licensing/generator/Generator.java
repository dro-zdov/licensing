package com.codesample.licensing.generator;

import com.codesample.licensing.constants.SecurityConstants;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class Generator {

    public void generateKeypair(final File outputDir) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(SecurityConstants.SIGNATURE_KEY_ALGORITHM);
        kpg.initialize(SecurityConstants.SIGNATURE_KEY_SIZE);
        KeyPair keyPair = kpg.generateKeyPair();

        final File privateKeyFile = new File(outputDir, SecurityConstants.PRIVATE_KEY_FILENAME);
        FileUtils.write(privateKeyFile, serialize(keyPair.getPrivate()), StandardCharsets.UTF_8);
        System.out.println("Generated private key at " + privateKeyFile.getAbsolutePath());

        final File publicKeyFile = new File(outputDir, SecurityConstants.PUBLIC_KEY_FILENAME);
        FileUtils.write(publicKeyFile,  serialize(keyPair.getPublic()), StandardCharsets.UTF_8);
        System.out.println("Generated public key at " + publicKeyFile.getAbsolutePath());
    }

    private String serialize(PublicKey publicKey) {
        RSAPublicKey key = (RSAPublicKey) publicKey;
        return key.getModulus().toString() + "|" + key.getPublicExponent().toString();
    }

    private String serialize(PrivateKey privateKey) {
        RSAPrivateKey key = (RSAPrivateKey) privateKey;
        return key.getModulus().toString() + "|" + key.getPrivateExponent().toString();
    }
}
