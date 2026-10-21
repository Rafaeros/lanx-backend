package br.lanx.api.cadastros.pais;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.pais.dtos.PaisResponse;
import br.lanx.api.cadastros.pais.exceptions.PaisNotFoundException;

@Service
public class PaisLookUpService {

    private final PaisRepository paisRepository;

    public PaisLookUpService(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }

    @Transactional(readOnly = true)
    public List<PaisResponse> findAll() {
        return paisRepository.findAll(Sort.by("nome")).stream().map(PaisResponse::fromEntity).toList();
    }

    public Optional<Pais> findOptionalByNome(String nome) {
        return paisRepository.findByNomeIgnoreCase(nome);
    }

    public Pais findByIdOrThrow(UUID id) {
        return paisRepository.findById(id).orElseThrow(
                () -> new PaisNotFoundException(String.valueOf(id)));
    }

    public Pais findByNome(String nome) {
        return paisRepository.findByNomeIgnoreCase(nome).orElseThrow(
                () -> new PaisNotFoundException(String.valueOf(nome)));
    }

    public Pais findByCodigoIso2(String codigoIso2) {
        return paisRepository.findByCodigoIso2(codigoIso2).orElseThrow(
                () -> new PaisNotFoundException(String.valueOf(codigoIso2)));
    }

    public Pais findByCodigoIso3(String codigoIso3) {
        return paisRepository.findByCodigoIso3(codigoIso3).orElseThrow(
                () -> new PaisNotFoundException(String.valueOf(codigoIso3)));
    }

}
