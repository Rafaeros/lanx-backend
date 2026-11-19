package br.lanx.api.shared.security.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CpfCryptoService {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKey aesKey;
    private final SecretKey hmacKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CpfCryptoService(@Value("${app.security.crypto.aes-key}") String aesKey,
            @Value("${app.security.crypto.hmac-key}") String hmacKey) {
        this.aesKey = new SecretKeySpec(
                Base64.getDecoder().decode(aesKey), "AES");
        this.hmacKey = new SecretKeySpec(
                Base64.getDecoder().decode(hmacKey), HMAC_ALGORITHM);

        if (this.aesKey.getEncoded().length != 32 || this.hmacKey.getEncoded().length != 32) {
            throw new IllegalArgumentException("As chaves devem possuir 32 bytes");
        }
    }

    public String encrypt(String cpf) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            secureRandom.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    aesKey,
                    new GCMParameterSpec(TAG_LENGTH, nonce));

            byte[] encrypted = cipher.doFinal(
                    normalize(cpf).getBytes(StandardCharsets.UTF_8));

            byte[] result = ByteBuffer
                    .allocate(nonce.length + encrypted.length)
                    .put(nonce)
                    .put(encrypted)
                    .array();

            return Base64.getEncoder().encodeToString(result);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Erro ao criptografar CPF", e);
        }
    }

    private String normalize(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    public String decrypt(String encryptedCpf) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedCpf);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] nonce = new byte[NONCE_LENGTH];
            buffer.get(nonce);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    aesKey,
                    new GCMParameterSpec(TAG_LENGTH, nonce));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);

        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(
                    "Erro ao descriptografar CPF", e);
        }
    }

    public String hash(String cpf) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hmacKey);

            byte[] result = mac.doFinal(
                    normalize(cpf).getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(result);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Erro ao gerar hash do CPF", e);
        }
    }
}
