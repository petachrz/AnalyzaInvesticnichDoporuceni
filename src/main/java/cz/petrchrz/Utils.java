package cz.petrchrz;

import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Pomocná třída pro prací s Excel soubory a časovými údaji
 * 
 * @author Petr Chrz
 */ 
public class Utils {

    /**
     * Definice časových jednotek
     */
    public enum TimeUnit {DAY, YEAR}

    /** Vytváření instance pro formátování časových údajů (formát dd-MMM-yyyy) */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy",  new Locale.Builder().setLanguage("cs").setRegion("CZ").build());
    
    /**
     * Zpracování souboru Excel s požadovaným listem
     * 
     * @param filePath Cesta k Excel souboru
     * @param sheetName Název konkrétního listu v souboru
     * @return zpracovaný Excel list
     * @throws Exception Pokud list s daným názvem neexistuje nebo nelze soubor načíst
     * 
     */
    public static Sheet processSheet(String filePath, String sheetName) throws Exception {
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new Exception("List " + sheetName + " neexistuje v souboru.");
            }
            return sheet;
        }
    }

    /**
     * Posune zadaný datum o určitý počet dní či let
     *
     * @param date  Výchozí datum
     * @param unit  Časová jednotka (DAY nebo YEAR)
     * @param amount Počet jednotek, o které se má datum posunout (může být kladný i záporný)
     * @return Nový datum, posunutý o zadaný počet dnů/let
     * @throws IllegalArgumentException Pokud je zadána neznámá časová jednotka
     */
    public static Date calculateDate(Date date, TimeUnit unit, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch (unit) {
            case DAY:
                calendar.add(Calendar.DAY_OF_MONTH, amount);
                break;
            case YEAR:
                calendar.add(Calendar.YEAR, amount);
                break;
            default:
                throw new IllegalArgumentException("Neznámá časová jednotka: " + unit);
        }
        return calendar.getTime();
    }

    public static SimpleDateFormat getSimpleDateFormat() {
        return sdf;
    }
}