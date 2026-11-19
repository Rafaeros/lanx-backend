package br.lanx.api.cadastros.enderecos;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.cidades.Cidade;
import br.lanx.api.cadastros.cidades.CidadeRepository;
import br.lanx.api.cadastros.cidades.exceptions.CidadeNotFoundException;
import br.lanx.api.cadastros.enderecos.dtos.EnderecoRequest;
import br.lanx.api.cadastros.enderecos.exceptions.CepInvalidoException;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final CidadeRepository cidadeRepository;

    public EnderecoService(EnderecoRepository enderecoRepository, CidadeRepository cidadeRepository) {
        this.enderecoRepository = enderecoRepository;
        this.cidadeRepository = cidadeRepository;
    }

    @Transactional
    public Endereco create(EnderecoRequest req) {
        Cidade cidade = cidadeRepository.findById(req.cidadeId()).orElseThrow(
                () -> new CidadeNotFoundException("Não existe uma cidade com esse código IBGE: " + req.cidadeId()));

        var endereco = new Endereco(
                normalize(req.cep()),
                req.logradouro().trim(),
                req.numero().trim(),
                req.complemento() == null || req.complemento().isBlank() ? null : req.complemento().trim(),
                req.bairro().trim(),
                cidade);

        return enderecoRepository.save(endereco);
    }

    private static String normalize(String cep) {
        String digits = cep.replaceAll("\\D", "");
        if (digits.length() != 8) {
            throw new CepInvalidoException("CEP deve ter 8 digitos: " + cep);
        }
        return digits;
    }

}
