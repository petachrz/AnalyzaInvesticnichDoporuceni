package cz.petrchrz;

import org.apache.poi.ss.usermodel.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Třída pro načítání tržních dat akcií z Excel souboru.
 * 
 * Knihovna Apache POI.
 * 
 * Data jsou načítána do dvourozměrneho pole {@code stockData[][]}.
 * Pro rychlé vyhledání tržních dat pro konkrétní den v poli je vytvořena {@code HashMap}.
 *
 * @author Petr Chrz
 *
 */
public class StockDataLoader {
    /** Cesta k Excel souboru */
    private final String filePath;
    
    /** Název listu, ze kterého se data načítají */
    private final String sheetName;

    /** Tržní data ve formě dvourozměrného pole, kde: 
     * <ul>
     *     <li><b>stockData[i][0]</b> - {@link Date} - 0  Datum</li>
     *     <li><b>stockData[i][1]</b> - {@code int} - 1 Open (otevírací cena)</li>
     *     <li><b>stockData[i][2]</b> - {@code int} - 2 Close (zavírací cena)</li>
     *     <li><b>stockData[i][3]</b> - {@code int} - 3 Low (denní minimum)</li>
     *     <li><b>stockData[i][4]</b> - {@code int} - 4 High (denní maximum)</li>
     *     <li><b>stockData[i][5]</b> - {@code int} - 5 Change (změna ceny)</li>
     * </ul>
     */
    private Object[][] stockData;

    /** HashMap pro propojení dat (Date) s indexem řádku, kde 
     * * <ul>
     *     <li><b>Klíč</b> - {@link Date} - Hledaný datum </li>
     *     <li><b>Hodnota</b> - {@code int} - Index odpovídajicí řádku v poli {@code stockData[][]} s konkrétnimi údaji pro daný den </li>
     * </ul>
    */
    private Map<Date, Integer> dateIndexMap;

    public StockDataLoader(String filePath, String sheetName) {
        this.filePath = filePath;
        this.sheetName = sheetName;
    }
   
    /**
     * Načítá tržní data o dané akcii z Excel listu a ukladá je do dvourozměrného pole.
     * <p> Sloupce: 
     * <ul>
     * <li> Datum </li>
     * <li> Zahájení / Open </li>
     * <li> Závěr / Close </li>
     * <li> Denní minimum </li>
     * <li> Denní maximum </li>
     * <li> Změna ceny / change </li>
     * </ul>
     *
     */
    public void loadData() throws Exception {
        Sheet sheet = Utils.processSheet(filePath, sheetName);

        int firstRow = 1;
        int lastRow = sheet.getLastRowNum();
        int rowCount = lastRow - firstRow + 1;

        stockData = new Object[rowCount][6]; 

        for (int i = firstRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell dateCell = row.getCell(0);
            Cell openCell = row.getCell(2);
            Cell closeCell = row.getCell(3);
            Cell lowCell = row.getCell(5);
            Cell highCell = row.getCell(6);
            Cell changCell = row.getCell(4);

            if (dateCell == null || openCell == null || closeCell == null || lowCell == null || highCell == null) {
                continue;
            }

            int index = i - firstRow;

            stockData[index][0] = dateCell.getDateCellValue();
            stockData[index][1] = (int) openCell.getNumericCellValue();
            stockData[index][2] = (int) closeCell.getNumericCellValue();
            stockData[index][3] = (int) lowCell.getNumericCellValue();
            stockData[index][4] = (int) highCell.getNumericCellValue();
            stockData[index][5] = (double) changCell.getNumericCellValue();
        }
        System.out.println("List " + sheetName +  " | Načteno " + rowCount + " řádků tržních dat.");
    }
    
    /**
     * Vytváří HashMap propojující datum z prvního sloupce (sloupec A) s indexem řádku.
     * 
     * HashMap ukládá Datum pro konkrétní den tržních dat jako klíč, 
     * celé číslo (index řádku obsahující tržní data o daném dni) jako hodnotu.
     * Pro vyhledávání a procházení konkrétních tržních dat není potřeba procházet celé pole.
     * * <ul>
     *     <li><b>Klíč</b> - {@link Date} - Hledaný datum </li>
     *     <li><b>Hodnota</b> - {@code int} - Index odpovídajicí řádku poli {@code stockData[][]} s konkrétnimi údaji pro daný den </li>
     * </ul>
     */
    public void loadDateIndexMap() throws Exception {
        
        Sheet sheet = Utils.processSheet(filePath, sheetName);
        dateIndexMap = new HashMap<>();

        int firstRow = 1;
        int lastRow = sheet.getLastRowNum();

        for (int i = firstRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell dateCell = row.getCell(0);
            if (dateCell == null || dateCell.getCellType() != CellType.NUMERIC || !DateUtil.isCellDateFormatted(dateCell)) {
                continue;
            }

            Date date = dateCell.getDateCellValue();
            dateIndexMap.put(date, i - firstRow);
        }
        System.out.println("IndexMap | Načteno " + dateIndexMap.size() + " platných dat.");
    }

    public Map<Date, Integer> getDateIndexMap() {
        return dateIndexMap;
    }

    public Object[][] getStockData() {
        return stockData;
    }
}