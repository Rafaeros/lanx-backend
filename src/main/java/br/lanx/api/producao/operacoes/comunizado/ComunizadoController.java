package br.lanx.api.producao.operacoes.comunizado;

import static br.lanx.api.producao.shared.ProducaoPermissions.COMUNIZADO_IMPORTAR;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.lanx.api.shared.web.planilha.Planilha;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/producao/operacoes/comunizado")
public class ComunizadoController {

    private final ComunizadoService comunizadoService;

    public ComunizadoController(ComunizadoService comunizadoService) {
        this.comunizadoService = comunizadoService;
    }

    /**
     * Recebe a IT (lista de corte, .xlsx) e devolve o comunizado no layout ORN105.
     * Campos opcionais do formulário: produto, docReferencia, realizadoEm (yyyy-MM-dd),
     * quantidade, op, inicio, fim, operador.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + COMUNIZADO_IMPORTAR + "')")
    public ResponseEntity<byte[]> comunizar(
            @RequestPart("arquivo") MultipartFile arquivo,
            @Valid @ModelAttribute ComunizadoRequest request) {
        ComunizadoService.Resultado resultado = comunizadoService.executar(arquivo, request);
        return Planilha.download(resultado.nomeArquivo(), resultado.conteudo());
    }
}
