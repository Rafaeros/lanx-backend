package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.in.ConsultarAnalisePedidosUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.in.ImportarAnalisePedidosUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.shared.web.ApiResponse;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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
    public ApiResponse<List<AnalisePedidoResponse>> consultar(ImportarAnalisePedidoRequest request) {
        return ApiResponse.ok("Consultado com sucesso", consultarUseCase.consultar(request.toDomain()).stream()
                .map(AnalisePedidoResponseMapper::from)
                .toList());
    }

    @PostMapping("/importar")
    public ResponseEntity<ApiResponse<ImportarAnalisePedidoResponse>> importar(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        var filtro = new FiltroPedidos(dataInicio, dataFim);
        int itensImportados = importarUseCase.importar(filtro);

        return ApiResponse.ok("Importado com sucesso", new ImportarAnalisePedidoResponse(itensImportados)).toEntity();
    }
}