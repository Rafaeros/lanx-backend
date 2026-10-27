package br.lanx.api.qualidade.relatorios.iqf.adapter.in.web;

import static br.lanx.api.qualidade.shared.QualidadePermissions.IQF_EXPORTAR;
import static br.lanx.api.qualidade.shared.QualidadePermissions.IQF_READ;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.qualidade.relatorios.iqf.application.ConsultarIqfUseCase;
import br.lanx.api.shared.web.ApiResponse;
import br.lanx.api.shared.web.planilha.Planilha;

@RestController
@RequestMapping("/qualidade/relatorios/iqf")
public class IqfController {

    private final ConsultarIqfUseCase consultarIqfUseCase;

    public IqfController(ConsultarIqfUseCase consultarIqfUseCase) {
        this.consultarIqfUseCase = consultarIqfUseCase;
    }

    /** IQF de prazo geral, por fornecedor e mês a mês, das entregas previstas no período. */
    @GetMapping
    @PreAuthorize("hasAuthority('" + IQF_READ + "')")
    public ApiResponse<IqfResponse> consultar(ConsultarIqfRequest request) {
        var resultado = consultarIqfUseCase.consultar(request.inicio(), request.fim(), request.rateiosSelecionados(),
                request.tolerancia());
        return ApiResponse.ok("IQF calculado", IqfResponse.from(resultado));
    }

    /** IQF de um único fornecedor no período, mês a mês (mesmos filtros da consulta geral). */
    @GetMapping("/fornecedores/{fornecedorId}")
    @PreAuthorize("hasAuthority('" + IQF_READ + "')")
    public ApiResponse<IqfResponse> consultarFornecedor(@PathVariable UUID fornecedorId, ConsultarIqfRequest request) {
        var resultado = consultarIqfUseCase.consultarFornecedor(fornecedorId, request.inicio(), request.fim(),
                request.rateiosSelecionados(),
                request.tolerancia());
        return ApiResponse.ok("IQF do fornecedor calculado", IqfResponse.doFornecedor(resultado));
    }

    /** Rateios disponíveis para o filtro (id e nome, ex.: MATERIA-PRIMA). */
    @GetMapping("/rateios")
    @PreAuthorize("hasAuthority('" + IQF_READ + "')")
    public ApiResponse<List<IqfResponse.RateioResumo>> rateios() {
        return ApiResponse.ok("Rateios",
                consultarIqfUseCase.listarRateios().stream().map(IqfResponse.RateioResumo::from).toList());
    }

    /** Planilha (.xlsx) com resumo, IQF mês a mês (com gráfico) e ranking de fornecedores. */
    @GetMapping("/exportar")
    @PreAuthorize("hasAuthority('" + IQF_EXPORTAR + "')")
    public ResponseEntity<byte[]> exportar(ConsultarIqfRequest request) {
        var resultado = consultarIqfUseCase.consultar(request.inicio(), request.fim(), request.rateiosSelecionados(),
                request.tolerancia());
        String nome = "iqf-fornecedores_" + resultado.inicioPeriodo() + "_" + resultado.fimPeriodo() + ".xlsx";
        return Planilha.download(nome, IqfPlanilha.gerar(resultado));
    }
}
