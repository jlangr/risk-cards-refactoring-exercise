package cards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static cards.Card.CANNON;
import static cards.Card.CAVALRY;
import static cards.Card.INFANTRY;
import static cards.Card.WILDCARD;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;

public class ATrader {
    private Trader trader = new Trader();
    private Card[] bestResult;

    @BeforeEach
    public void initializeBestResult() {
        bestResult = new Card[3];
    }

    @Nested
    class PopulatesBestResultWith {
        @BeforeEach
        public void createWithArbitraryCardMode() {
            trader.setCardMode(Trader.CARD_INCREASING_SET);
        }

        @Test
        public void returnsThreeMatchingWhenOnlyThree() {
            trader.getBestTrade(cards(CANNON, CANNON, CANNON), bestResult);

            assertThat(cardTypes(bestResult), is(asList(CANNON, CANNON, CANNON)));
        }

        @Test
        public void returnsThreeMatchingFromLargerHand() {
            trader.getBestTrade(cards(INFANTRY, CANNON, INFANTRY, CANNON, INFANTRY), bestResult);

            assertThat(cardTypes(bestResult), is(asList(INFANTRY, INFANTRY, INFANTRY)));
        }

        @Test
        public void returnsThreeWithHigherScore() {
            trader.getBestTrade(cards(INFANTRY, CANNON, INFANTRY, CAVALRY, INFANTRY), bestResult);

            assertThat(cardTypes(bestResult), is(asList(CANNON, CAVALRY, INFANTRY)));
        }

        @Test
        public void order1() {
            trader.getBestTrade(uniqueCards(
                    INFANTRY, 0,
                    CANNON, 1,
                    INFANTRY, 2,
                    INFANTRY, 3,
                    CAVALRY, 4), bestResult);

            assertThat(cardIds(bestResult),
                    is(asList(1, 4, 0)));
        }

        @Test
        public void order2() {
            trader.getBestTrade(uniqueCards(
                    CAVALRY, 0,
                    CANNON, 1,
                    CANNON, 2,
                    CAVALRY, 3,
                    CAVALRY, 4), bestResult);

            assertThat(cardIds(bestResult),
                    is(asList(0, 3, 4)));
        }

        @Test
        public void order3WithOneWildcard() {
            trader.getBestTrade(uniqueCards(
                    CAVALRY, 0,
                    CANNON, 1,
                    CANNON, 2,
                    WILDCARD, 3), bestResult);

            assertThat(cardIds(bestResult),
                    is(asList(1, 0, 3)));
        }

        @Test
        public void order3WithTwoWildcards() {
            trader.getBestTrade(uniqueCards(
                    CAVALRY, 0,
                    WILDCARD, 1,
                    INFANTRY, 2,
                    WILDCARD, 3), bestResult);

            assertThat(cardIds(bestResult),
                    is(asList(1, 0, 2)));
        }

        @Test
        public void doesNotPopulateBestResultWhenInsufficientCards() {
            int armies = trader.getBestTrade(cards(INFANTRY), bestResult);

            assertThat(armies, is(equalTo(0)));
        }

        @Test
        public void doesNotPopulateBestResultWhenNoMatch() {
            int armies = trader.getBestTrade(cards(INFANTRY, CAVALRY, INFANTRY, CAVALRY), bestResult);

            assertThat(armies, is(equalTo(0)));
        }
    }

    @Nested
    class InFixedSetMode {
        @BeforeEach
        public void create() {
            trader.setCardMode(Trader.CARD_FIXED_SET);
        }

        @Test
        public void choosesHigherPoints() {
            int armies = trader.getBestTrade(uniqueCards(
                    CAVALRY, 0,
                    INFANTRY, 1,
                    INFANTRY, 2,
                    INFANTRY, 3,
                    CAVALRY, 4,
                    CAVALRY, 5), bestResult);

            assertThat(armies, is(equalTo(6)));
            assertThat(cardIds(bestResult),
                    is(asList(0, 4, 5)));
        }

        @Test
        public void armiesIs6ForCavalrySet() {
            int armies = trader.getBestTrade(cards(CAVALRY, CAVALRY, CAVALRY), bestResult);
            assertThat(armies, is(equalTo(6)));
        }

        @Test
        public void armiesIs4ForInfantrySet() {
            int armies = trader.getBestTrade(cards(INFANTRY, INFANTRY, INFANTRY), bestResult);
            assertThat(armies, is(equalTo(4)));
        }

        @Test
        public void armiesIs8ForCannonSet() {
            int armies = trader.getBestTrade(cards(CANNON, CANNON, CANNON), bestResult);
            assertThat(armies, is(equalTo(8)));
        }

        @Test
        public void allCardsDifferentWithWildcard() {
            int armies = trader.getBestTrade(cards(WILDCARD, INFANTRY, CANNON), bestResult);
            assertThat(armies, is(equalTo(10)));
        }

        @Test
        public void allCardsDifferent() {
            int armies = trader.getBestTrade(cards(CAVALRY, INFANTRY, CANNON), bestResult);
            assertThat(armies, is(equalTo(10)));
        }

