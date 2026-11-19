package br.lanx.api.rh.cadastros.colaboradores;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.enderecos.Endereco;
import br.lanx.api.rh.cadastros.colaboradores.dtos.ColaboradorDetailResponse;
import br.lanx.api.rh.cadastros.colaboradores.dtos.CreateColaboradorRequest;
import br.lanx.api.rh.cadastros.colaboradores.entities.Colaborador;
import br.lanx.api.rh.cadastros.colaboradores.entities.ColaboradorEmail;
import br.lanx.api.rh.cadastros.colaboradores.entities.ColaboradorEndereco;
import br.lanx.api.rh.cadastros.colaboradores.entities.ColaboradorTelefone;

@Component
public class ColaboradorMapper {

    public Colaborador toEntity(
        CreateColaboradorRequest req,
        Departamento departamento) {
        var colaborador = new Colaborador();
        colaborador.setNome(req.nome());
        colaborador.setDepartamento(departamento);

        req.telefones().forEach(
            dto -> colaborador.adicionarTelefone(
                new ColaboradorTelefone(
                    dto.telefone(),
                    dto.tipo(),
                    dto.principal())));

        req.emails().forEach(
            dto -> colaborador.adicionarEmail(
                new ColaboradorEmail(
                    dto.email(),
                    dto.tipo(),
                    dto.principal())));

        return colaborador;
    }

    public ColaboradorEndereco toColaboradorEndereco(
        Endereco endereco,
        CreateColaboradorRequest.Endereco dto) {
        return new ColaboradorEndereco(endereco, dto.tipo(), dto.principal());
    }

    public ColaboradorDetailResponse toDetail(Colaborador c, String cpf) {
        return new ColaboradorDetailResponse(
            c.getId(),
            c.getNome(),
            cpf,
            c.getDepartamento().getId(),
            c.getDepartamento().getNome(),
            c.getTelefones().stream().map(this::toTelefone).toList(),
            c.getEmails().stream().map(this::toEmail).toList(),
            c.getEnderecos().stream().map(this::toEndereco).toList(),
            c.getCreatedAt(),
            c.getUpdatedAt());
    }

    private ColaboradorDetailResponse.Telefone toTelefone(ColaboradorTelefone t) {
        return new ColaboradorDetailResponse.Telefone(
            t.getId(),
            t.getTelefone(),
            t.getTipo(),
            t.isPrincipal(),
            t.getCreatedAt());
    }

    private ColaboradorDetailResponse.Email toEmail(ColaboradorEmail e) {
        return new ColaboradorDetailResponse.Email(
            e.getId(),
            e.getEmail(),
            e.getTipo(),
            e.isPrincipal(),
            e.getCreatedAt());
    }

    private ColaboradorDetailResponse.Endereco toEndereco(ColaboradorEndereco ce) {
        var end = ce.getEndereco();
        return new ColaboradorDetailResponse.Endereco(
            end.getId(),
            ce.getTipo(),
            ce.isPrincipal(),
            end.getCep(),
            end.getLogradouro(),
            end.getNumero(),
            end.getComplemento(),
            end.getBairro(),
            end.getCidade().getCodigoIbge(),
            end.getCidade().getNome());
    }
}
