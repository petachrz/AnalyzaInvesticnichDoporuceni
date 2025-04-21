package cz.petrchrz;

/**
 * Konfigurační třída pro práci s Excel souborem.
 * 
 * @author Petr Chrz
 */
public class ExcelConfig {

    /** Cesta k Excel souboru */
    private static final String filePath = "Cilove_ceny_CHRZ_data.xlsx";

    /** Názvy listů obsahujících cílové ceny akcií */
    private static final String[] TPsheetNames = {"CEZ_ciloveCeny", "ERSTE_ciloveCeny", "KB_ciloveCeny", "TABAK_ciloveCeny","VIG_ciloveCeny"};

    /** Názvy listů obsahujících historická data o vývoji akcií */
    private static final String[] StocksheetNames = {"CEZ_vyvoj","ERSTE_vyvoj","KB_vyvoj","TABAK_vyvoj","VIG_vyvoj"};


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
