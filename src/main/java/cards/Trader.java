package cards;

import java.util.*;
import java.util.stream.Stream;
import static java.util.Arrays.asList;
import static cards.Card.*;

import static java.util.stream.Collectors.*;

class Trader {
    public final static int CARD_INCREASING_SET = 0;
    public final static int CARD_FIXED_SET = 1;
    public final static int CARD_ITALIANLIKE_SET = 2;

    private int cardState;
    private int cardMode;

    abstract class Scorer {
        protected List<Card> cards;
        public Scorer(List<Card> cards) { this.cards = cards; }
        int value() { return getTradeAbsValue(types(cardsToScore()), getCardMode()); }
        abstract List<Card> cardsToScore();
        abstract boolean shouldScore();
    }

    class UniqueScorer extends Scorer {
        UniqueScorer(List<Card> cards) { super(cards); }
        @Override List<Card> cardsToScore() { return firstOfEachType(cards); }
        @Override boolean shouldScore() { return countUnique(cards) >= 3; }
    }

    class WildcardScorer extends Scorer {
        public WildcardScorer(List<Card> cards) { super(cards); }
        @Override List<Card> cardsToScore() { return getCards(cards, WILDCARD); }
        @Override boolean shouldScore() { return count(types(cards), WILDCARD) >= 3; }
    }

    class TypeScorer extends Scorer {
        private String cardType;

        public TypeScorer(String cardType, List<Card> cards) {
            super(cards);
            this.cardType = cardType;
        }

        @Override List<Card> cardsToScore() {
            return fill3(getCards(cards, cardType), getCards(cards, WILDCARD));
        }

        private List<Card> fill3(List<Card> cardList, List<Card> wildCards) {
            return Stream.concat(cardList.stream(), wildCards.stream().limit(3 - cardList.size())).collect(toList());
        }

        @Override boolean shouldScore() {
            return getCards(cards, cardType).size() + getCards(cards, WILDCARD).size() >= 3;
        }
    }

    public int getBestTrade(List<Card> cards, Card[] bestResult) {
        int bestValue = 0;
        for (Scorer scorer: asList(new UniqueScorer(cards), new WildcardScorer(cards),
                new TypeScorer(CANNON, cards), new TypeScorer(INFANTRY, cards), new TypeScorer(CAVALRY, cards)))
            if (scorer.shouldScore() && scorer.value() > bestValue) {
                bestValue = scorer.value();
                if (bestResult == null)
                    return bestValue;
                populateBestResult(bestResult, scorer.cardsToScore());
            }
        return bestValue;
    }

    private List<String> types(List<Card> cards) {
        return cards.stream().map(Card::getType).collect(toList());
    }

    private void populateBestResult(Card[] bestResult, List<Card> cards) {
        bestResult[0] = cards.get(0);
        bestResult[1] = cards.get(1);
        bestResult[2] = cards.get(2);
    }

    private Card getCardOrWildcard(List<Card> cards, String type) {
        Card card = getCard(cards, type);
        if (card != null) return card;
        return getCard(cards, WILDCARD);
    }

    private Card getCard(List<Card> cards, String type) {
        List<Card> matching = getCards(cards, type);
        return matching.isEmpty() ? null : matching.get(0);
    }

    private List<Card> getCards(List<Card> cards, String type) {
        return cards.stream().filter(c -> c.getType().equals(type)).collect(toList());
    }

    private int countUnique(List<Card> cards) {
        return cards.stream().map(c -> c.getType()).collect(toSet()).size();
    }

    private List<Card> firstOfEachType(List<Card> cards) {
        return asList(getCardOrWildcard(cards, CANNON), getCardOrWildcard(cards, CAVALRY), getCardOrWildcard(cards, INFANTRY));
    }

    public int getTradeAbsValue(List<String> types, int cardMode) {
        switch (cardMode) {
            case CARD_INCREASING_SET: return increasingArmies(types);
            case CARD_FIXED_SET: return fixedArmies(types);
            case CARD_ITALIANLIKE_SET:
            default:
                return italianLikeArmies(types);
        }
    }

    private int italianLikeArmies(List<String> types) {
        if (areAllSame(types))
            switch (sameSetType(types)) {
                case CAVALRY: return 8;
                case INFANTRY: return 6;
                case CANNON: return 4;
                default: return 0; // 3 wildcards
            }
        if (!hasWildcard(types) && areAllDifferent(types))
            return 10;
        if (areAllSameWithOneWildcard(types))
            return 12;
        return 0; //2 wildcards or wildcard + two different
    }

    private int fixedArmies(List<String> types) {
        if (areAllSame(types) || areAllSameWithOneWildcard(types))
            switch (sameSetType(types)) {
                case INFANTRY: return 4;
                case CAVALRY: return 6;
                case CANNON: return 8;
                default: return 12; // 3 wildcards
            }
        if (count(types, WILDCARD) == 2 || areAllDifferent(types))
            return 10;
        return 0;
    }

    private boolean areAllSameWithOneWildcard(List<String> types) {
        return count(types, WILDCARD) == 1 && areAllSame(remove(types, WILDCARD));
    }

    private String sameSetType(List<String> types) {
        List<String> results = remove(types, WILDCARD);
        return results.isEmpty() ? WILDCARD : results.get(0);
    }

    private boolean areAllDifferent(List<String> types) {
        return new HashSet<>(types).size() == 3;
    }

    private boolean areAllSame(List<String> types) {
        return new HashSet<>(types).size() == 1;
    }

    private boolean hasWildcard(List<String> types) {
        return count(types, WILDCARD) > 0;
    }

    private List<String> remove(List<String> types, String type) {
        return types.stream().filter(t -> !t.equals(type)).collect(toList());
    }

    private long count(List<String> types, String type) {
        return types.stream().filter(c -> c.equals(type)).count();
    }

    private int increasingArmies(List<String> types) {
        if (hasWildcard(types) || areAllSame(types) || areAllDifferent(types))
            return getNewCardState();
        return 0;
    }

    public int getNewCardState() {
        if (cardState < 4)
            return cardState + 4;
        if (cardState < 12)
            return cardState + 2;
        if (cardState < 15)
            return cardState + 3;
        return cardState + 5;
    }

    public int getCardMode() {
        return cardMode;
    }

    void setCardMode(int cardMode) {
        this.cardMode = cardMode;
    }

    void setCardState(int cardState) {
        this.cardState = cardState;
    }
}
