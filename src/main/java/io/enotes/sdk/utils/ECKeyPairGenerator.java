package io.enotes.sdk.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

public final class ECKeyPairGenerator {
    public static final String ALGORITHM = "EC";
    public static final String CURVE_NAME = "secp256k1";
    private static final ECGenParameterSpec SECP256K1_CURVE = new ECGenParameterSpec("secp256k1");

    private ECKeyPairGenerator() {
    }

    public static KeyPair generateKeyPair() {
        return ECKeyPairGenerator.Holder.INSTANCE.generateKeyPair();
    }

    public static KeyPairGenerator getInstance(String provider, SecureRandom random) throws NoSuchProviderException {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC", provider);
            gen.initialize(SECP256K1_CURVE, random);
            return gen;
        } catch (NoSuchAlgorithmException var3) {
            throw new AssertionError("Assumed JRE supports EC key pair generation", var3);
        } catch (InvalidAlgorithmParameterException var4) {
            throw new AssertionError("Assumed correct key spec statically", var4);
        }
    }

    public static KeyPairGenerator getInstance(Provider provider, SecureRandom random) {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC", provider);
            gen.initialize(SECP256K1_CURVE, random);
            return gen;
        } catch (NoSuchAlgorithmException var3) {
            throw new AssertionError("Assumed JRE supports EC key pair generation", var3);
        } catch (InvalidAlgorithmParameterException var4) {
            throw new AssertionError("Assumed correct key spec statically", var4);
        }
    }

    private static class Holder {
        private static final KeyPairGenerator INSTANCE;

        private Holder() {
        }

        static {
            try {
                INSTANCE = KeyPairGenerator.getInstance("EC");
                INSTANCE.initialize(ECKeyPairGenerator.SECP256K1_CURVE);
            } catch (NoSuchAlgorithmException var1) {
                throw new AssertionError("Assumed JRE supports EC key pair generation", var1);
            } catch (InvalidAlgorithmParameterException var2) {
                throw new AssertionError("Assumed correct key spec statically", var2);
            }
        }
    }
}
