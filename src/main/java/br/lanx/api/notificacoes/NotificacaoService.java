package br.lanx.api.notificacoes;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.user.UserService;
import br.lanx.api.notificacoes.dtos.ContagemNaoLidasResponse;
import br.lanx.api.notificacoes.dtos.NotificacaoResponse;
import br.lanx.api.notificacoes.exceptions.NotificacaoNotFoundException;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.events.NaoConformidadeCriadaEvent;

@Service
public class NotificacaoService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NotificacaoRepository notificacaoRepository;
    private final UserService userService;
    private final NotificacoesProperties props;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UserService userService,
            NotificacoesProperties props) {
        this.notificacaoRepository = notificacaoRepository;
        this.userService = userService;
        this.props = props;
    }

    @Transactional
    public List<NotificacaoResponse> notificarNaoConformidadeCriada(NaoConformidadeCriadaEvent event) {
        TipoNotificacao tipo = TipoNotificacao.NAO_CONFORMIDADE_CRIADA;
        List<UUID> destinatarios = userService
                .findActiveIdsByDepartamentos(props.naoConformidadeCriada().departamentos());
        Set<UUID> jaNotificados = notificacaoRepository.findUserIdsJaNotificados(tipo, event.naoConformidadeId());

        List<Notificacao> novas = destinatarios.stream()
                .filter(userId -> !jaNotificados.contains(userId))
                .map(userId -> {
                    Notificacao n = new Notificacao();
                    n.setUserId(userId);
                    n.setTipo(tipo);
                    n.setTitulo("Nova não conformidade");
                    n.setMensagem(event.tipo() + " registrada em " + event.dataNaoConformidade().format(DATA));
                    n.setLink("/teste-inspecao/nao-conformidades");
                    n.setReferenciaId(event.naoConformidadeId());
                    return n;
                })
                .toList();
        return notificacaoRepository.saveAllAndFlush(novas).stream()
                .map(NotificacaoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResult<NotificacaoResponse> findAll(UUID userId, boolean somenteNaoLidas, Pageable pageable) {
        Page<Notificacao> page = somenteNaoLidas
                ? notificacaoRepository.findByUserIdAndLidaEmIsNullOrderByCreatedAtDesc(userId, pageable)
                : notificacaoRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResult.from(page, NotificacaoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ContagemNaoLidasResponse count(UUID userId) {
        return new ContagemNaoLidasResponse(notificacaoRepository.countByUserIdAndLidaEmIsNull(userId));
    }

    @Transactional
    public void marcarComoLida(UUID userId, UUID id) {
        notificacaoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(
                        () -> new NotificacaoNotFoundException("Notificação não encontrada"))
                .marcarComoLida();
    }

    @Transactional
    public void marcarTodasComoLidas(UUID userId) {
        notificacaoRepository.marcarTodasComoLidas(userId);
    }

}
