package cz.petrchrz;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Hlavní třída pro analýzu cílových cen.
 * 
 * Obsahuje hlavní metody:
 * 
 *<ul>
    <li><code>analyze()</code>: Provede kompletní analýzu cílových cen a tržních dat
        (vyhledá relevantní indexy, spočítá počet obchodních dní, zkontroluje 
        dosažení cílové ceny a vypočítá doplňující statistiky).</li>
    <li><code>checkTargetPrice(int startIndex, int finalIndex)</code>: Zjistí, zda byla cílová cena dosažena</li>
    <li><code>calculateStats(int startIndex, int finalIndex)</code>: Vypočítá doplňující údaje (max, min, průměrné ceny, volatilitu) a uloží je do <code>AnalysisResult</code></li>
</ul>
 * Výsledky analýzy jsou uloženy do objektu {@code AnalysisResult} a exportovány do CSV
 * pomocí instance {@code CsvWriter}.
 * 
 * @author Petr Chrz
 */
public class TargetPricesAnalysis { 
    /** Pole obsahující datumy vydání cílových cen */
    private Date[] targetDates;
    /** Pole obsahující cílové ceny */
    private int[] targetPrices;
    /** Pole obsahující názvy vydavatelských společností */
    private String[] companies;
// === Index 0 targetDates odpovídá indexu 0 targetPrices a companies === 

/**
    * Reprezentuje tržní data, kde
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
    
    /** HashMap pro propojení dat (Date) s indexem řádku.
     * <ul>
     *     <li><b>Klíč</b> - {@link Date} - Hledaný datum </li>
     *     <li><b>Hodnota</b> - {@code int} - Index odpovídající řádku v poli {@code stockData[][]} s konkrétnimi údaji pro daný den</li>
     * </ul>
    */
    private Map<Date, Integer> dateIndexMap;
   
    /** Objekt pro zapisování výsledků do csv souboru */
    private final CsvWriter csv;
    
    /** Objekt pro ukládaní výsledků analýzy */
    private final AnalysisResult result;
   
    /** Objekt pro formátovaní časových údajů */
    private final SimpleDateFormat sdf;
     
    /**
     * Index cílové ceny, který právě zpracováváme (odpovídá prvkům v targetDates,
     * targetPrices a companies)
     */
    private Integer targetIndex;

    /** Počáteční index v poli (odpovídá počátečnímu příslušnému dni sledovaného období) */
    private Integer startIndex;

    /** Konečný index v poli (odpovídá poslednímu příslušnému dni sledovaného období) */
    private Integer finalIndex;

    /** Dočasná proměnná pro vyhledání nejbližšího data */
    private Date tempDate;

    /** Maximální přípustný datum pro provedení analýzy */
    private final Date limitDate;

    /** Dočasný index pro vyhledávání dat */
    private Integer findIndex;
   
    /** Počáteční zkoumaný obchodní den */ 
    private Date startDate;
    
    /** Poslední zkoumaný obchodní den */
    private Date finalDate;

    public TargetPricesAnalysis(CsvWriter csvWriter, AnalysisResult result) {
        this.result = result;
        this.csv = csvWriter;
        this.sdf = Utils.getSimpleDateFormat();
       
        try {
            this.limitDate = sdf.parse("01-úno-2025");
        } catch (ParseException e) {
            throw new RuntimeException("Chyba při parsování limitDate: " + e.getMessage(), e);
        }
    }

    public void setTargetData(Date[] dates, int[] prices, String[] companies) {
        this.targetDates = dates;
        this.targetPrices = prices;
        this.companies = companies;
    }

    public Date[] getTargetDates() {
        return targetDates;
    }

    public int[] getTargetPrices() {
        return targetPrices;
    }

    public void setStockData(Object[][] data) {
        this.stockData = data;
    }

    public Object[][] getStockData() {
        return stockData;
    }

    public void setDateIndexMap(Map<Date, Integer> map) {
        this.dateIndexMap = map;
    }


    /**
    * Spustí analýzu cílových cen.
    * <p>
    * Metoda postupně volá dílčí metody pro validaci, dat, výpočet indexů,
    * kontrolu dosažení cílové ceny, výpočet statistik a zápis výsledků do CSV.
    * <p>.
    */
    public void analyze() {
       
        try {
            if (!validateData()) {
                return;
            }

            for (int targetIndex = 0; targetIndex < targetDates.length; targetIndex++) {

                 this.targetIndex = targetIndex;
                 this.startDate = targetDates[targetIndex];
                 this.finalDate = Utils.calculateDate(startDate, Utils.TimeUnit.YEAR, 1);

            if (finalDate.after(limitDate)) {
                continue;
            }

                findIndexes(targetIndex, startDate, finalDate); 
                
                if ((int)targetPrices[targetIndex] == (int) stockData[startIndex][1]) {
                    continue;
                }

                calculateDays(startIndex, finalIndex);
                checkTargetPrice(startIndex, finalIndex);
                calculateStats(startIndex, finalIndex);
                csv.writeData(result.toCsv() + ";" + result.csvAddStats());
        }

        } catch (Exception e) {
            System.err.println("Chyba při analýze: " + e.getMessage());
        }
    }

