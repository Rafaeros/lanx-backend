package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.planilha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.PontaConector;

class ListaCortePoiReaderTest {

    private final ListaCortePoiReader leitor = new ListaCortePoiReader();

    private static void linha(XSSFSheet s, int r, Object... valores) {
        var row = s.createRow(r);
        for (int c = 0; c < valores.length; c++) {
            if (valores[c] instanceof Number n)
                row.createCell(c).setCellValue(n.doubleValue());
            else if (valores[c] != null)
                row.createCell(c).setCellValue(valores[c].toString());
        }
    }

    private static ByteArrayInputStream planilha() throws IOException {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            wb.createSheet("CAPA");
            XSSFSheet s = wb.createSheet("IT CABOS");
            linha(s, 0, "IT002 - LISTA DE CORTE");
            linha(s, 4, "CIRC.", "COMP", "TIPO", "TERMINAL A", "CAV. A", "CONECT. A", "TERMINAL B", "CAV B",
                    "CONECT. B");
            linha(s, 5, "W01", 500, "CFR0050PR", "T1", 1, "CNFAT04CZ (C1)", "T2", 3, "CNMAT06CZ (C2)");
            linha(s, 6, "W02", 400, "CFR0050VM", "T1", 2, null, "", "", "BONDER VM");
            linha(s, 7, "W03", 300, "CFR0050AM", "T1", 2, "CNFAT04CZ (C1)", "", "", "PONTA LIVRE PT1");
            s.addMergedRegion(new CellRangeAddress(5, 6, 5, 5)); // CONECT A mesclado: W02 também é C1
            linha(s, 8, "NOTA 1: crimpar duplo");
            linha(s, 9, "LEGENDA");
            linha(s, 10, "W99", 1, "X", "", 9, "CNFAT04CZ (C9)");
            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Test
    void le_a_aba_de_cabos_com_mescla_e_rodape() throws IOException {
        var cabos = leitor.lerCabos(planilha());

        assertThat(cabos).extracting("circ").containsExactly("W01", "W02", "W03");
        assertThat(cabos.get(0).a()).isEqualTo(new PontaConector("CNFAT04CZ", "C1", "1"));
        assertThat(cabos.get(0).b()).isEqualTo(new PontaConector("CNMAT06CZ", "C2", "3"));
        assertThat(cabos.get(0).linha()).isEqualTo(6);
        assertThat(cabos.get(1).a()).isEqualTo(new PontaConector("CNFAT04CZ", "C1", "2"));
        assertThat(cabos.get(1).b()).isNull(); // BONDER não é conector
        assertThat(cabos.get(2).b()).isEqualTo(new PontaConector("PONTA LIVRE", "PT1", ""));
    }

    @Test
    void arquivo_que_nao_e_xlsx() {
        assertThatThrownBy(() -> leitor.lerCabos(new ByteArrayInputStream("não é planilha".getBytes())))
                .isInstanceOf(DesenhoInvalidoException.class);
    }

    @Test
    void planilha_sem_aba_de_cabos() throws IOException {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            linha(wb.createSheet("X"), 0, "qualquer coisa");
            wb.write(out);
            assertThatThrownBy(() -> leitor.lerCabos(new ByteArrayInputStream(out.toByteArray())))
                    .isInstanceOf(DesenhoInvalidoException.class).hasMessageContaining("aba de cabos");
        }
    }
}
