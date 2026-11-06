package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.planilha;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.LeitorListaCortePort;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.CaboListaCorte;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.PontaConector;

/**
 * Lê a aba de cabos da lista de corte IT002 com o Apache POI, com as mesmas regras do leitor do worker
 * ({@code lanx_desenho/lista_corte/leitor.py}): a aba é a que tem o cabeçalho CIRC, COMP, TIPO, TERMINAL A,
 * CONECT A e CONECT B; células mescladas valem para todas as linhas da mescla; linhas "NOTA..." são
 * ignoradas e "LEGENDA" encerra a tabela.
 */
@Component
class ListaCortePoiReader implements LeitorListaCortePort {

    private static final List<String> CABECALHO = List.of("CIRC", "COMP", "TIPO", "TERMINALA", "CONECTA",
            "CONECTB");
    /** Linhas lidas, no máximo, procurando o cabeçalho (o formulário tem ~6 linhas de título). */
    private static final int LINHAS_CABECALHO = 30;

    /** "CNFAT04CZ (C1)" */
    private static final Pattern CONECTOR = Pattern.compile("^\\s*([^()]+?)\\s*\\(([^)]+)\\)\\s*$");
    /** "PONTA LIVRE PT1": o mesmo que "PONTA LIVRE (PT1)" */
    private static final Pattern PONTA_LIVRE = Pattern.compile("^\\s*PONTA\\s+LIVRE\\s+([A-Z]{1,3}\\d+[A-Z]?)\\s*$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public List<CaboListaCorte> lerCabos(InputStream xlsx) {
        try (Workbook wb = WorkbookFactory.create(xlsx)) {
            List<CaboListaCorte> cabos = new ArrayList<>();
            boolean achouAba = false;
            for (Sheet sheet : wb) {
                var aba = new Aba(sheet);
                Map<String, Integer> col = aba.cabecalho();
                if (col == null)
                    continue;
                achouAba = true;
                cabos.addAll(aba.cabos(col));
            }
            if (!achouAba)
                throw new DesenhoInvalidoException("Lista de corte sem a aba de cabos: não encontrei o cabeçalho "
                        + "CIRC, COMP, TIPO, TERMINAL A, CONECT A, CONECT B.");
            return cabos;
        } catch (DesenhoInvalidoException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            // arquivo que não é xlsx, corrompido ou protegido
            throw new DesenhoInvalidoException("Não foi possível ler a lista de corte. Confira se é um .xlsx válido.");
        }
    }

    /** Uma aba com as mesclas já resolvidas: célula dentro de uma mescla devolve o valor do canto. */
    private static final class Aba {

        private final Sheet sheet;
        private final DataFormatter fmt = new DataFormatter(Locale.ROOT);
        private final Map<Long, CellRangeAddress> mesclas = new HashMap<>();

        Aba(Sheet sheet) {
            this.sheet = sheet;
            fmt.setUseCachedValuesForFormulaCells(true);
            for (CellRangeAddress m : sheet.getMergedRegions())
                for (int r = m.getFirstRow(); r <= m.getLastRow(); r++)
                    for (int c = m.getFirstColumn(); c <= m.getLastColumn(); c++)
                        mesclas.put(chave(r, c), m);
        }

        Map<String, Integer> cabecalho() {
            int ultima = Math.min(sheet.getLastRowNum(), LINHAS_CABECALHO);
            for (int r = sheet.getFirstRowNum(); r <= ultima; r++) {
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;
                Map<String, Integer> col = new HashMap<>();
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    String nome = normalizar(texto(r, c));
                    if (!nome.isEmpty())
                        col.putIfAbsent(nome, c);
                }
                if (col.keySet().containsAll(CABECALHO)) {
                    col.put("_LINHA", r);
                    return col;
                }
            }
            return null;
        }

        List<CaboListaCorte> cabos(Map<String, Integer> col) {
            List<CaboListaCorte> out = new ArrayList<>();
            for (int r = col.get("_LINHA") + 1; r <= sheet.getLastRowNum(); r++) {
                String primeira = texto(r, 0);
                String circ = texto(r, col.get("CIRC"));
                if (primeira.toUpperCase().startsWith("NOTA"))
                    continue;
                if (normalizar(primeira).equals("LEGENDA") || normalizar(circ).equals("LEGENDA"))
                    break; // rodapé do formulário
                if (circ.isEmpty())
                    continue;
                out.add(new CaboListaCorte(circ, sheet.getSheetName(), r + 1,
                        ponta(texto(r, col.get("CONECTA")), texto(r, col.getOrDefault("CAVA", -1))),
                        ponta(texto(r, col.get("CONECTB")), texto(r, col.getOrDefault("CAVB", -1)))));
            }
            return out;
        }

        String texto(int r, int c) {
            if (c < 0)
                return "";
            CellRangeAddress m = mesclas.get(chave(r, c));
            if (m != null) {
                r = m.getFirstRow();
                c = m.getFirstColumn();
            }
            Row row = sheet.getRow(r);
            Cell cell = row == null ? null : row.getCell(c);
            return cell == null ? "" : fmt.formatCellValue(cell).trim();
        }

        private static long chave(int r, int c) {
            return ((long) r << 20) | c;
        }
    }

    /** Emenda (BONDER), ponta sem referência ou texto vazio → {@code null}: não é conector. */
    static PontaConector ponta(String conector, String cavidade) {
        var livre = PONTA_LIVRE.matcher(conector);
        if (livre.matches())
            return new PontaConector("PONTA LIVRE", livre.group(1), cavidade);
        var m = CONECTOR.matcher(conector);
        if (!m.matches())
            return null;
        return new PontaConector(m.group(1), m.group(2), cavidade);
    }

    /** "TERMINAL A" → "TERMINALA", "CAV. A" → "CAVA" (igual ao _norm do worker). */
    static String normalizar(String s) {
        return s.replaceAll("[\\s.:]", "").toUpperCase();
    }
}
