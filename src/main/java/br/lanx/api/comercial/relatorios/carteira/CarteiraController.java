package br.lanx.api.comercial.relatorios.carteira;

import static br.lanx.api.comercial.shared.ComercialPermissions.CARTEIRA_EXPORTAR;
import static br.lanx.api.comercial.shared.ComercialPermissions.CARTEIRA_READ;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.comercial.relatorios.carteira.dtos.CarteiraRequest;
import br.lanx.api.comercial.relatorios.carteira.dtos.CarteiraResponse;
import br.lanx.api.shared.web.ApiResponse;
import br.lanx.api.shared.web.planilha.Planilha;

@RestController
@RequestMapping("/comercial/relatorios/carteira")
public class CarteiraController {

    private final CarteiraService carteiraService;

    public CarteiraController(CarteiraService carteiraService) {
        this.carteiraService = carteiraService;
    }

    /** Pedidos pendentes do período com a etapa da OP e os materiais que faltam (consulta o ERP). */
    @GetMapping
    @PreAuthorize("hasAuthority('" + CARTEIRA_READ + "')")
    public ApiResponse<CarteiraResponse> consultar(CarteiraRequest request) {
        return ApiResponse.ok("Carteira consultada",
                carteiraService.consultar(request.inicio(), request.fim(), request.forcarAtualizacao()));
    }

    /** Planilha (.xlsx) com a carteira e uma aba com os materiais pendentes, um por linha. */
    @GetMapping("/exportar")
    @PreAuthorize("hasAuthority('" + CARTEIRA_EXPORTAR + "')")
    public ResponseEntity<byte[]> exportar(CarteiraRequest request) {
        var carteira = carteiraService.consultar(request.inicio(), request.fim(), request.forcarAtualizacao());
        String nome = "carteira-pedidos_" + carteira.dataInicio() + "_" + carteira.dataFim() + ".xlsx";
        return Planilha.download(nome, CarteiraPlanilha.gerar(carteira));
    }
}
