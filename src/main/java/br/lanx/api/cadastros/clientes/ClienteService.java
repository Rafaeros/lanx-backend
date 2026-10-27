package br.lanx.api.cadastros.clientes;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.clientes.dtos.ClienteRequest;
import br.lanx.api.cadastros.clientes.dtos.ClienteResponse;
import br.lanx.api.cadastros.clientes.exceptions.ClienteAlreadyExistsException;
import br.lanx.api.cadastros.clientes.exceptions.ClienteInUseException;
import br.lanx.api.cadastros.clientes.exceptions.ClienteNotFoundException;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class ClienteService {

    private static final String NOME_DUPLICADO = "Já existe um cliente com esse nome.";
    private static final String CNPJ_DUPLICADO = "Já existe um cliente com esse CNPJ.";

    private final ClienteRepository clienteRepository;
    private final List<ClienteUsageCheck> usageChecks;

    public ClienteService(ClienteRepository clienteRepository, List<ClienteUsageCheck> usageChecks) {
        this.clienteRepository = clienteRepository;
        this.usageChecks = usageChecks;
    }

    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        String nome = request.nome().trim();
        String cnpj = soDigitos(request.cnpj());
        if (clienteRepository.existsByNomeIgnoreCase(nome)) {
            throw new ClienteAlreadyExistsException(NOME_DUPLICADO);
        }
        if (cnpj != null && clienteRepository.existsByCnpj(cnpj)) {
            throw new ClienteAlreadyExistsException(CNPJ_DUPLICADO);
        }
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCnpj(cnpj);
        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    /** busca vazia = todos; senão, nome contendo o texto ou CNPJ contendo os dígitos. */
    @Transactional(readOnly = true)
    public PageResult<ClienteResponse> findAll(String busca, Pageable pageable) {
        if (busca == null || busca.isBlank()) {
            return PageResult.from(clienteRepository.findAll(pageable).map(ClienteResponse::fromEntity));
        }
        String texto = busca.trim();
        String digitos = soDigitos(texto);
        // sem dígitos na busca, o filtro de CNPJ não pode casar com tudo
        var page = clienteRepository.findByNomeContainingIgnoreCaseOrCnpjContaining(texto,
                digitos == null ? texto : digitos, pageable);
        return PageResult.from(page.map(ClienteResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public ClienteResponse findById(UUID id) {
        return ClienteResponse.fromEntity(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Cliente findByIdOrThrow(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado: " + id));
    }

    @Transactional
    public ClienteResponse update(UUID id, ClienteRequest request) {
        Cliente cliente = findByIdOrThrow(id);
        String nome = request.nome().trim();
        String cnpj = soDigitos(request.cnpj());
        if (clienteRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ClienteAlreadyExistsException(NOME_DUPLICADO);
        }
        if (cnpj != null && clienteRepository.existsByCnpjAndIdNot(cnpj, id)) {
            throw new ClienteAlreadyExistsException(CNPJ_DUPLICADO);
        }
        cliente.setNome(nome);
        cliente.setCnpj(cnpj);
        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    /** Exclusão lógica; vetada enquanto houver ordens de produção apontando para o cliente. */
    @Transactional
    public void delete(UUID id) {
        Cliente cliente = findByIdOrThrow(id);
        if (usageChecks.stream().anyMatch(check -> check.isInUse(id))) {
            throw new ClienteInUseException("Cliente em uso por ordens de produção e não pode ser excluído.");
        }
        clienteRepository.delete(cliente);
    }

    /** "12.345.678/0001-90" → "12345678000190"; vazio → null (cliente sem CNPJ). */
    private static String soDigitos(String texto) {
        if (texto == null) {
            return null;
        }
        String digitos = texto.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }
}
