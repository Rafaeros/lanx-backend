package br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.lanx.api.cadastros.clientes.Cliente;
import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.cadastros.operadores.Operador;
import br.lanx.api.cadastros.tiponaoconformidade.TipoNaoConformidade;
import br.lanx.api.producao.operacoes.ordensdeproducao.OrdemDeProducao;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.NaoConformidade;

/**
 * Relacionamentos podem vir nulos: operador é opcional e os demais somem se
 * forem excluídos logicamente (@SQLRestriction). {@code anexos} é nulo na
 * listagem e preenchido só no detalhe.
 */
public record NaoConformidadeResponse(
        UUID id,
        Resumo tipo,
        Resumo departamento,
        OrdemDeProducaoResumo ordemDeProducao,
        Resumo operador,
        BigDecimal quantidadeNaoConforme,
        LocalDate dataNaoConformidade,
        Instant createdAt,
        List<AnexoResponse> anexos) {

    public record Resumo(UUID id, String nome) {
    }

    /** O que a planilha de não conformidades mostra da OP. */
    public record OrdemDeProducaoResumo(
            UUID id,
            String codigo,
            String clienteNome,
            String materialCodigo,
            String materialDescricao,
            BigDecimal quantidade,
            LocalDate previsao) {
    }

    public static NaoConformidadeResponse fromEntity(NaoConformidade nc) {
        return fromEntity(nc, null);
    }

    public static NaoConformidadeResponse fromEntity(NaoConformidade nc, List<AnexoResponse> anexos) {
        TipoNaoConformidade tipo = nc.getTipo();
        Departamento departamento = nc.getDepartamento();
        Operador operador = nc.getOperador();
        return new NaoConformidadeResponse(
                nc.getId(),
                tipo != null ? new Resumo(tipo.getId(), tipo.getNome()) : null,
                departamento != null ? new Resumo(departamento.getId(), departamento.getNome()) : null,
                ordemDeProducao(nc.getOrdemDeProducao()),
                operador != null ? new Resumo(operador.getId(), operador.getNome()) : null,
                nc.getQuantidadeNaoConforme(),
                nc.getDataNaoConformidade(),
                nc.getCreatedAt(),
                anexos);
    }

    private static OrdemDeProducaoResumo ordemDeProducao(OrdemDeProducao op) {
        if (op == null) {
            return null;
        }
        Cliente cliente = op.getCliente();
        Material material = op.getMaterial();
        return new OrdemDeProducaoResumo(
                op.getId(),
                op.getCodigo(),
                cliente != null ? cliente.getNome() : null,
                material != null ? material.getCodigo() : null,
                material != null ? material.getDescricao() : null,
                op.getQuantidade(),
                op.getPrevisao());
    }
}
