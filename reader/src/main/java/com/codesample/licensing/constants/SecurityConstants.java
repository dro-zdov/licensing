package com.codesample.licensing.constants;

public class SecurityConstants {
    public final static String SIGNATURE_KEY_ALGORITHM = "RSA";
    public final static int SIGNATURE_KEY_SIZE = 2048;

    public final static String SECRET_KEY_ALGORITHM = "AES";
    public final static int SECRET_KEY_SIZE = 128;

    public final static String ENCRYPTED_KEY_ALGORITHM = "RSA/ECB/PKCS1Padding";

    public final static String PRIVATE_KEY_FILENAME = "private.key";
    public final static String PUBLIC_KEY_FILENAME = "public.key";
}
