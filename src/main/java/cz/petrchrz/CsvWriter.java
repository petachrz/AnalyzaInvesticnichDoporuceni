package cz.petrchrz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Třída pro zápis dat do CSV souboru
 * 
 * @author Petr Chrz
 */
public class CsvWriter {
    private final BufferedWriter writer;
    private final StringBuilder line = new StringBuilder();

    /**
     * Vytvoří instanci {@code CsvWriter} a otevře CSV soubor pro zápis.
     *
     * Pokud soubor neexistuje nebo je prázdný, automaticky se zapíše hlavička definujicí sloupce (pro účely MS Excel).
     *
     * @param filename Název souboru, do kterého se bude zapisovat.
     */
    public CsvWriter(String filename) throws IOException {
        File file = new File(filename);
        boolean isNewFile = !file.exists() || file.length() == 0;

        this.writer = new BufferedWriter(new FileWriter(filename, true)); 

        if (isNewFile) {
        writer.write("Vydáno;Platnost do;Vydavatel;Cílová cena;Otevírací cena;Zavírací cena;Cílová cena / Open;Zachycení směru;Dosaženo;"+
        "Započítaný den;Počet uplynulých obchodních dní;Počet uplynulých kalendářních dní;Maximální počet obchodních dní;Maximální počet kalendářních dní;Započítaná hodnota;" +
        "Očekávaný výnos;Skutečný výnos;Rozdíl;Očekávaný výnos v %;Skutečný výnos v %;Rozdíl v p.b.;Rozdíl v %;Očekávaný výnos p.a.;Skutečný výnos p.a.;" +
        "Maximum;Minimum;Den dosažení maxima;Den dosažení minima;Odchylka cílové ceny od extrému;Odchylka v %;Průměr Open; Průměr Close; Volatilita");
        writer.newLine();
        }
    }

    /**
     * Zápis do CSV souboru.
     * @param message reprezentuje řádek CSV souboru
     */
    public void writeData(String message) {
        try {
            line.setLength(0); 
            line.append(message);
            writer.write(line.toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Chyba při zápisu do CSV: " + e.getMessage());
        }
    }

    /**
     * Uložení a zavření CSV souboru.
     */
    public void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("Chyba při zavírání souboru: " + e.getMessage());
        }
    }
}