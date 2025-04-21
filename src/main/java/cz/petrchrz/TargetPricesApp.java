package cz.petrchrz;

import java.util.Scanner;

/**
 * Program pro analýzu cílových cen akcií.
 * <p>
 * Program načte data cílových cen a tržních hodnot z Excel souboru,
 * provede analýzu, zjistí, zda bylo dosaženo cílové ceny v daném období,
 * a vypočítá doplňující informace, jako je počet obchodních dní, průměrné ceny,
 * volatilita a další ukazatele. Výsledky analýzy jsou následně exportovány
 * do CSV souboru.
 * <p>
 *  ** Hlavní kroky programu: **
 * <ul>
 *   <li>Načtení cílových cen (datum vydání, cena, vydavatel).</li>
 *   <li>Načtení tržních dat (otevírací cena, zavírací cena, maximum, minimum, denní změna ceny).</li>
 *   <li>Analýza dosažení cílové ceny v rámci platného období.</li>
 *   <li>Výpočet doplňujicích údajů (počet obchodních dní, volatilita, maxima a minima a další).</li>
 *   <li>Uložení výsledků do CSV souboru.</li>
 * </ul>
 * <p>
 *  ** Požadavky na vstupní data: **
 * <ul>
 *   <li>Data musí být dostatečně očištěná – odstraněny neplatné, nekompletní nebo prázdné řádky.</li>
 *   <li>Formát dat musí odpovídat očekávané struktuře (datumy, čísla, správně rozdělené sloupce).</li>
 *   <li>Tržní data musí obsahovat dostatečné historické období pro provedení analýzy.</li>
 * </ul>
 * <p>
 * @author Petr Chrz
 * @since 2.3.2025
 */
public class TargetPricesApp {
    private static final String filePath = ExcelConfig.getFilePath();
    private static final String[] TPsheetNames = ExcelConfig.getTPsheetNames();
    private static final String[] StocksheetNames = ExcelConfig.getStocksheetNames();

    /**
     * Hlavní metoda aplikace.  
     * <p>
     * Pro spuštění analýzy je potřeba potvrzení zadáním symbolu "Y".  
     * Pokud uživatel zadá "Y", spustí se analýza prostřednictvím metody {@link #analysisStart()}.  
     * Výsledek analýzy (úspěšná/neúspěšná) je vypsán do konzole.  
     * <p>
     * Pokud je analýza úspěšně provedena, je v adresáři programu vytvořen CSV soubor s výsledky.  
     * Tento soubor je možné importovat například do Microsoft Excel pro další analýzu a vizualizaci.  
     *
     */
    public static void main(String[] args) {
        
        System.out.println("Hello World ");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Chcete zahájit analýzu? (Y pro ano): ");
        String input = scanner.nextLine().trim().toUpperCase();

        if ("Y".equals(input)) {
            System.out.println("Program zahájen...");
            boolean success = analysisStart();
            System.out.println("Program proběhl " + (success ? " úspěšně." : "neúspeěně."));
        } else {
            System.out.println("Analýza zrušena.");
        }

        scanner.close();
    }

    /**
     * Spustí analýzu pro každý definovaný list s cílovými cenami a tržními daty.
     * <p>
     * Provede:
     * <ul>
     *   <li>Vytvoření výstupního CSV (pokud neexistuje).</li>
     *   <li>Inicializaci {@code AnalysisResult} a {@code TargetPricesLoader} 
     *       pro načtení cílových cen.</li>
     *   <li>Inicializaci {@code StockDataLoader} pro načtení tržních dat.</li>
     *   <li>Vytvoření instance {@code TargetPricesAnalysis}, nastavení dat a analýzu.</li>
     *   <li>Uzavření CSV souboru.</li>
     * </ul>
     * 
     * Při chybě se analýza přeruší a metoda vrátí {@code false}.
     *
     * @return {@code true} pokud proběhla analýza u všech listů úspěšně, jinak {@code false}.
     */
    private static boolean analysisStart() {

        for (int i = 0; i < TPsheetNames.length; i++) {
            
            System.out.println("Analýza cílových cen " + TPsheetNames[i] + " probíhá...");
           
            try {
                String sheetName = TPsheetNames[i] + "_analyza.csv";
    
                CsvWriter csvWriter = new CsvWriter(sheetName);
    
                AnalysisResult result = new AnalysisResult();
 
                TargetPricesLoader targetPricesLoader = new TargetPricesLoader(filePath, TPsheetNames[i]);
                targetPricesLoader.loadTargetPrices();
    
                StockDataLoader stockDataLoader = new StockDataLoader(filePath, StocksheetNames[i]);
                stockDataLoader.loadData();
                stockDataLoader.loadDateIndexMap();
    
                TargetPricesAnalysis analysis = new TargetPricesAnalysis(csvWriter, result);
                analysis.setTargetData(targetPricesLoader.getTargetDates(), targetPricesLoader.getTargetPrices(), targetPricesLoader.getCompanies());
                analysis.setStockData(stockDataLoader.getStockData());
                analysis.setDateIndexMap(stockDataLoader.getDateIndexMap());
    
                // analysis.testMarketData();
                // analysis.testTargetPrices();
    
                analysis.analyze();
    
                csvWriter.close();
    
            } catch (Exception e) {
                System.err.println("Chyba: " + e.getMessage());
                return false;
            }
            System.out.println("Analýza cílových cen " + TPsheetNames[i] + " dokončena.");
        }
        return true;
    }
}





