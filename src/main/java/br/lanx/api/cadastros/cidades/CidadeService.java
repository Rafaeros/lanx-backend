package br.lanx.api.cadastros.cidades;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.cidades.dtos.CidadeResponse;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class CidadeService {
    
    private final CidadeRepository cidadeRepository;

    public CidadeService(CidadeRepository cidadeRepository) {
        this.cidadeRepository = cidadeRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<CidadeResponse> buscar(String nome, Pageable pageable){
        nome = (nome == null || nome.isBlank()) ? "" : nome.trim().toLowerCase(); 
        return PageResult.from(cidadeRepository
            .findByNomeContainingIgnoreCase(nome, pageable)
            .map(CidadeResponse::fromEntity));
    }

}
