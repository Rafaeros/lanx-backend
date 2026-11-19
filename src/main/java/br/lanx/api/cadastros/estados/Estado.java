package br.lanx.api.cadastros.estados;

import org.springframework.data.annotation.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(schema = "cadastros", name = "estados")
public class Estado {

    @Id
    @Column(name = "codigo_uf", nullable = false)
    private Integer codigoUf;

    @Column(length = 2, nullable = false)
    private String uf;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(nullable = false)
    private Float latitude;

    @Column(nullable = false)
    private Float longitude;

    @Column(length = 12, nullable = false)
    private String regiao;

    protected Estado() {
    }

    public Integer getCodigoUf() {
        return codigoUf;
    }

    public String getUf() {
        return uf;
    }

    public String getNome() {
        return nome;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public String getRegiao() {
        return regiao;
    }

}
