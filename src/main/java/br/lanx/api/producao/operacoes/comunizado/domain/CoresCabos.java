package br.lanx.api.producao.operacoes.comunizado.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Mapeamento sigla → cor dos cabos, lido do CSV
 * resources/producao/comunizado/cores-cabos.csv. Para incluir ou ajustar uma
 * cor basta editar o arquivo.
 */
public final class CoresCabos {

    public static final String ARQUIVO = "/producao/comunizado/cores-cabos.csv";
    private static final Pattern HEX = Pattern.compile("^[0-9A-Fa-f]{6}$");

    /** Mais longas primeiro: "AZCL" precisa ganhar de "AZ". */
    private final List<CorCabo> porTamanho;

    private CoresCabos(List<CorCabo> cores) {
        this.porTamanho = cores.stream()
                .sorted(Comparator.comparingInt((CorCabo c) -> c.codigo().length()).reversed())
                .toList();
    }

    /** Mapeamento padrão do classpath (carregado uma vez). */
    public static CoresCabos padrao() {
        return Padrao.INSTANCIA;
    }

    private static final class Padrao {
        private static final CoresCabos INSTANCIA = carregar();

        private static CoresCabos carregar() {
            try (InputStream in = CoresCabos.class.getResourceAsStream(ARQUIVO)) {
                if (in == null) {
                    throw new IllegalStateException("Mapeamento de cores não encontrado: " + ARQUIVO);
                }
                return ler(in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /** CSV com ';': codigo;nome;texto;fundo;negrito[;faixa]. Linhas com '#' e o cabeçalho são ignorados. */
    public static CoresCabos ler(InputStream csv) throws IOException {
        List<CorCabo> cores = new ArrayList<>();
        try (var reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            String linha;
            int numero = 0;
            while ((linha = reader.readLine()) != null) {
                numero++;
                String limpa = linha.strip();
                if (limpa.isEmpty() || limpa.startsWith("#") || limpa.toLowerCase(Locale.ROOT).startsWith("codigo;")) {
                    continue;
                }
                String[] c = limpa.split(";", -1);
                if (c.length < 5 || c[0].isBlank() || !HEX.matcher(c[2].strip()).matches()
                        || (!c[3].isBlank() && !HEX.matcher(c[3].strip()).matches())
                        || (c.length > 5 && !c[5].isBlank() && !HEX.matcher(c[5].strip()).matches())) {
                    throw new IllegalStateException(
                            "Linha " + numero + " inválida em " + ARQUIVO + ": '" + linha + "'");
                }
                cores.add(new CorCabo(
                        c[0].strip().toUpperCase(Locale.ROOT),
                        c[1].strip(),
                        c[2].strip().toUpperCase(Locale.ROOT),
                        c[3].isBlank() ? null : c[3].strip().toUpperCase(Locale.ROOT),
                        c[4].strip().equalsIgnoreCase("sim"),
                        c.length > 5 && !c[5].isBlank() ? c[5].strip().toUpperCase(Locale.ROOT)
                                : c[2].strip().toUpperCase(Locale.ROOT),
                        cores.size()));
            }
        }
        return new CoresCabos(cores);
    }

    /** A cor (mais longa) com que o texto começa: "AZCLBR" → AZCL. */
    public Optional<CorCabo> prefixo(String texto) {
        if (texto == null || texto.isEmpty()) {
            return Optional.empty();
        }
        return porTamanho.stream().filter(c -> texto.startsWith(c.codigo())).findFirst();
    }

    public List<CorCabo> todas() {
        return porTamanho.stream().sorted(Comparator.comparingInt(CorCabo::ordem)).toList();
    }
}
