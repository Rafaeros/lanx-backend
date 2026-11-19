package br.lanx.api.rh.cadastros.colaboradores.entities;

import br.lanx.api.cadastros.enderecos.Endereco;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoEndereco;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(schema = "recursos_humanos", name = "colaborador_enderecos")
public class ColaboradorEndereco {

    @EmbeddedId
    private ColaboradorEnderecoId id = new ColaboradorEnderecoId();

    @MapsId("colaboradorId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private Colaborador colaborador;

    @MapsId("enderecoId")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEndereco tipo = TipoEndereco.RESIDENCIAL;

    @Column(nullable = false)
    private boolean principal = false;

    protected ColaboradorEndereco() {
    }

    public ColaboradorEndereco(Endereco endereco, TipoEndereco tipo, boolean principal) {
        this.endereco = endereco;
        this.tipo = tipo;
        this.principal = principal;
    }

    public ColaboradorEnderecoId getId() {
        return id;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public TipoEndereco getTipo() {
        return tipo;
    }

    public void setTipo(TipoEndereco tipo) {
        this.tipo = tipo;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

}
