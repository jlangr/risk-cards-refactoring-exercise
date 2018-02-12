package cards;

import java.io.Serializable;

public class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String CAVALRY = "Cavalry";
    public final static String INFANTRY = "Infantry";
    public final static String CANNON = "Cannon";
    public final static String WILDCARD = "wildcard";

    private String type;
    private int countryIndex;

    public Card(String type) {
        if (!type.equals(CAVALRY) && !type.equals(INFANTRY) && !type.equals(CANNON) && !type.equals(WILDCARD)) {
            throw new IllegalArgumentException("trying to make a card with an unknown type: " + type);
        }
        this.type = type;
    }

    public Card(String type, int countryIndex) {
        this.countryIndex = countryIndex;
        this.type = type;
    }

    public int getCountryIndex() {
        return countryIndex;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (countryIndex != card.countryIndex) return false;
        return type.equals(card.type);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + countryIndex;
        return result;
    }
}

