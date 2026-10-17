CREATE SCHEMA IF NOT EXISTS auth;

CREATE SCHEMA IF NOT EXISTS cadastros;

CREATE SCHEMA IF NOT EXISTS compras;

-- ============================================
-- AUTH
-- ============================================
CREATE TABLE
    auth.users (
        id UUID PRIMARY KEY,
        username VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ
    );

CREATE TABLE
    auth.roles (
        id UUID PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE,
        description VARCHAR(255),
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ
    );

CREATE TABLE
    auth.user_roles (
        user_id UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
        role_id UUID NOT NULL REFERENCES auth.roles (id) ON DELETE CASCADE,
        PRIMARY KEY (user_id, role_id)
    );

CREATE INDEX idx_user_roles_role ON auth.user_roles (role_id);

CREATE TABLE
    auth.permissions (
        id UUID PRIMARY KEY,
        code VARCHAR(150) NOT NULL UNIQUE,
        feature_key VARCHAR(100) NOT NULL,
        action VARCHAR(50) NOT NULL,
        description VARCHAR(255) NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW ()
    );

CREATE TABLE
    auth.role_permissions (
        role_id UUID NOT NULL REFERENCES auth.roles (id) ON DELETE CASCADE,
        permission_id UUID NOT NULL REFERENCES auth.permissions (id) ON DELETE CASCADE,
        PRIMARY KEY (role_id, permission_id)
    );

CREATE INDEX idx_role_permissions_permission ON auth.role_permissions (permission_id);

-- ============================================
-- CADASTROS
-- ============================================
CREATE TABLE
    cadastros.pais (
        id UUID PRIMARY KEY,
        nome VARCHAR(200) NOT NULL,
        codigo_iso2 VARCHAR(2) NOT NULL,
        codigo_iso3 VARCHAR(3) NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        CONSTRAINT uk_pais_codigo_iso2 UNIQUE (codigo_iso2),
        CONSTRAINT uk_pais_codigo_iso3 UNIQUE (codigo_iso3)
    );

CREATE TABLE
    cadastros.unidades_medida (
        id UUID PRIMARY KEY,
        nome VARCHAR(100) NOT NULL,
        sigla VARCHAR(10) NOT NULL UNIQUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ
    );

CREATE TABLE
    cadastros.materiais (
        id UUID PRIMARY KEY,
        codigo VARCHAR(100) NOT NULL UNIQUE,
        descricao VARCHAR(500) NOT NULL,
        unidade_medida_id UUID,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ,
        CONSTRAINT fk_materiais_unidade_medida FOREIGN KEY (unidade_medida_id) REFERENCES cadastros.unidades_medida (id) ON DELETE RESTRICT
    );

CREATE INDEX idx_materiais_unidade_medida_id ON cadastros.materiais (unidade_medida_id);

CREATE INDEX idx_materiais_ativos ON cadastros.materiais (id)
WHERE
    deleted_at IS NULL;

CREATE TABLE
    cadastros.fornecedores (
        id UUID PRIMARY KEY,
        nome VARCHAR(255) NOT NULL UNIQUE,
        pais_id UUID,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        deleted_at TIMESTAMPTZ,
        CONSTRAINT fk_fornecedores_pais FOREIGN KEY (pais_id) REFERENCES cadastros.pais (id) ON DELETE RESTRICT
    );

CREATE INDEX idx_fornecedores_pais_id ON cadastros.fornecedores (pais_id);

-- ============================================
-- COMPRAS - ANÁLISE DE PEDIDOS
-- ============================================
-- Fornecedor, material e unidade de medida são normalizados nos cadastros na importação
CREATE TABLE
    compras.analise_pedidos_itens (
        id UUID PRIMARY KEY,
        material_id UUID NOT NULL,
        unidade_medida_id UUID NOT NULL,
        fornecedor_id UUID,
        requisicao_de_compra VARCHAR(50),
        solicitante VARCHAR(150),
        data_emissao_rc DATE,
        data_previsao_entrega_rc DATE,
        pedido_de_compra VARCHAR(50),
        comprador VARCHAR(150),
        data_emissao_pc DATE,
        data_previsao_entrega_pc DATE,
        data_entrega_pc DATE,
        quantidade_solicitada DECIMAL(19, 4),
        quantidade_atendida DECIMAL(19, 4),
        saldo DECIMAL(19, 4),
        local_ VARCHAR(50),
        valor_unitario DECIMAL(19, 4),
        valor_frete DECIMAL(19, 4),
        ipi DECIMAL(19, 4),
        valor_total DECIMAL(19, 4),
        condicao_pagamento VARCHAR(100),
        ncm VARCHAR(20),
        recebimento VARCHAR(50),
        nota_fiscal VARCHAR(50),
        quantidade_recebimentos INT,
        rateio VARCHAR(100),
        referencia VARCHAR(100),
        invoice VARCHAR(100),
        responsavel_compras VARCHAR(150),
        observacao TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_analise_pedido_material FOREIGN KEY (material_id) REFERENCES cadastros.materiais (id) ON DELETE RESTRICT,
        CONSTRAINT fk_analise_pedido_unidade_medida FOREIGN KEY (unidade_medida_id) REFERENCES cadastros.unidades_medida (id) ON DELETE RESTRICT,
        CONSTRAINT fk_analise_pedido_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES cadastros.fornecedores (id) ON DELETE RESTRICT
    );

CREATE INDEX idx_analise_pedido_material_id ON compras.analise_pedidos_itens (material_id);

CREATE INDEX idx_analise_pedido_unidade_medida_id ON compras.analise_pedidos_itens (unidade_medida_id);

CREATE INDEX idx_analise_pedido_fornecedor_id ON compras.analise_pedidos_itens (fornecedor_id);

CREATE INDEX idx_analise_pedido_data_emissao_pc ON compras.analise_pedidos_itens (data_emissao_pc);

CREATE INDEX idx_analise_pedido_pedido_de_compra ON compras.analise_pedidos_itens (pedido_de_compra);

CREATE INDEX idx_analise_pedido_requisicao ON compras.analise_pedidos_itens (requisicao_de_compra);