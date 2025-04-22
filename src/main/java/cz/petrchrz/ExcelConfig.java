package cz.petrchrz;

/**
 * Konfigurační třída pro práci s Excel souborem.
 * 
 * @author Petr Chrz
 */
public class ExcelConfig {

    /** Cesta k Excel souboru */
    private static final String filePath = "NAZEV_ZDROJOVEHO_SOUBORU.xlsx";

    /** Názvy listů obsahujících cílové ceny akcií */
    private static final String[] TPsheetNames = {"NAZVY_LISTU_S_CILOVYMI_CENAMI"};

    /** Názvy listů obsahujících historická data o vývoji akcií */
    private static final String[] StocksheetNames = {"NAZVY_LISTU_S_TRZNIMI_DATY"};


    public static String getFilePath() {
        return filePath;
    }

    public static String[] getTPsheetNames() {
        return TPsheetNames;
    }

    public static String[] getStocksheetNames() {
        return StocksheetNames;
    }
}
