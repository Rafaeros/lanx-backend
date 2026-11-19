package br.lanx.api.cadastros.estados;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.estados.dtos.EstadoResponse;
import br.lanx.api.cadastros.estados.exceptions.EstadoNotFoundException;

@Service
public class EstadoService {

    private final EstadoRepository estadoRepository;

    public EstadoService(EstadoRepository estadoRepository) {
        this.estadoRepository = estadoRepository;
    }

    @Transactional(readOnly = true)
    public List<EstadoResponse> findAll(String busca) {
        String filtro = busca == null || busca.isBlank() ? "" : "%" + busca.trim().toLowerCase(Locale.ROOT) + "%";
        return estadoRepository.buscar(filtro).stream().map(EstadoResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Estado findByNome(String nome) {
        return estadoRepository.findByNomeIgnoreCase(nome).orElseThrow(
                () -> new EstadoNotFoundException(String.valueOf(nome)));
    }
}