    /**
     * Zjistí, zda byla cílová cena dosažena ve vymezeném období zadanými indexy.
     * 
     * @param startIndex Počáteční index v poli stockData (odpovídá počátečnímu zkoumanému dni).
     * @param finalIndex Konečný index v poli stockData (odpovídá poslednímu zkoumanému dni).
     */
    public void checkTargetPrice(int startIndex, int finalIndex) {
        if (!validateIndexes(startIndex, finalIndex)) return;

        int targetPrice = targetPrices[targetIndex];
        int days = 0;

        if (targetPrice < (int) stockData[startIndex][1]) {

            for (int i = startIndex; i >= finalIndex; i--) {
                if ((int) stockData[i][3] <= targetPrice) {

                    sendResult(targetDates[targetIndex], targetPrice, companies[targetIndex],
                    "Nižší", (int) stockData[startIndex][1], 
                    true, (Date) stockData[i][0], days, 
                    targetPrice, (int) stockData[finalIndex][2]);       
                    return;
                }
                days++;
            }
            sendResult(targetDates[targetIndex], targetPrice, companies[targetIndex], 
            "Nižší", (int) stockData[startIndex][1], 
            false, (Date) stockData[finalIndex][0], days, 
            (int) stockData[finalIndex][2], (int) stockData[finalIndex][2]);       
        }
         else {
            for (int i = startIndex; i >= finalIndex; i--) {

                if ((int) stockData[i][4] >= targetPrice) {

                    sendResult(targetDates[targetIndex], targetPrice, companies[targetIndex],
                    "Vyšší",(int) stockData[startIndex][1], 
                    true, (Date) stockData[i][0], days, 
                    targetPrice, (int) stockData[finalIndex][2]);
                    return;         
            }
            days++;
        }
        sendResult(targetDates[targetIndex], targetPrice, companies[targetIndex], 
        "Vyšší", (int) stockData[startIndex][1], 
        false, (Date) stockData[finalIndex][0], days, 
        (int) stockData[finalIndex][2], (int) stockData[finalIndex][2]);
        } 
    }


    /**
     * Vypočítá doplňující statistiky (max, min, průměrné ceny) pro tržní data ve sledovaném období.
     *
     * @param startIndex Počáteční index v poli stockData (odpovídá počátečnímu zkoumanému dni).
     * @param finalIndex Konečný index v poli stockData (odpovídá poslednímu zkoumanému dni).
     */
    public void calculateStats(int startIndex, int finalIndex) {
       
        if (!validateIndexes(startIndex, finalIndex)) return;

        int maxHigh = Integer.MIN_VALUE, minLow = Integer.MAX_VALUE;
        Date maxHighDate = null, minLowDate = null;
        double sumOpen = 0, sumClose = 0;
        int count = 0;

        for (int i = startIndex; i >= finalIndex; i--) {
            Date date = (Date) stockData[i][0];
            int open = (int) stockData[i][1];
            int close = (int) stockData[i][2];
            int low = (int) stockData[i][3];
            int high = (int) stockData[i][4];

            sumOpen += open;
            sumClose += close;

            if (high > maxHigh) {
                maxHigh = high;
                maxHighDate = date;
            }

            if (low < minLow) {
                minLow = low;
                minLowDate = date;
            }

            count++;
        }

        double avgOpen = count > 0 ? sumOpen / count : 0;
        double avgClose = count > 0 ? sumClose / count : 0;
        double volatility = calculateVolatility(startIndex, finalIndex);

        sendStats(avgOpen, avgClose, volatility, maxHigh, minLow, maxHighDate, minLowDate);
    }

