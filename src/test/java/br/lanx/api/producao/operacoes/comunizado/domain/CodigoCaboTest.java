package br.lanx.api.producao.operacoes.comunizado.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class CodigoCaboTest {

    private final CoresCabos cores = CoresCabos.padrao();

    private CodigoCabo ler(String tipo) {
        return CodigoCabo.ler(tipo, cores).orElseThrow();
    }

    @Test
    void decompoeModeloBitolaCorFaixaESufixo() {
        CodigoCabo simples = ler("CFR0100AM");
        assertThat(simples.modelo()).isEqualTo("CFR");
        assertThat(simples.bitola()).isEqualTo(100);
        assertThat(simples.cor().codigo()).isEqualTo("AM");
        assertThat(simples.faixa()).isNull();

        CodigoCabo bicolor = ler("CFR0035AZPR");
        assertThat(bicolor.cor().codigo()).isEqualTo("AZ");
        assertThat(bicolor.faixa().codigo()).isEqualTo("PR");
        assertThat("CFR0035AZPR".substring(bicolor.inicioDaFaixa())).isEqualTo("PR");

        // sigla composta ganha da simples: AZCL (azul claro), não AZ + CL
        CodigoCabo claro = ler("CFR0100AZCLBR");
        assertThat(claro.cor().codigo()).isEqualTo("AZCL");
        assertThat(claro.faixa().codigo()).isEqualTo("BR");

        CodigoCabo sufixo = ler("CFR0050VDSEV");
        assertThat(sufixo.cor().codigo()).isEqualTo("VD");
        assertThat(sufixo.faixa()).isNull();
        assertThat(sufixo.sufixo()).isEqualTo("SEV");
    }

    @Test
    void foraDoPadraoNaoEReconhecido() {
        assertThat(CodigoCabo.ler("SOLDA", cores)).isEmpty();
        assertThat(CodigoCabo.ler("CFR0100XX", cores)).isEmpty();
        assertThat(CodigoCabo.ler(null, cores)).isEmpty();
    }

    @Test
    void ordenaPorModeloBitolaECor() {
        List<String> tipos = new ArrayList<>(List.of("CFR0250PR", "CFR0150VM", "CFR0100VD", "CFR0150PR", "CFR0100AM"));
        tipos.sort((a, b) -> CodigoCabo.ORDEM.compare(ler(a), ler(b)));
        assertThat(tipos).containsExactly("CFR0100AM", "CFR0100VD", "CFR0150PR", "CFR0150VM", "CFR0250PR");
    }

    /** Todo cabo do estoque (docs/estoque_20_10_2026.xlsx) tem a cor mapeada em cores-cabos.csv. */
    @Test
    void todasAsCoresDoEstoqueEstaoMapeadas() throws Exception {
        List<String> semCor = new ArrayList<>();
        int total = 0;
        try (InputStream in = Files.newInputStream(Path.of("docs", "estoque_20_10_2026.xlsx"));
                var wb = new XSSFWorkbook(in)) {
            DataFormatter fmt = new DataFormatter();
            for (Row row : wb.getSheetAt(0)) {
                String codigo = fmt.formatCellValue(row.getCell(0)).trim();
                if (row.getRowNum() == 0 || !codigo.startsWith("CF")) {
                    continue;
                }
                total++;
                if (CodigoCabo.ler(codigo, cores).isEmpty()) {
                    semCor.add(codigo);
                }
            }
        }
        assertThat(total).isGreaterThan(400);
        assertThat(semCor).as("códigos sem cor mapeada").isEmpty();
    }
}
