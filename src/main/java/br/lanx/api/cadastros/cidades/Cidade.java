package br.lanx.api.cadastros.cidades;

import org.springframework.data.annotation.Immutable;

import br.lanx.api.cadastros.estados.Estado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(schema = "cadastros", name = "cidades")
public class Cidade {

    @Id
    @Column(name = "codigo_ibge", nullable = false)
    private Integer codigoIbge;

    @Column(length = 100, nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="codigo_uf", nullable = false)
    private Estado estado;

    @Column(name = "siafi_id", nullable = false, unique = true)
    private String siafiId;

    @Column(nullable = false)
    private Integer ddd;

    @Column(length = 32, nullable = false)
    private String fusoHorario;

    protected Cidade() {
    }

    public Integer getCodigoIbge() {
        return codigoIbge;
    }

    public String getNome() {
        return nome;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getSiafiId() {
        return siafiId;
    }

    public Integer getDdd() {
        return ddd;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    

}
