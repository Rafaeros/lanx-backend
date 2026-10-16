package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import static br.lanx.api.compras.shared.ComprasPermissions.ANALISE_PEDIDOS_IMPORTAR;
import static br.lanx.api.compras.shared.ComprasPermissions.ANALISE_PEDIDOS_READ;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.in.ConsultarAnalisePedidosUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.in.ImportarAnalisePedidosUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/compras/relatorios/analise-pedidos")
public class AnalisePedidoComprasController {

    private final ConsultarAnalisePedidosUseCase consultarUseCase;
    private final ImportarAnalisePedidosUseCase importarUseCase;

    public AnalisePedidoComprasController(ConsultarAnalisePedidosUseCase consultarUseCase,
            ImportarAnalisePedidosUseCase importarUseCase) {
        this.consultarUseCase = consultarUseCase;
        this.importarUseCase = importarUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + ANALISE_PEDIDOS_READ + "')")
    public ApiResponse<PageResult<AnalisePedidoResponse>> consultar(ConsultarAnalisePedidosRequest request,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var resultado = consultarUseCase.consultar(request.toDomain(), new PageQuery(page, size));
        return ApiResponse.ok("Consultado com sucesso", resultado.map(AnalisePedidoResponseMapper::from));
    }

    /** Totais de tudo que atende aos filtros (para os indicadores da tela), independentemente da página. */
    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('" + ANALISE_PEDIDOS_READ + "')")
    public ApiResponse<ResumoAnalisePedidosResponse> resumo(ConsultarAnalisePedidosRequest request) {
        return ApiResponse.ok("Resumo calculado",
                ResumoAnalisePedidosResponse.from(consultarUseCase.resumir(request.toDomain())));
    }

    @PostMapping("/importar")
    @PreAuthorize("hasAuthority('" + ANALISE_PEDIDOS_IMPORTAR + "')")
    public ResponseEntity<ApiResponse<ImportarAnalisePedidoResponse>> importar(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        var filtro = new FiltroPedidos(dataInicio, dataFim);
        int itensImportados = importarUseCase.importar(filtro);

        return ApiResponse.ok("Importado com sucesso", new ImportarAnalisePedidoResponse(itensImportados)).toEntity();
    }
}