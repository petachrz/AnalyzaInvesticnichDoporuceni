package cz.petrchrz;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
* Třída pro zpracování výsledků analýzy cílových cen akcií.
* 
* @author Petr Chrz
*/
public class AnalysisResult {

      /** Den vydání cílové ceny */
      private Date targetDate;

      /** Odhadovaná cílová cena */
      private int targetPrice;
  
      /** Informace, zda je cílová cena vyšší nebo nižší než otevrírací cena akcie v den vydání (Vyšší / Nžší) */
      private String prediction;
  
      /** Otevírací cena akcie v den vydání cílové ceny */
      private int openPrice;
  
      /** Zavírací cena akcie na konci platnosti cílové ceny */
      private int closePrice;
  
      /** Informace, zda byla cílová cena dosažena (TRUE / FALSE) */
      private boolean targetReached;
  
      /** Den dosažení cílové ceny, případně poslední platný den zkoumaného období */
      private Date reachedDate;
  
      /** Počet obchodních dní mezi vydáním a dosažením cílové ceny, jinak celkový počet dní v rámci sledovaného období */
      private int daysToReach;
  
      /** Započítaná hodnota pro kalkuaci zisku / ztráty */
      private int profit;
  
      /** Celkový počet obchodních dní v rámci sledovaného období */
      private int days;
  
      /** Datum konce platnosti cílové ceny */
      private Date finalDate;
  
      /** Vydavatel cílové ceny */
      private String company;
  
      /** Průměrná otevírací cena akcie za sledované období*/
      private double avgOpenPrice;
  
      /** Průměrná zavírací cena akcie za sledované období */
      private double avgClosePrice;
  
      /** Volatilita akcie v % za sledované období */
      private double volatility;
  
      /** Nejvyšší dosažená cena ve sledovaném období */
      private int maxHigh;
  
      /** Den dosažení maxima */
      private Date maxHighDate;
  
      /** Nejnižší dosažená cena ve sledovaném období*/
      private int minLow;
  
      /** Den dosažení minima */
      private Date minLowDate;
  
    /** Instance {@link SimpleDateFormat} pro formátovaní časových údajů */
    SimpleDateFormat sdf = Utils.getSimpleDateFormat();

