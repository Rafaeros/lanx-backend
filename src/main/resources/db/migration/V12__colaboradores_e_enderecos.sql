CREATE SCHEMA IF NOT EXISTS recursos_humanos;
CREATE SCHEMA IF NOT EXISTS cadastros;

CREATE TABLE recursos_humanos.colaboradores (
  id UUID PRIMARY KEY,
  departamento_id UUID NOT NULL,
  cpf_encrypted VARCHAR(80) NOT NULL,
  cpf_hash VARCHAR(64) NOT NULL,
  nome VARCHAR(255) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  CONSTRAINT uk_cpf_hash UNIQUE (cpf_hash),
  CONSTRAINT fk_colaboradores_departamento FOREIGN KEY (departamento_id)
    REFERENCES cadastros.departamentos (id) ON DELETE RESTRICT
);
CREATE INDEX idx_colaboradores_departamento ON recursos_humanos.colaboradores(departamento_id);

CREATE TABLE recursos_humanos.colaboradores_telefones (
  id UUID PRIMARY KEY,
  colaborador_id UUID NOT NULL REFERENCES recursos_humanos.colaboradores(id) ON DELETE CASCADE,
  telefone VARCHAR(16) NOT NULL,
  tipo VARCHAR(20) NOT NULL DEFAULT 'CELULAR',
  principal BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_colab_telefone UNIQUE (colaborador_id, telefone),
  CONSTRAINT ck_telefone_formato CHECK (telefone ~ '^\+[1-9][0-9]{7,14}$')
);
CREATE UNIQUE INDEX uk_colab_tel_principal
  ON recursos_humanos.colaboradores_telefones(colaborador_id) WHERE principal;

CREATE TABLE recursos_humanos.colaboradores_emails (
  id UUID PRIMARY KEY,
  colaborador_id UUID NOT NULL REFERENCES recursos_humanos.colaboradores(id) ON DELETE CASCADE,
  email VARCHAR(255) NOT NULL,
  tipo VARCHAR(50) NOT NULL DEFAULT 'PESSOAL',
  principal BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


CREATE TABLE cadastros.enderecos(
  id UUID PRIMARY KEY,
  cep         CHAR(8)      NOT NULL CHECK (cep ~ '^[0-9]{8}$'),
  logradouro VARCHAR(150) NOT NULL,
  numero VARCHAR(10) NOT NULL,
  complemento VARCHAR(100),
  bairro VARCHAR(100) NOT NULl,
  cidade_id INT NOT NULl REFERENCES cadastros.cidades(codigo_ibge),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_colab_email_colaborador ON recursos_humanos.colaboradores_emails(colaborador_id);
CREATE UNIQUE INDEX uk_colab_email ON recursos_humanos.colaboradores_emails(colaborador_id, lower(email));
CREATE UNIQUE INDEX uk_colab_email_principal ON recursos_humanos.colaboradores_emails(colaborador_id) WHERE principal;

CREATE TABLE recursos_humanos.colaborador_enderecos(
  colaborador_id UUID NOT NULL REFERENCES recursos_humanos.colaboradores(id) ON DELETE CASCADE,
  endereco_id UUID NOT NULL REFERENCES cadastros.enderecos(id) ON DELETE CASCADE,
  tipo VARCHAR(20) NOT NULL DEFAULT 'RESIDENCIAL',
  principal BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY(colaborador_id, endereco_id),
  CONSTRAINT uk_colab_end_endereco UNIQUE (endereco_id)
);
CREATE UNIQUE INDEX uk_colab_end_principal
  ON recursos_humanos.colaborador_enderecos(colaborador_id) WHERE principal;


