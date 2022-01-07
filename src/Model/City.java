package Model;

import java.io.Serializable;

/**
 * Represents a city
 */
public enum City implements Serializable {
    /**
     * City of Berlin
     */
    BERLIN,
    /**
     * City of Vienna
     */
    VIENNA,
    /**
     * City of Brussels
     */
    BRUSSELS,
    /**
     * City of Sofia
     */
    SOFIA,
    /**
     * City of Zagreb
     */
    ZAGREB,
    /**
     * City of Paphos
     */
    PAPHOS,
    /**
     * City of Prague
     */
    PRAGUE,
    /**
     * City of Copenhagen
     */
    COPENHAGEN,
    /**
     * City of Tallinn
     */
    TALLINN,
    /**
     * City of Helsinki
     */
    HELSINKI,
    /**
     * City of Paris
     */
    PARIS,
    /**
     * City of Athens
     */
    ATHENS,
    /**
     * City of Budapest
     */
    BUDAPEST,
    /**
     * City of Dublin
     */
    DUBLIN,
    /**
     * City of Rome
     */
    ROME,
    /**
     * City of Riga
     */
    RIGA,
    /**
     * City of Palanga
     */
    PALANGA,
    /**
     * City of Luxembourg
     */
    LUXEMBOURG,
    /**
     * City of Malta
     */
    MALTA,
    /**
     * City of Podgorica
     */
    PODGORICA,
    /**
     * City of Rabat
     */
    RABAT,
    /**
     * City of Amsterdam
     */
    AMSTERDAM,
    /**
     * City of Oslo
     */
    OSLO,
    /**
     * City of Kraków
     */
    KRAKOW,
    /**
     * City of Lisbon
     */
    LISBON,
    /**
     * City of Bucharest
     */
    BUCHAREST,
    /**
     * City of Nis
     */
    NIS,
    /**
     * City of Bratislava
     */
    BRATISLAVA,
    /**
     * City of Madrid
     */
    MADRID,
    /**
     * City of Stockholm
     */
    STOCKHOLM,
    /**
     * City of Zurich
     */
    ZURICH,
    /**
     * City of Bodrum
     */
    BODRUM,
    /**
     * City of Kyiv
     */
    KYIV,
    /**
     * City of London
     */
    LONDON,
    /**
     * City of Tokyo
     */
    TOKYO,
    /**
     * City of Seoul
     */
    SEOUL,
    /**
     * City of New York
     */
    NY,
    /**
     * City of Luanda
     */
    LUANDA,
    /**
     * City of Toronto
     */
    TORONTO,
    /**
     * City of Sydney
     */
    SYDNEY,
    /**
     * City of Brasília
     */
    BRASILIA,
    /**
     * City of Bangkok
     */
    BANGKOK,
    /**
     * City of Dubai
     */
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
            case NY -> {
                return "New York";
            }
            case LUANDA -> {
                return "Luanda";
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

}
