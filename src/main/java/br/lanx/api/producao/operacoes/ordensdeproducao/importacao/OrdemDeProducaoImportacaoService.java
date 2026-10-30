package br.lanx.api.producao.operacoes.ordensdeproducao.importacao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.clientes.Cliente;
import br.lanx.api.cadastros.clientes.ClienteService;
import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.cadastros.materiais.MaterialService;
import br.lanx.api.cadastros.materiais.dtos.CreateMaterialRequest;
import br.lanx.api.cadastros.materiais.enums.TipoMaterial;
import br.lanx.api.cadastros.unidademedida.application.port.in.FindOrCreateUnidadeMedidaUseCase;
import br.lanx.api.producao.operacoes.ordensdeproducao.OrdemDeProducao;
import br.lanx.api.producao.operacoes.ordensdeproducao.OrdemDeProducaoRepository;
import br.lanx.api.producao.operacoes.ordensdeproducao.dtos.ImportarOrdensResponse;

/**
 * Importa a planilha de OPs do ERP: atualiza as OPs que já existem (pelo
 * código) e cria as novas. Cliente e produto que ainda não estão cadastrados
 * são criados na hora (cliente pelo nome; produto como produto acabado). A
 * etapa atual, preenchida à mão, nunca é tocada.
 */
@Service
public class OrdemDeProducaoImportacaoService {

    /** A planilha não traz a unidade do produto, e Material exige uma. */
    static final String UNIDADE_PADRAO_NOME = "Unidade";
    static final String UNIDADE_PADRAO_SIGLA = "UN";

    private final OrdemDeProducaoRepository repository;
    private final ClienteService clienteService;
    private final MaterialService materialService;
    private final FindOrCreateUnidadeMedidaUseCase unidadeMedida;

    public OrdemDeProducaoImportacaoService(OrdemDeProducaoRepository repository, ClienteService clienteService,
            MaterialService materialService, FindOrCreateUnidadeMedidaUseCase unidadeMedida) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.materialService = materialService;
        this.unidadeMedida = unidadeMedida;
    }

    /**
     * Tudo numa transação: um erro inesperado desfaz a importação inteira.
     * Linhas inválidas não derrubam o arquivo: voltam em {@code rejeitadas}.
     */
    @Transactional
    public ImportarOrdensResponse importar(InputStream arquivo, boolean incluirFinalizadas) throws IOException {
        var leitura = OrdemDeProducaoPlanilhaReader.ler(arquivo);

        List<LinhaOrdemImportada> linhas = new ArrayList<>();
        int finalizadasIgnoradas = 0;
        for (var linha : leitura.linhas()) {
            if (!incluirFinalizadas && linha.finalizada()) {
                finalizadasIgnoradas++;
            } else {
                linhas.add(linha);
            }
        }

        // uma consulta para as OPs já cadastradas, em vez de uma por linha
        Map<String, OrdemDeProducao> ops = new HashMap<>();
        repository.findByCodigoIn(linhas.stream().map(LinhaOrdemImportada::codigo).distinct().toList())
                .forEach(op -> ops.put(chave(op.getCodigo()), op));

        // a planilha repete o mesmo cliente/produto em muitas linhas: cada um é resolvido uma vez
        Map<String, Cliente> clientes = new HashMap<>();
        Map<String, Material> materiais = new HashMap<>();
        UUID unidadePadrao = null;

        int criadas = 0;
        int atualizadas = 0;
        int clientesCriados = 0;
        int materiaisCriados = 0;
        for (var linha : linhas) {
            Cliente cliente = clientes.get(chave(linha.cliente()));
            if (cliente == null) {
                var resultado = clienteService.findByNomeOrCreate(linha.cliente());
                cliente = resultado.cliente();
                clientes.put(chave(linha.cliente()), cliente);
                if (resultado.created()) {
                    clientesCriados++;
                }
            }

            Material material = materiais.get(chave(linha.materialCodigo()));
            if (material == null) {
                if (unidadePadrao == null) {
                    unidadePadrao = unidadeMedida.findByNomeOrCreate(
                            new FindOrCreateUnidadeMedidaUseCase.Command(UNIDADE_PADRAO_NOME, UNIDADE_PADRAO_SIGLA))
                            .unidade().id();
                }
                // material existente mantém o tipo que já tem; novo entra como produto acabado
                var resultado = materialService.findByCodigoOrCreate(new CreateMaterialRequest(linha.materialCodigo(),
                        linha.materialDescricao(), unidadePadrao, TipoMaterial.PRODUTO_ACABADO));
                material = materialService.findByIdOrThrow(resultado.material().id());
                materiais.put(chave(linha.materialCodigo()), material);
                if (resultado.created()) {
                    materiaisCriados++;
                }
            }

            OrdemDeProducao op = ops.get(chave(linha.codigo()));
            if (op == null) {
                op = new OrdemDeProducao();
                op.setCodigo(linha.codigo());
                // código repetido mais abaixo na planilha atualiza esta mesma OP
                ops.put(chave(linha.codigo()), op);
                criadas++;
            } else {
                atualizadas++;
            }
            op.setCliente(cliente);
            op.setMaterial(material);
            op.setQuantidade(linha.quantidade());
            op.setPrevisao(linha.entrega());
            repository.save(op);
        }

        return new ImportarOrdensResponse(leitura.linhas().size() + leitura.rejeitadas().size(), criadas, atualizadas,
                finalizadasIgnoradas, clientesCriados, materiaisCriados, leitura.rejeitadas());
    }

    private static String chave(String texto) {
        return texto.trim().toUpperCase(Locale.ROOT);
    }
}
