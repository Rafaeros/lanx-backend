package br.lanx.api.producao.operacoes.comunizado;

import javax.xml.namespace.QName;

import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;

/**
 * Preenchimento em gradiente horizontal de duas cores (cabo bicolor: cor →
 * faixa). O poi-ooxml-lite não traz as classes tipadas de
 * {@code <gradientFill>}, então o elemento é montado pelo XmlCursor com o
 * mesmo XML que o Excel grava:
 *
 * <pre>
 * &lt;fill&gt;&lt;gradientFill degree="0"&gt;
 *   &lt;stop position="0"&gt;&lt;color rgb="FF00B050"/&gt;&lt;/stop&gt;
 *   &lt;stop position="1"&gt;&lt;color rgb="FFFFC000"/&gt;&lt;/stop&gt;
 * &lt;/gradientFill&gt;&lt;/fill&gt;
 * </pre>
 */
final class PreenchimentoGradiente {

    private static final String NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final QName GRADIENTE = new QName(NS, "gradientFill");
    private static final QName PARADA = new QName(NS, "stop");
    private static final QName COR = new QName(NS, "color");

    private PreenchimentoGradiente() {
    }

    /** Aplica o gradiente {@code de} → {@code para} (hex RGB) ao estilo. */
    static void aplicar(XSSFCellStyle estilo, StylesTable estilos, String de, String para) {
        CTFill fill = CTFill.Factory.newInstance();
        // O StylesTable reaproveita um preenchimento "igual" já cadastrado, e o equals do
        // POI só compara cor de frente, de fundo e tipo de padrão (vazios num gradiente):
        // todo gradiente pareceria o primeiro (PRLR e VMBR saíam com as cores do PRLR).
        // Por isso o fill entra com um marcador único (as duas cores) e só depois de
        // cadastrado o marcador vira o gradiente.
        var marcador = fill.addNewPatternFill();
        marcador.addNewFgColor().setRgb(argb(de));
        marcador.addNewBgColor().setRgb(argb(para));
        int id = estilos.putFill(new XSSFCellFill(fill, estilos.getIndexedColors()));

        fill.unsetPatternFill();
        try (XmlCursor cursor = fill.newCursor()) {
            cursor.toEndToken();
            cursor.beginElement(GRADIENTE);
            cursor.insertAttributeWithValue("degree", "0");
            parada(cursor, "0", de);
            parada(cursor, "1", para);
        }
        estilo.getCoreXf().setFillId(id);
        estilo.getCoreXf().setApplyFill(true);
    }

    private static byte[] argb(String hex) {
        return new byte[] { (byte) 0xFF,
                (byte) Integer.parseInt(hex.substring(0, 2), 16),
                (byte) Integer.parseInt(hex.substring(2, 4), 16),
                (byte) Integer.parseInt(hex.substring(4, 6), 16) };
    }

    private static void parada(XmlCursor cursor, String posicao, String hex) {
        cursor.beginElement(PARADA);
        cursor.insertAttributeWithValue("position", posicao);
        cursor.beginElement(COR);
        cursor.insertAttributeWithValue("rgb", "FF" + hex);
        cursor.toNextToken(); // sai de <color/>
        cursor.toNextToken(); // sai de <stop>
    }

    /** Clareia a cor (mistura com branco) para o texto continuar legível por cima. */
    static String clarear(String hex, double quanto) {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i += 2) {
            int c = Integer.parseInt(hex.substring(i, i + 2), 16);
            sb.append(String.format("%02X", (int) Math.round(c + (255 - c) * quanto)));
        }
        return sb.toString();
    }
}
