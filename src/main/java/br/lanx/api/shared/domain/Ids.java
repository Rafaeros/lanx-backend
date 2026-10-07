package br.lanx.api.shared.domain;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Geração de ids no padrão UUIDv7 (RFC 9562): ordenado pelo tempo de criação.
 */
public final class Ids {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Ids() {
    }

    public static UUID newId() {
        byte[] b = new byte[16];
        RANDOM.nextBytes(b); // começa tudo aleatório

        long millis = System.currentTimeMillis(); // 48 bits iniciais = timestamp
        b[0] = (byte) (millis >>> 40);
        b[1] = (byte) (millis >>> 32);
        b[2] = (byte) (millis >>> 24);
        b[3] = (byte) (millis >>> 16);
        b[4] = (byte) (millis >>> 8);
        b[5] = (byte) millis;

        b[6] = (byte) ((b[6] & 0x0F) | 0x70); // versão 7 (0111 nos 4 bits altos)
        b[8] = (byte) ((b[8] & 0x3F) | 0x80); // variante RFC (10 nos 2 bits altos)

        ByteBuffer buffer = ByteBuffer.wrap(b);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}