package br.lanx.api.cadastros.enderecos;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository extends JpaRepository<Endereco, UUID> {
} 
