/*
 * Copyright 2017 Hussain Al-Derry
 *
 * Licensed under the Apache License, Version 2.0
 */
package com.slipkprojects.ultrasshservice.util.securepreferences.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.slipkprojects.ultrasshservice.util.securepreferences.model.EncryptionAlgorithm;

/**
 * Cipher implementation for the encrypted preferences store.
 */
public final class CipherServiceImpl implements CipherService {

    private final Logger mLogger;
    private final String mEncryptionAlgorithm;
    private final int ivSize;
    private final Cipher mCipher;

    public static CipherService getInstance(EncryptionAlgorithm algorithm) {
        switch (algorithm) {
            case AES:
                return new CipherServiceImpl("AES", "GCM", "NoPadding", 12);
            case TripleDES:
                return new CipherServiceImpl("DESede", "CBC", "PKCS5Padding", 8);
            default:
                throw new IllegalArgumentException("Unknown Algorithm");
        }
    }

    private CipherServiceImpl(String algorithm, String blockChaining, String paddingType, int ivSize) {
        this.mEncryptionAlgorithm = algorithm;
        this.ivSize = ivSize;
        this.mLogger = Logger.getLogger(CipherService.class.getName());

        try {
            String encryptionMode = String.format("%s/%s/%s", algorithm, blockChaining, paddingType);
            mLogger.info("Encryption-Mode: " + encryptionMode);
            mCipher = Cipher.getInstance(encryptionMode);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            mLogger.log(Level.SEVERE, "method: constructor()", e);
            throw new IllegalStateException("Unable to initialize cipher, mode might not be supported", e);
        }
    }

    @Override
    public int getIVSize() {
        return ivSize;
    }

    @Override
    public byte[] encrypt(byte[] key, byte[] iv, byte[] data) {
        synchronized (mCipher) {
            try {
                mCipher.init(Cipher.ENCRYPT_MODE, generateSecretKeySpec(key), generateParameters(iv));
                return mCipher.doFinal(data);
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                mLogger.log(Level.SEVERE, "method: encrypt()", e);
                throw new IllegalStateException(String.format("%s: %s", e.getClass().getName(), e.getMessage()), e);
            }
        }
    }

    @Override
    public byte[] decrypt(byte[] key, byte[] iv, byte[] data) {
        synchronized (mCipher) {
            try {
                mCipher.init(Cipher.DECRYPT_MODE, generateSecretKeySpec(key), generateParameters(iv));
                return mCipher.doFinal(data);
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                mLogger.log(Level.SEVERE, "method: decrypt()", e);
                throw new IllegalStateException(String.format("%s: %s", e.getClass().getName(), e.getMessage()), e);
            }
        }
    }

    private SecretKey generateSecretKeySpec(byte[] key) {
        return new SecretKeySpec(key, mEncryptionAlgorithm);
    }

    /*
     * AES/GCM requires GCMParameterSpec (including the authentication-tag size).
     * IvParameterSpec is not a portable parameter type for GCM and can cause
     * InvalidAlgorithmParameterException on Android crypto providers.
     */
    private AlgorithmParameterSpec generateParameters(byte[] iv) {
        if ("AES".equals(mEncryptionAlgorithm)) {
            return new GCMParameterSpec(128, iv);
        }
        return new IvParameterSpec(iv);
    }
}
