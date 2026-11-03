package br.lanx.api.notificacoes;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.lanx.api.notificacoes.dtos.ContagemNaoLidasResponse;
import br.lanx.api.notificacoes.dtos.NotificacaoResponse;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.security.CurrentUser;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final NotificacaoSseRegistry sse;
    private final CurrentUser currentUser;

    public NotificacaoController(NotificacaoService notificacaoService, NotificacaoSseRegistry sse,
            CurrentUser currentUser) {
        this.notificacaoService = notificacaoService;
        this.sse = sse;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ApiResponse<PageResult<NotificacaoResponse>> getAll(
            @RequestParam(defaultValue = "false") boolean somenteNaoLidas, Pageable pageable) {
        return ApiResponse.ok("Notificações listadas",
                notificacaoService.findAll(currentUser.id(), somenteNaoLidas, pageable));
    }

    @GetMapping("/nao-lidas/contagem")
    public ApiResponse<ContagemNaoLidasResponse> countNaoLidas() {
        return ApiResponse.ok("Notificaçõoes não lidas", notificacaoService.count(currentUser.id()));
    }

    @PatchMapping("/{id}/lida")
    public ApiResponse<Void> marcarComoLida(@PathVariable UUID id) {
        notificacaoService.marcarComoLida(currentUser.id(), id);
        return ApiResponse.ok("Notificação marcada como lida", (Void) null);
    }

    @PatchMapping("/lidas")
    public ApiResponse<Void> marcarTodasComoLidas() {
        notificacaoService.marcarTodasComoLidas(currentUser.id());
        return ApiResponse.ok("Todas as notificações marcadas como lidas", (Void) null);

    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpServletResponse response) {
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        return sse.connect(currentUser.id());
    }
}