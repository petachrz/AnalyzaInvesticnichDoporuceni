package cz.petrchrz;

import org.apache.poi.ss.usermodel.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Třída pro načítání cílových cen akcií a příslušných informací z Excel souboru.
 * <p>
 * Knihovna Apache POI.
 * <p>
 * Data jsou ukládána do polí {@code targetDates[]}, {@code targetPrices[]} a {@code companies[]}.
 *
 * @author Petr Chrz
 * 
 */
public class TargetPricesLoader {
    
    /** Cesta k Excel souboru obsahujícímu cílové ceny */
    private final String filePath;
    
    /** Název listu v Excel souboru, ze kterého se data načítají */
    private final String sheetName;
    
    /** Pole obsahující data vydání cílových cen */
    private Date[] targetDates;
    
    /** Pole obsahující cílové ceny */
    private int[] targetPrices;
    
    /** Pole obsahující názvy společností, ke kterým se cílové ceny vztahují */
    private String[] companies;
    
    /** Objekt pro formátování dat získaných z Excel souboru */
    private final SimpleDateFormat sdf;

    public TargetPricesLoader(String filePath, String sheetName) {
        this.filePath = filePath;
        this.sheetName = sheetName;
        this.sdf = Utils.getSimpleDateFormat(); 
    }

     /**
      * Načte cílové ceny z Excel souboru a uloží je do interních polí.
      * <p>Zpracovává sloupce: <ul>
      * <li> Sloupec 0: Datum vydání cílové ceny </li>
      * <li> Sloupec 1: Název vydavatelské společnosti</li>
      * <li> Sloupec 4: Cílová cena akcie </li>
      - </ul>
      */
    public void loadTargetPrices() throws Exception {
        Sheet sheet = Utils.processSheet(filePath, sheetName);

        int firstRow = 1; 
        int lastRow = sheet.getLastRowNum();
        int rowCount = lastRow - firstRow + 1;

        targetDates = new Date[rowCount]; 
        targetPrices = new int[rowCount];
        companies = new String[rowCount];

        for (int i = firstRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell dateCell = row.getCell(0); 
            Cell priceCell = row.getCell(4);
            Cell companyCell = row.getCell(1);

            if (dateCell == null || priceCell == null || companyCell == null) {
                continue;
            }

            int index = i - firstRow;

            try {
                targetDates[index] = sdf.parse(dateCell.toString());
            } catch (ParseException e) {
                System.err.println("Chyba při parsování data v řádku " + (i + 1) + ": " + dateCell.toString());
                targetDates[index] = null; 
            }

            targetPrices[index] = (int) priceCell.getNumericCellValue();
            companies[index] = (String) companyCell.getStringCellValue(); 
        }

        System.out.println("List " + sheetName + " | Načteno " + rowCount + " cílových cen.");
    }

    public Date[] getTargetDates() { 
        return targetDates;
    }

    public int[] getTargetPrices() {
        return targetPrices;
    }

    public String[] getCompanies(){
        return companies;
    }
}