    // Gettery a Settery
    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
    }

    public void setTargetPrice(int targetPrice) {
        this.targetPrice = targetPrice;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public void setOpenPrice(int openPrice) {
        this.openPrice = openPrice;
    }

    public void setClosePrice(int closePrice){
        this.closePrice=closePrice;
    }

    public void setTargetReached(boolean targetReached) {
        this.targetReached = targetReached;
    }

    public void setReachedDate(Date reachedDate) {
        this.reachedDate = reachedDate;
    }

    public void setDaysToReach(int daysToReach) {
        this.daysToReach = daysToReach;
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }

    public void setAvgOpenPrice(double avgOpenPrice) {
        this.avgOpenPrice = avgOpenPrice;
    }

    public void setAvgClosePrice(double avgClosePrice) {
        this.avgClosePrice = avgClosePrice;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public void setMaxHigh(int maxHigh, Date maxHighDate) {
        this.maxHigh = maxHigh;
        this.maxHighDate = maxHighDate;
    }

    public void setMinLow(int minLow, Date minLowDate) {
        this.minLow = minLow;
        this.minLowDate = minLowDate;
    }

    public void setDays(int days){
        this.days = days;
    }

    public void setfinalDate(Date finalDate){
       this.finalDate=finalDate;
    }

    public void setCompany(String company){
        this.company = company;
    }

    /**
    * Generuje CSV řádek s výsledky analýzy pro konkrétní cílovou cenu.
    *
    * @return Řetězec reprezentující CSV řádek s výsledky analýzy
    */
    public String toCsv(){
        return (getTargetDate() + ";" +
                getFinalDate() + ";" +
                getCompany() + ";" +
                getTargetPrice() + ";" +
                getOpenPrice() + ";" +
                getClosePrice() + ";" +
                getPrediction() + ";" +
                checkDirrections() + ";" +
                getTargetReached() + ";" +
                getReachedDate() + ";" +
                getDaysToReach() + ";" +
                calcDaysbetween(targetDate, reachedDate) + ";" +
                getDays() + ";" +
                calcDaysbetween(targetDate, finalDate) + ";" +
                getProfit() + ";" +
                calcAbsoluteExpectedReturn() + ";" +
                calcAbsoluteRealReturn() + ";" +
                calcAbsoluteDifference() + ";" +
                String.format("%.2f", calcRelativeExpectedReturn()) + ";" +
                String.format("%.2f", calcRelativeRealReturn()) + ";" +
                String.format("%.2f", calcPercentagePointDifference()) + ";" +
                String.format("%.2f", calcRelativeDifference()) + ";" + 
                String.format("%.2f", calcAnnualExpectedReturn()) + ";" +
                String.format("%.2f", calcAnnualRealReturn())
                );
    }

    /**
    * Generuje CSV řádek s doplňujicími údaji.
    * 
    * @return Řetězec reprezentující CSV řádek s doplňujicími údaji
    */
    public String csvAddStats() {
        return (
            getMaxHigh() + ";" +
            getMinLow() + ";" +
            getMaxHighDate() + ";" +
            getMinLowDate() + ";" +
            calcAbsoluteAccuracy() + ";" +
            String.format("%.2f", calcRelativeAccuracy()) + ";" +
            String.format("%.2f", getAvgOpenPrice()) + ";" +
            String.format("%.2f",getAvgClosePrice()) + ";" + 
            String.format("%.2f",getVolatility())
        );
    }    
 
     /**
     * Vypočítá anualizovaný výnos na základě reálného výnosu a počtu dní.
     * @param realYield reálný výnos
     * @param days počet dní
     * @return anualizovaný výnos
     */
    private double annualReturn (double realYield, int days) {
        if (days == 0)  return realYield;
        return (Math.pow(1 + (realYield/100), 365.0 / days) - 1) * 100;
    }

        /**
        * Vrátí datum vydání cílové ceny ve formátu dd-MMM-yyyy.
        * @return Datum vydání cílové ceny.
        */
    public String getTargetDate() {
        if (targetDate == null) {
             return null;
         }
         return sdf.format(targetDate);
     }

        /**
        * Vrátí datum konce platnosti cílové ceny ve formátu dd-MMM-yyyy.
        * @return Datum konce platnosti cílové ceny.
        */
     public String getFinalDate() {
         if (finalDate == null) {
             return null;
         }
         return sdf.format(finalDate);
     }
  
        /**
        * Vrátí název vydavatele cílové ceny.
        * @return Název vydavatele cílové ceny.
        */
     public String getCompany() {
         return company;
     }
        /**
        * Vrátí cílovou cenu
        * @return Cílová cena.
        */
     public int getTargetPrice() {
         return targetPrice;
     }
        /**
        * Vrátí otevírací cenu akcie v den vydání cílové ceny.
        * @return Otevírací cena akcie.
        */
     public int getOpenPrice() {
         return openPrice;
     }
        /**
        * Vrátí zavírací cenu akcie na konci platnosti cílové ceny.
        * @return Zavírací cena akcie.
        */
     public int getClosePrice() {
         return closePrice;
     }
        /**
        * Vrátí inormaci, zda je cílová cena vyšší nebo nižší než otevírací cena akcie v den vydání.
        * @return Vyšší / Nižší.
        */
     public String getPrediction() {
         return prediction;
     }
     /**
      * Vrátí započítanou hodnotu pro kalkulaci zisku / ztráty.
      * @return Započítaná hodnota pro kalkulaci zisku / ztráty.
      */
     public int getProfit() {
         return profit;
     }
     /**
      * Vrátí počet obchodních dní mezi vydáním a dosažením cílové ceny, jinak celkový počet dní v rámci sledovaného období.
      * @return Počet obchodních dní.
      */
     public int getDaysToReach() {
         return daysToReach;
     }
     /**
      * Vrátí celkových počet obchodních dní v rámci platnosti cílové ceny.
      * @return Celkový počet obchodních dní.
      */
     public int getDays() {
         return days;
     }
     /**
      * Vrátí nejvyšší dosaženou cenu ve sledovaném období.
      * @return Nejvyšší dosažená cena.
      */
     public int getMaxHigh() {
         return maxHigh;
     }
        /**
        * Vrátí den dosažení maxima.
        * @return Den dosažení maxima.
        */
     public String getMaxHighDate() {
         return sdf.format(maxHighDate);
     }
        /**
        * Vrátí nejnižší dosaženou cenu ve sledovaném období.
        * @return Nejnižší dosažená cena.
        */
     public int getMinLow() {
         return minLow;
     }
        /**
        * Vrátí den dosažení minima.
        * @return Den dosažení minima.
        */
     public String getMinLowDate() {
         return sdf.format(minLowDate);
     }
      
        /**
         * Vrátí průměrnou otervírací cenu akcie za sledované období.
         * @return Průměrná otevírací cena akcie.
         */
     public double getAvgOpenPrice() {
         return avgOpenPrice;
     }
        /**
        * Vrátí průměrnou zavírací cenu akcie za sledované období.
        * @return Průměrná zavírací cena akcie.
        */
     public double getAvgClosePrice() {
         return avgClosePrice;
     }
        /**
        * Vrátí volatilitu akcie v % za sledované období.
        * @return Volatilita akcie.
        */
     public double getVolatility() {
         return volatility;
     }
     
        /**
        * Kontroluje, zda směr pohybu akcie odpovídá očekávanému směru na základě cílové ceny.
        * Pokud je cílová cena vyšší než otevírací cena, kontroluje, zda je zavírací cena vyšší než otevírací cena.
        * Pokud je cílová cena nižší než otevírací cena, kontroluje, zda je zavírací cena nižší než otevírací cena.
        * @return ANO, pokud se směr zavírací ceny shoduje s očekávaným směrem, jinak "NE;".
        */
     public String checkDirrections() {
         if (targetPrice > openPrice) {
             return closePrice > openPrice ? "ANO" : "NE";
         } else if (targetPrice < openPrice) {
             return closePrice < openPrice ? "ANO" : "NE";
         } else {
             return "N/A";
         }
     }
        /**
        * Kontroluje, zda byla cílová cena dosažena.
        * @return ANO, pokud byla cílová cena dosažena, jinak "NE;".
        */
     public String getTargetReached() {
         if (targetReached) {
             return "ANO";
         } else {
             return "NE";
         }
     }
        /**
        * Vrátí den dosažení cílové ceny ve formátu dd-MMM-yyy.
        * Pokud cílová cena nebyla dosažena, vrátí poslední platný den zkoumaného období.
        * @return Den dosažení cílové ceny.
        */
     public String getReachedDate() {
         if (reachedDate == null) {
             return null;
         }
         return sdf.format(reachedDate);
     }
        /**
        * Vypočítá počet kalendářních dní mezi dvěma daty.
        * @param startDate Počáteční datum.
        * @param endDate Konečný datum.
        * @return Rozdíl v počtu dní mezi dvěma daty.
        */
     public int calcDaysbetween(Date startDate, Date endDate) {
         if (startDate == null || endDate == null) {
             return 0;
         }
         return (int) ChronoUnit.DAYS.between(
                 startDate.toInstant().atZone(ZoneId.of("Europe/Prague")).toLocalDate(),
                 endDate.toInstant().atZone(ZoneId.of("Europe/Prague")).toLocalDate());
     }
        /**
        * Vypočítá absolutní očekávaný výnos.
        * @return Absolutní očekávaný výnos.
        */
     public int calcAbsoluteExpectedReturn() {
         if (targetPrice >= openPrice) {
             return targetPrice - openPrice;
         } else {
             return openPrice - targetPrice;
         }
     }
        /**
        * Vypočítá absolutní reálný výnos.
        * @return Absolutní reálný výnos.
        */
     public int calcAbsoluteRealReturn() {
            if (targetPrice >= openPrice) {
                return profit - openPrice;
            } else {
                return openPrice - profit;
            }
        }
        /**
         * Vypočítá relativní očekávaný výnos v procentech.
         * @return Relativní očekávaný výnos (%).
         */
     public double calcRelativeExpectedReturn() {
            if (targetPrice >= openPrice) {
                return ((targetPrice - openPrice) / (double) openPrice ) * 100;
            } else {
                return ((openPrice - targetPrice) / (double) openPrice) * 100;
            }
     }
        /**
         * Vypočítá relativní reálný výnos v procentech.
         * @return Relativní reálný výnos (%).
         */
     public double calcRelativeRealReturn() {
            if (targetPrice >= openPrice) {
                return ((profit - openPrice) / (double) openPrice) * 100;
            } else {
                return ((openPrice - profit) / (double) openPrice) * 100;
            }
     }
        /**
         * Vypočítá absolutní rozdíl mezi očekávaným a skutečným výnosem.
         * @return Absolutní rozdíl mezi očekávaným a skutečným výnosem.
         */
     public int calcAbsoluteDifference() {
         return calcAbsoluteExpectedReturn() - calcAbsoluteRealReturn();
        } 
    
        /**
         * Vypočítá rozdíl mezi očekávaným a skutečným výnosem v procentních bodech.
         * @return rozdíl mezi a očekávaným a skutečným výnosem v p.b..
         */
    public double calcPercentagePointDifference() {
        return (calcRelativeExpectedReturn() - calcRelativeRealReturn());
    }

        /**
         * Vypočítá relativní rozdíl mezi očekávaným a skutečným výnosem v procentech.
         * @return Relativní rozdíl mezi očekávaným a skutečným výnosem v procentech
         */
     public double calcRelativeDifference() {
         return ((( calcRelativeExpectedReturn()-calcRelativeRealReturn()) / calcRelativeExpectedReturn())) * 100;
     }

       /**
        * Vypočítá absolutní přesnost cílové ceny.
        * @return Absolutní přesnost cílové ceny.
        */
     public int calcAbsoluteAccuracy() {
            if (targetPrice >= openPrice) {
                return maxHigh - targetPrice;
            } else {
                return targetPrice - minLow;
         }
    }
    
        /**
         * Vypočítá relativní přesnost cílové ceny v procentech.
         * @return Relativní přesnost cílové ceny (%).
         */
     public double calcRelativeAccuracy() {
            if (targetPrice >= openPrice) {
                return ((maxHigh - targetPrice) / (double) targetPrice) * 100;
            } else {
                return ((targetPrice - minLow) / (double) targetPrice) * 100;
            }
        }

        /**
         * Vypočítá anualizovaný očekávaný výnos na základě relativního očekávaného výnosu a počtu dní.
         * @return Anualizovaný očekávaný výnos (%).
         */
     public double calcAnnualExpectedReturn() {
         return annualReturn(calcRelativeExpectedReturn(), (int) ChronoUnit.DAYS.between(
                 targetDate.toInstant().atZone(ZoneId.of("Europe/Prague")).toLocalDate(),
                 finalDate.toInstant().atZone(ZoneId.of("Europe/Prague")).toLocalDate()));
     }
       
        /**
         * Vypočítá anualizovaný reálný výnos na základě relativního reálného výnosu a počtu dní.
         * @return Anualizovaný reálný výnos (%).
         */
     public double calcAnnualRealReturn() {
         return annualReturn(calcRelativeRealReturn(), (int) ChronoUnit.DAYS.between(
                 targetDate.toInstant().atZone(ZoneId.of("Europe/Prague")).toLocalDate(),
                 reachedDate.toInstant().atZone(ZoneId.of("Europe/Prague")).toLocalDate()));
     }
}