    /**
     * Vypočítá volatilitu ve sledovaném období.
     * 
     * Využívá výběrovou směrodatnou odchylku procentuálních změn (sloupec Change)
     *
     * @param startIndex Počáteční index v poli stockData (odpovídá počátečnímu zkoumanému dni).
     * @param finalIndex Konečný index v poli stockData (odpovídá poslednímu zkoumanému dni).
     * @return Volatilita sledovaného období v %.
     */
    private double calculateVolatility(int startIndex, int finalIndex) {
        
        if (!validateIndexes(startIndex, finalIndex)) return 0;
    
        double sum = 0, sumSquaredDiff = 0;
        int count = 0;

        for (int i = startIndex; i >= finalIndex; i--) {
            double percentChange = (double) stockData[i][5]; 
            sum += percentChange;
            count++; 
        }

        if (count < 2) return 0; 
    
        double mean = sum / count; 

        for (int i = startIndex; i >= finalIndex; i--) {
            double percentChange = (double) stockData[i][5];
            sumSquaredDiff += Math.pow(percentChange - mean, 2);
        }
        return Math.sqrt(sumSquaredDiff / (count - 1));
    }  

     /**
     * Stanoví počet obchodních dní ve sledovaném období.
     *
     * @param startIndex Počáteční index v poli {@code stockdata[][]} (odpovídá počátečnímu zkoumanému dni).
     * @param finalIndex Konečný index v poli {@code stockdata[][]} (odpovídá poslednímu zkoumanému dni).
     */
     private void calculateDays(int startIndex, int finalIndex) {

        if (!validateIndexes(startIndex, finalIndex)) return;

        int count =0;
        for (int i = startIndex; i >= finalIndex; i--) {
            count++;
            }
            result.setDays(count);
        }

    /**
     * Najde odpovídající indexy v {@code dateIndexMap} na základě zadaného počátečního a konečného datumu.
     * Pokud data pro konkrétní den neexistují, je zavolána metoda {@code findNearestDateIndex} pro
     * nalezení nejblizšího možného dne
     *
     * @param index     Index cílové ceny (odpovídá indexu v {@code targetPrices[]}).
     * @param startDate Počáteční datum sledovaného období.
     * @param finalDate Konečný datum (o rok později) sledovaného období.
     */
    private void findIndexes(int Index, Date startDate, Date finalDate) {
           
    try {
        if (Index < 0 || Index >= targetDates.length) {
            System.err.println("Neplatný index cílové ceny.");
            return;
        }

        boolean finalDateFound = false;

        this.startIndex = dateIndexMap.get(startDate);
        this.finalIndex = dateIndexMap.get(finalDate);

        if (startIndex == null) {
            this.startIndex = findNearestDateIndex(startDate, 1);
        }

        if (finalIndex == null) {
            this.finalIndex = findNearestDateIndex(finalDate, 1);
            result.setfinalDate((Date)stockData[finalIndex][0]);
            finalDateFound = true;
        }

        if (!finalDateFound) {
            result.setfinalDate(finalDate);         
        }

    } catch (Exception e) {
        System.err.println("Chyba: " + e.getMessage());
    }
}
    /**
     * Hledá nejbližší dostupný index v {@code dateIndexMap} k zadanému datu.
     * Posouvá se po předaných krocích, dokud nenalezne nějaký index.
     *
     * @param date Datum, od kterého začíná hledání.
     * @param step Krok v dnech (kladný či záporný), pro posun dopředu/dozadu.
     * @return Nalezený index (Integer) nebo null, pokud nebyl nalezen.
     */
    private Integer findNearestDateIndex(Date date, int step) {
    tempDate = date;
    findIndex = dateIndexMap.get(tempDate);

    while (findIndex == null) {
        tempDate = Utils.calculateDate(tempDate, Utils.TimeUnit.DAY, step);
        findIndex = dateIndexMap.get(tempDate);
    }

    return findIndex;
}
   /**
     * Naplní objekt {@code AnalysisResult} údaji o konkrétní cílové ceně a přúslušných výsledcích provedené analýzy
     *
     * @param targetDate          Datum vydání cílové ceny
     * @param targetPrice         Cílová cena
     * @param company             Název vydavatelské společnosti
     * @param currrentTargetPrice Řetězec "Nižší" nebo "Vyšší" podle relace cílové a otevírací ceny
     * @param openPrice           Otevírací cena v den vydání cílové ceny
     * @param targetReached       Informace, zda byla cílová cena dosažena
     * @param reachedDate         Datum dosažení cílové ceny
     * @param daysToReach         Počet obchodních dní k dosažení cílové ceny
     * @param profit              Hodnota pro kalkulaci zisku / ztráty
     * @param closePrice          Zavírací cena na konci období
     */
    private void sendResult(Date targetDate, int targetPrice, String company, String currrentTargetPrice, int openPrice, boolean targetReached, Date reachedDate, int daysToReach, int profit, int closePrice) {
    result.setTargetDate(targetDate);
    result.setTargetPrice(targetPrice);
    result.setCompany(company);
    result.setPrediction(currrentTargetPrice);
    result.setOpenPrice(openPrice);
    result.setClosePrice(closePrice);
    result.setTargetReached(targetReached);
    result.setReachedDate(reachedDate);
    result.setDaysToReach(daysToReach);
    result.setProfit(profit);
}

