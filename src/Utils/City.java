package Utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a city
 */
public enum City implements Serializable {
    BERLIN,
    VIENNA,
    BRUSSELS,
    BANJA_LUKA,
    SOFIA,
    ZAGREB,
    PAPHOS,
    PRAGUE,
    COPENHAGEN,
    TALLINN,
    HELSINKI,
    PARIS,
    ATHENS,
    BUDAPEST,
    DUBLIN,
    TEL_AVIV,
    ROME,
    RIGA,
    PALANGA,
    LUXEMBOURG,
    MALTA,
    PODGORICA,
    RABAT,
    AMSTERDAM,
    OSLO,
    KRAKOW,
    LISBON,
    BUCHAREST,
    NIS,
    BRATISLAVA,
    MADRID,
    STOCKHOLM,
    ZURICH,
    BODRUM,
    KYIV,
    LONDON,
    TOKYO,
    SEOUL,
    NEW_YORK,
    LUANDA,
    MEXICO_CITY,
    TORONTO,
    SYDNEY,
    BRASILIA,
    BANGKOK,
    DUBAI;

    /**
     * Converts a City to a string
     * @return String
     */
    public String toString(){
        switch (this){
            case BERLIN -> {
                return "Berlin";
            }
            case VIENNA -> {
                return "Vienna";
            }
            case BRUSSELS -> {
                return "Brussels";
            }
            case BANJA_LUKA -> {
                return "Banja Luka";
            }
            case SOFIA -> {
                return "Sofia";
            }
            case ZAGREB -> {
                return "Zagreb";
            }
            case PAPHOS -> {
                return "Paphos";
            }
            case PRAGUE -> {
                return "Prague";
            }
            case COPENHAGEN -> {
                return "Copenhagen";
            }
            case TALLINN -> {
                return "Tallinn";
            }
            case HELSINKI -> {
                return "Helsinki";
            }
            case PARIS -> {
                return "Paris";
            }
            case ATHENS -> {
                return "Athens";
            }
            case BUDAPEST -> {
                return "Budapest";
            }
            case DUBLIN -> {
                return "Dublin";
            }
            case TEL_AVIV -> {
                return "Tel Aviv";
            }
            case ROME -> {
                return "Rome";
            }
            case RIGA -> {
                return "Riga";
            }
            case PALANGA -> {
                return "Palanga";
            }
            case LUXEMBOURG -> {
                return "Luxembourg";
            }
            case MALTA -> {
                return "Malta";
            }
            case PODGORICA -> {
                return "Podgorica";
            }
            case RABAT -> {
                return "Rabat";
            }
            case AMSTERDAM -> {
                return "Amsterdam";
            }
            case OSLO -> {
                return "Oslo";
            }
            case KRAKOW -> {
                return "Krakow";
            }
            case LISBON -> {
                return "Lisbon";
            }
            case BUCHAREST -> {
                return "Bucharest";
            }
            case NIS -> {
                return "Nis";
            }
            case BRATISLAVA -> {
                return "Bratislava";
            }
            case MADRID -> {
                return "Madrid";
            }
            case STOCKHOLM -> {
                return "Stockholm";
            }
            case ZURICH -> {
                return "Zurich";
            }
            case BODRUM -> {
                return "Bodrum";
            }
            case KYIV -> {
                return "Kyiv";
            }
            case LONDON -> {
                return "London";
            }
            case TOKYO -> {
                return "Tokyo";
            }
            case SEOUL -> {
                return "Seoul";
            }
            case NEW_YORK -> {
                return "New York";
            }
            case LUANDA -> {
                return "Luanda";
            }
            case MEXICO_CITY -> {
                return "Mexico City";
            }
            case TORONTO -> {
                return "Toronto";
            }
            case SYDNEY -> {
                return "Sydney";
            }
            case BRASILIA -> {
                return "Brasilia";
            }
            case BANGKOK -> {
                return "Bangkok";
            }
            case DUBAI -> {
                return "Dubai";
            }
            default -> {
                return "Heaven";
            }
        }
    }

    public Set<City> getCitiesWithFirstChar(char c){
        Set<City> ans = new HashSet<>();
        switch(c){
            case 'A' -> {
                ans.add(AMSTERDAM);
                ans.add(ATHENS);
            }
            case 'B' -> {
                ans.add(BANGKOK);
                ans.add(BANJA_LUKA);
                ans.add(BERLIN);
                ans.add(BODRUM);
                ans.add(BRASILIA);
                ans.add(BRATISLAVA);
                ans.add(BRUSSELS);
                ans.add(BUCHAREST);
                ans.add(BUDAPEST);
            }
            case 'C' -> {
                ans.add(COPENHAGEN);
            }
            case 'D' -> {
                ans.add(DUBAI);
                ans.add(DUBLIN);
            }
            case 'H' -> {
                ans.add(HELSINKI);
            }
            case 'K' -> {
                ans.add(KRAKOW);
                ans.add(KYIV);
            }
            case 'L' -> {
                ans.add(LISBON);
                ans.add(LONDON);
                ans.add(LUANDA);
                ans.add(LUXEMBOURG);
            }
            case 'M' -> {
                ans.add(MADRID);
                ans.add(MALTA);
                ans.add(MEXICO_CITY);
            }
            case 'N' -> {
                ans.add(NEW_YORK);
                ans.add(NIS);
            }
            case 'O' -> {
                ans.add(OSLO);
            }
            case 'P' -> {
                ans.add(PALANGA);
                ans.add(PAPHOS);
                ans.add(PARIS);
                ans.add(PRAGUE);
            }
            case 'R' -> {
                ans.add(RABAT);
                ans.add(RIGA);
                ans.add(ROME);
            }
            case 'S' -> {
                ans.add(SEOUL);
                ans.add(SOFIA);
                ans.add(STOCKHOLM);
                ans.add(SYDNEY);
            }
            case 'T' -> {
                ans.add(TALLINN);
                ans.add(TEL_AVIV);
                ans.add(TOKYO);
                ans.add(TORONTO);
            }
            case 'V' -> {
                ans.add(VIENNA);
            }
            case 'Z' -> {
                ans.add(ZAGREB);
                ans.add(ZURICH);
            }
            default -> {

            }
        }
        return ans;
    }

}