        @Test
        public void twoWildcardsTreatedAsAllDifferent() {
            int armies = trader.getBestTrade(cards(CANNON, WILDCARD, WILDCARD), bestResult);
            assertThat(armies, is(equalTo(10)));
        }

        @Test
        public void threeWildcardsNotReallyPossible() {
            int armies = trader.getBestTrade(cards(WILDCARD, WILDCARD, WILDCARD), bestResult);
            assertThat(armies, is(equalTo(12)));
        }
    }

    @Nested
    class InItalianMode {
        @BeforeEach
        public void create() {
            trader.setCardMode(Trader.CARD_ITALIANLIKE_SET);
        }

        @Test
        public void allTheSameCavalry() {
            int armies = trader.getBestTrade(cards(CAVALRY, CAVALRY, CAVALRY), bestResult);
            assertThat(armies, is(equalTo(8)));
        }

        @Test
        public void allTheSameInfantry() {
            int armies = trader.getBestTrade(cards(INFANTRY, INFANTRY, INFANTRY), bestResult);
            assertThat(armies, is(equalTo(6)));
        }

        @Test
        public void allTheSameCannon() {
            int armies = trader.getBestTrade(cards(CANNON, CANNON, CANNON), bestResult);
            assertThat(armies, is(equalTo(4)));
        }

        @Test
        public void allTheSameWildcard() {
            int armies = trader.getBestTrade(cards(WILDCARD, WILDCARD, WILDCARD), bestResult);
            assertThat(armies, is(equalTo(0)));
        }

        @Test
        public void allTheSameWithOneWildcard() {
            int armies = trader.getBestTrade(cards(WILDCARD, CANNON, CANNON), bestResult);
            assertThat(armies, is(equalTo(12)));
        }

        @Test
        public void twoWildcardsApparentlySuck() {
            int armies = trader.getBestTrade(cards(WILDCARD, WILDCARD, CANNON), bestResult);
            assertThat(armies, is(equalTo(0)));
        }

        @Test
        public void choosesHigherPoints() {
            int armies = trader.getBestTrade(uniqueCards(
                    CAVALRY, 0,
                    INFANTRY, 1,
                    INFANTRY, 2,
                    INFANTRY, 3,
                    CAVALRY, 4,
                    CAVALRY, 5), bestResult);

            assertThat(armies, is(equalTo(8)));
            assertThat(cardIds(bestResult),
                    is(asList(0, 4, 5)));
        }
    }

    @Nested
    class InIncreasingMode {
        @BeforeEach
        public void create() {
            trader.setCardMode(Trader.CARD_INCREASING_SET);
        }

        @Test
        public void increasesBy4WhenStateUnder4() {
            trader.setCardState(0);

            int armies = trader.getBestTrade(cards(CANNON, CANNON, CANNON), bestResult);

            assertThat(armies, is(equalTo(0 + 4)));
        }

        @Test
        public void increasesBy2WhenStateUnder12() {
            trader.setCardState(4);

            int armies = trader.getBestTrade(cards(INFANTRY, INFANTRY, INFANTRY), bestResult);

            assertThat(armies, is(equalTo(4 + 2)));
        }

        @Test
        public void increasesBy3WhenStateUnder15() {
            trader.setCardState(14);

            int armies = trader.getBestTrade(cards(INFANTRY, INFANTRY, INFANTRY), bestResult);

            assertThat(armies, is(equalTo(14 + 3)));
        }

        @Test
        public void increasesBy5WhenState15OrMore() {
            trader.setCardState(15);

            int armies = trader.getBestTrade(cards(INFANTRY, INFANTRY, INFANTRY), bestResult);

            assertThat(armies, is(equalTo(15 + 5)));
        }

        @Test
        public void choosesFirstMatchingTypeSincePointsAreSame() {
            trader.getBestTrade(uniqueCards(
                    CAVALRY, 0,
                    INFANTRY, 1,
                    INFANTRY, 2,
                    INFANTRY, 3,
                    CAVALRY, 4,
                    CAVALRY, 5), bestResult);

            assertThat(cardIds(bestResult),
                    is(asList(1, 2, 3)));
        }
    }

    private List<String> cardTypes(Card[] cards) {
        return Arrays.stream(cards)
                .map(c -> c.getType())
                .collect(Collectors.toList());
    }

    private List<Integer> cardIds(Card[] cards) {
        return Arrays.stream(cards)
                .map(c -> c.getCountryIndex())
                .collect(Collectors.toList());
    }

    private List<Card> cards(String... cards) {
        return Arrays.stream(cards)
                .map(c -> new Card(c))
                .collect(Collectors.toList());
    }

    private List<Card> uniqueCards(Object... cardsAndIds) {
        List<Card> results = new ArrayList<>();
        for (int i = 0; i < cardsAndIds.length; i += 2) {
            String type = (String)cardsAndIds[i];
            int id = (Integer)cardsAndIds[i + 1];
            results.add(new Card(type, id));
        }
        return results;
    }
}
