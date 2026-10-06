package br.lanx.api.modules.auth.domain;

public enum Action {
    READ(1),    // 0001
    CREATE(1 << 1), // 0010
    UPDATE(1 << 2), // 0100
    DELETE(1 << 3); // 1000

    private final int bit;

    Action(int bit) {
        this.bit = bit;
    }

    public int bit() {
        return bit;
    }

    /** mask 0101 (5) contém READ e UPDATE */
    public boolean isIn(int mask) {
        return (mask & bit) != 0;
    }
}
