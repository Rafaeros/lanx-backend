package br.lanx.api.cadastros.compradores;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.compradores.dtos.CompradorRequest;
import br.lanx.api.cadastros.compradores.dtos.CompradorResponse;
import br.lanx.api.cadastros.compradores.exceptions.CompradorAlreadyExistsException;
import br.lanx.api.cadastros.compradores.exceptions.CompradorInUseException;
import br.lanx.api.cadastros.compradores.exceptions.CompradorNotFoundException;
import br.lanx.api.cadastros.shared.UsuarioLookup;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class CompradorService {

    private static final String NOME_DUPLICADO = "Já existe um comprador com esse nome.";

    private final CompradorRepository compradorRepository;
    private final UsuarioLookup usuarioLookup;
    private final List<CompradorUsageCheck> usageChecks;

    public CompradorService(CompradorRepository compradorRepository, UsuarioLookup usuarioLookup,
            List<CompradorUsageCheck> usageChecks) {
        this.compradorRepository = compradorRepository;
        this.usuarioLookup = usuarioLookup;
        this.usageChecks = usageChecks;
    }

    @Transactional
    public CompradorResponse create(CompradorRequest request) {
        String nome = request.nome().trim();
        if (compradorRepository.existsByNomeIgnoreCase(nome)) {
            throw new CompradorAlreadyExistsException(NOME_DUPLICADO);
        }
        Comprador comprador = new Comprador();
        comprador.setNome(nome);
        comprador.vincularUserId(usuarioValido(request.userId()));
        return toResponse(compradorRepository.save(comprador));
    }

    /** busca vazia = todos; senão, nome contendo o texto (sem diferenciar maiúsculas). */
    @Transactional(readOnly = true)
    public PageResult<CompradorResponse> findAll(String busca, Pageable pageable) {
        var page = busca == null || busca.isBlank()
                ? compradorRepository.findAll(pageable)
                : compradorRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        // usernames da página numa consulta só
        Map<UUID, String> usernames = usuarioLookup.usernames(
                page.getContent().stream().map(Comprador::getUserId).filter(Objects::nonNull).toList());
        return PageResult.from(page.map(c -> CompradorResponse.of(c, username(usernames, c.getUserId()))));
    }

    @Transactional(readOnly = true)
    public CompradorResponse findById(UUID id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Comprador findByIdOrThrow(UUID id) {
        return compradorRepository.findById(id)
                .orElseThrow(() -> new CompradorNotFoundException("Comprador não encontrado: " + id));
    }

    @Transactional
    public CompradorResponse update(UUID id, CompradorRequest request) {
        Comprador comprador = findByIdOrThrow(id);
        String nome = request.nome().trim();
        if (compradorRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new CompradorAlreadyExistsException(NOME_DUPLICADO);
        }
        comprador.setNome(nome);
        comprador.vincularUserId(usuarioValido(request.userId()));
        return toResponse(compradorRepository.save(comprador));
    }

    /** Exclusão lógica; vetada enquanto houver itens importados apontando para o comprador. */
    @Transactional
    public void delete(UUID id) {
        Comprador comprador = findByIdOrThrow(id);
        if (usageChecks.stream().anyMatch(check -> check.isInUse(id))) {
            throw new CompradorInUseException("Comprador em uso por itens importados e não pode ser excluído.");
        }
        compradorRepository.delete(comprador);
    }

    /** Usado na importação do ERP: busca pelo nome (sem diferenciar maiúsculas) ou cria sem usuário. */
    @Transactional
    public Comprador findByNomeOrCreate(String nome) {
        String limpo = nome.trim();
        return compradorRepository.findByNomeIgnoreCase(limpo).orElseGet(() -> {
            Comprador comprador = new Comprador();
            comprador.setNome(limpo);
            return compradorRepository.save(comprador);
        });
    }

    private UUID usuarioValido(UUID userId) {
        if (userId != null && !usuarioLookup.exists(userId)) {
            throw new CompradorNotFoundException("Usuário não encontrado: " + userId);
        }
        return userId;
    }

    /** Comprador sem vínculo não consulta o mapa (alguns Map, como Map.of(), recusam chave nula). */
    private static String username(Map<UUID, String> usernames, UUID userId) {
        return userId == null ? null : usernames.get(userId);
    }

    private CompradorResponse toResponse(Comprador comprador) {
        UUID userId = comprador.getUserId();
        String username = userId == null ? null : usuarioLookup.usernames(List.of(userId)).get(userId);
        return CompradorResponse.of(comprador, username);
    }
}
