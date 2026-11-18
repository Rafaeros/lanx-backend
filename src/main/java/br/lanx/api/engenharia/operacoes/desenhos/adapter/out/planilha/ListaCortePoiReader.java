package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.planilha;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
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
    /** Outro nome para a mesma coluna: algumas listas chamam o CIRC de GRAVAÇÃO (já sem acento). */
    private static final Map<String, String> SINONIMOS = Map.of("GRAVACAO", "CIRC");
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
                // GRAVAÇÃO vale como CIRC só se a linha não tiver CIRC (com as duas, vale CIRC)
                SINONIMOS.forEach((sinonimo, oficial) -> {
                    if (col.containsKey(sinonimo))
                        col.putIfAbsent(oficial, col.get(sinonimo));
                });
                if (col.keySet().containsAll(CABECALHO)) {
                    col.put("_LINHA", r);
                    return col;
                }
            }
            return null;
        }

        /**
         * A CAV desta linha continua uma célula mesclada que começou numa linha de cima: a engenharia juntou as
         * ligações no MESMO pino (ex.: malha soldada com o terra). A ponta conta uma vez só, na 1ª linha.
         */
        boolean mesmoPino(int r, int c) {
            if (c < 0)
                return false;
            CellRangeAddress m = mesclas.get(chave(r, c));
            return m != null && m.getFirstRow() < r;
        }

                List<CaboListaCorte> cabos(Map<String, Integer> col) {
            List<CaboListaCorte> out = new ArrayList<>();
            Map<String, String> pinoDoCabo = new HashMap<>(); // "CN1|6" → cabo-base que já usa o pino
            for (int r = col.get("_LINHA") + 1; r <= sheet.getLastRowNum(); r++) {
                String primeira = texto(r, 0);
                String circ = texto(r, col.get("CIRC"));
                if (primeira.toUpperCase().startsWith("NOTA"))
                    continue;
                if (normalizar(primeira).equals("LEGENDA") || normalizar(circ).equals("LEGENDA"))
                    break; // rodapé do formulário
                if (circ.isEmpty())
                    continue;
                String tipo = texto(r, col.getOrDefault("TIPO", -1)).trim().toUpperCase();
                PontaConector a = mesmoPino(r, col.getOrDefault("CAVA", -1)) ? null
                        : ponta(texto(r, col.get("CONECTA")), texto(r, col.getOrDefault("CAVA", -1)));
                PontaConector b = mesmoPino(r, col.getOrDefault("CAVB", -1)) ? null
                        : ponta(texto(r, col.get("CONECTB")), texto(r, col.getOrDefault("CAVB", -1)));
                a = malhaNoPinoDoCabo(a, tipo, pinoDoCabo);
                b = malhaNoPinoDoCabo(b, tipo, pinoDoCabo);
                out.add(new CaboListaCorte(circ, sheet.getSheetName(), r + 1, a, b));
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
    /** Cabo manga multivias: "CBM6V26AWG24AWG01-VD" → "CBM6V26AWG24AWG01" (a via/cor vem depois do hífen). */
    static String caboBase(String tipo) {
        int i = tipo.lastIndexOf('-');
        return i > 0 ? tipo.substring(0, i) : tipo;
    }

    /**
     * Malha ("-MLH") do cabo manga soldada no mesmo pino de uma via do MESMO cabo (junto com o terra): é o
     * mesmo pino, não cavidade duplicada. Registra o pino de cada ponta para as linhas seguintes.
     */
    static PontaConector malhaNoPinoDoCabo(PontaConector p, String tipo, Map<String, String> pinoDoCabo) {
        if (p == null)
            return null;
        String pino = p.ref() + "|" + p.cavidade().replaceFirst("^0+(?=\\d)", "");
        String base = caboBase(tipo);
        if (tipo.endsWith("-MLH") && base.equals(pinoDoCabo.get(pino)))
            return null;
        pinoDoCabo.putIfAbsent(pino, base);
        return p;
    }

    /** Sem espaço, ponto, dois-pontos nem acento, em maiúsculas: "Gravação" → "GRAVACAO". */
    static String normalizar(String s) {
        String semAcento = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.replaceAll("[\\s.:]", "").toUpperCase();
    }
}
