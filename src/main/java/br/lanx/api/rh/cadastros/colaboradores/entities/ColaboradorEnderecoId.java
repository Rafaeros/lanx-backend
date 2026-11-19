package br.lanx.api.rh.cadastros.colaboradores.entities;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public class ColaboradorEnderecoId implements Serializable {

    private UUID colaboradorId;
    private UUID enderecoId;

    protected ColaboradorEnderecoId() {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((colaboradorId == null) ? 0 : colaboradorId.hashCode());
        result = prime * result + ((enderecoId == null) ? 0 : enderecoId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ColaboradorEnderecoId other = (ColaboradorEnderecoId) obj;
        if (colaboradorId == null) {
            if (other.colaboradorId != null)
                return false;
        } else if (!colaboradorId.equals(other.colaboradorId))
            return false;
        if (enderecoId == null) {
            if (other.enderecoId != null)
                return false;
        } else if (!enderecoId.equals(other.enderecoId))
            return false;
        return true;
    }

    public UUID getColaboradorId() {
        return colaboradorId;
    }

    public UUID getEnderecoId() {
        return enderecoId;
    }

}