    /**
     * Naplní objekt {@code AnalysisResult} doplňujicími údaji (průměrné ceny, volatilita, max, min).
     *
     * @param avgOpenPrice  Průměrná otevírací cena ve sledovaném období
     * @param avgClosePrice Průměrná zavírací cena ve sledovaném období
     * @param volatility    Vypočítaná volatilita v % ve sledovaném období
     * @param max           Nejvyšší dosažená cena ve sledovaném období
     * @param min           Nejnižší dosažená cena ve sledovaném období
     * @param maxDate       Datum dosažení maxima
     * @param minDate       Datum dosažení minima
     */
    private void sendStats (double avgOpenPrice, double avgClosePrice, double volatility, int max, int min, Date maxDate, Date minDate) {
    result.setAvgOpenPrice(avgOpenPrice);
    result.setAvgClosePrice(avgClosePrice);
    result.setVolatility(volatility);
    result.setMaxHigh(max, maxDate);
    result.setMinLow(min, minDate);      
}

    /**
     * Ověří, zda jsou k dispozici všechna data potřebná pro analýzu.
     *
     * @return True, pokud jsou všechna data načtena, jinak False
     */
    private boolean validateData() {
    if (targetDates == null || targetPrices == null || stockData == null || dateIndexMap == null || csv == null || result == null) {
        System.err.println("XXX Data nejsou načtena XXX");
        System.err.println("targetDates: " + (targetDates == null ? "null" : "loaded"));
        System.err.println("targetPrices: " + (targetPrices == null ? "null" : "loaded"));
        System.err.println("stockData: " + (stockData == null ? "null" : "loaded"));
        System.err.println("dateIndexMap: " + (dateIndexMap == null ? "null" : "loaded"));
        System.err.println("csv: " + (csv == null ? "null" : "Set"));
        System.err.println("result: " + (result == null ? "null" : "Set"));
        return false;
    }
    return true;
}   

    /**
     * Ověří, zda jsou indexy {@code startIndex} a {@code finalIndex} platné pro průchod polem {@code stockdata[][]}.
     *
     * @param startIndex Počáteční index
     * @param finalIndex Konečný index
     * @return True, pokud jsou indexy platné, jinak False
     */
    private boolean validateIndexes(int startIndex, int finalIndex){
    if (startIndex < finalIndex || startIndex >= stockData.length || finalIndex < 0) {
        System.err.println("Chyba: Neplatné indexy pro výpočet statistik.");
        return false;
    }
        return true;
}
    
    /**
     * Interní pomocná metoda pro výpis tržních dat jednoho dne podle zadaného indexu.
     *
     * @param index Index pole {@code stockData[][]}.
     */
    private void printStockData(int index) {
        System.out.println("Index: "+ index + " | Date: " + sdf.format(stockData[index][0]) + ", Open: " + stockData[index][1] +
                ", Close: " + stockData[index][2] +
                ", Low: " + stockData[index][3] +
                ", High: " + stockData[index][4] 
                );
    }

    /**
     * Testovací metoda pro výpis ukázky tržních dat (prvních 3 a posledních 3 záznamů).
     */
    public void testMarketData() {
        if (stockData != null) {
            int rows = stockData.length;
            System.out.println("\n=== Ukázka tržních dat ===");

            for (int i = 0; i < Math.min(3, rows); i++) {
                printStockData(i);
            }

            System.out.println("...");

            for (int i = Math.max(rows - 3, 0); i < rows; i++) {
                printStockData(i);
            }
        }
    }

    /**
     * Interní pomocná metoda pro výpis cílové ceny podle zadaného indexu.
     *
     * @param index Index odpovídajicí {@code targetPrices[]}.
     */
    private void printTargetPrice(int index) {
        System.out.println("Index: " + index + " | Datum: " + targetDates[index] + " Cena: " + targetPrices[index]);
    }
    
    /**
     * Testovací metoda, která vypíše ukázku cílových cen
     * (první a posledních 3 záznamy)
     */
    public void testTargetPrices() {
        if (targetDates != null && targetPrices != null) {
            int count = targetPrices.length;
            System.out.println("\n=== Ukázka cílových cen ===");
    
            for (int i = 0; i < Math.min(3, count); i++) {
                printTargetPrice(i);
            }
    
            System.out.println("...");
    
            for (int i = Math.max(count - 3, 0); i < count; i++) {
                printTargetPrice(i);
            }
        }
    }
}