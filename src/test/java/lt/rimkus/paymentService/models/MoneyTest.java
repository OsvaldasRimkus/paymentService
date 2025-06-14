package lt.rimkus.paymentService.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    private Money money;

    @BeforeEach
    void setUp() {
        money = new Money();
    }

    @Nested
    @DisplayName("Amount Tests")
    class AmountTests {

        @Test
        @DisplayName("Should set and get amount successfully with valid BigDecimal")
        void setAmount_WhenValidBigDecimalProvided_ThenAmountIsSet() {
            // Given
            BigDecimal expectedAmount = new BigDecimal("100.50");

            // When
            money.setAmount(expectedAmount);
            BigDecimal actualAmount = money.getAmount();

            // Then
            assertEquals(expectedAmount, actualAmount);
        }

        @Test
        @DisplayName("Should set and get zero amount successfully")
        void setAmount_WhenZeroAmountProvided_ThenZeroAmountIsSet() {
            // Given
            BigDecimal zeroAmount = BigDecimal.ZERO;

            // When
            money.setAmount(zeroAmount);
            BigDecimal actualAmount = money.getAmount();

            // Then
            assertEquals(zeroAmount, actualAmount);
        }

        @Test
        @DisplayName("Should set and get negative amount successfully")
        void setAmount_WhenNegativeAmountProvided_ThenNegativeAmountIsSet() {
            // Given
            BigDecimal negativeAmount = new BigDecimal("-50.25");

            // When
            money.setAmount(negativeAmount);
            BigDecimal actualAmount = money.getAmount();

            // Then
            assertEquals(negativeAmount, actualAmount);
        }

        @Test
        @DisplayName("Should set and get large amount successfully")
        void setAmount_WhenLargeAmountProvided_ThenLargeAmountIsSet() {
            // Given
            BigDecimal largeAmount = new BigDecimal("999999999.99");

            // When
            money.setAmount(largeAmount);
            BigDecimal actualAmount = money.getAmount();

            // Then
            assertEquals(largeAmount, actualAmount);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when amount is null")
        void setAmount_WhenNullAmountProvided_ThenThrowIllegalArgumentException() {
            // Given
            BigDecimal nullAmount = null;

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> money.setAmount(nullAmount)
            );
            assertEquals("Amount cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return null when amount is not set")
        void getAmount_WhenAmountNotSet_ThenReturnNull() {
            // Given
            // money object with no amount set

            // When
            BigDecimal actualAmount = money.getAmount();

            // Then
            assertNull(actualAmount);
        }

        @Test
        @DisplayName("Should preserve precision when setting decimal amount")
        void setAmount_WhenDecimalWithPrecisionProvided_ThenPrecisionIsPreserved() {
            // Given
            BigDecimal preciseAmount = new BigDecimal("123.456789");

            // When
            money.setAmount(preciseAmount);
            BigDecimal actualAmount = money.getAmount();

            // Then
            assertEquals(preciseAmount, actualAmount);
            assertEquals(preciseAmount.scale(), actualAmount.scale());
        }
    }

    @Nested
    @DisplayName("Currency Tests")
    class CurrencyTests {

        @Test
        @DisplayName("Should set and get currency successfully with valid string")
        void setCurrency_WhenValidCurrencyProvided_ThenCurrencyIsSet() {
            // Given
            String expectedCurrency = "USD";

            // When
            money.setCurrency(expectedCurrency);
            String actualCurrency = money.getCurrency();

            // Then
            assertEquals(expectedCurrency, actualCurrency);
        }

        @Test
        @DisplayName("Should set and get currency with different valid currencies")
        void setCurrency_WhenDifferentValidCurrenciesProvided_ThenCurrenciesAreSet() {
            // Given
            String[] validCurrencies = {"EUR", "GBP", "JPY", "CHF", "CAD"};

            for (String expectedCurrency : validCurrencies) {
                // When
                money.setCurrency(expectedCurrency);
                String actualCurrency = money.getCurrency();

                // Then
                assertEquals(expectedCurrency, actualCurrency);
            }
        }

        @Test
        @DisplayName("Should set and get lowercase currency successfully")
        void setCurrency_WhenLowerCaseCurrencyProvided_ThenLowerCaseCurrencyIsSet() {
            // Given
            String lowercaseCurrency = "usd";

            // When
            money.setCurrency(lowercaseCurrency);
            String actualCurrency = money.getCurrency();

            // Then
            assertEquals(lowercaseCurrency, actualCurrency);
        }

        @Test
        @DisplayName("Should set and get empty string currency")
        void setCurrency_WhenEmptyStringProvided_ThenEmptyStringIsSet() {
            // Given
            String emptyCurrency = "";

            // When
            money.setCurrency(emptyCurrency);
            String actualCurrency = money.getCurrency();

            // Then
            assertEquals(emptyCurrency, actualCurrency);
        }

        @Test
        @DisplayName("Should set and get currency with special characters")
        void setCurrency_WhenCurrencyWithSpecialCharactersProvided_ThenCurrencyIsSet() {
            // Given
            String specialCurrency = "US$";

            // When
            money.setCurrency(specialCurrency);
            String actualCurrency = money.getCurrency();

            // Then
            assertEquals(specialCurrency, actualCurrency);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when currency is null")
        void setCurrency_WhenNullCurrencyProvided_ThenThrowIllegalArgumentException() {
            // Given
            String nullCurrency = null;

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> money.setCurrency(nullCurrency)
            );
            assertEquals("Currency cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return null when currency is not set")
        void getCurrency_WhenCurrencyNotSet_ThenReturnNull() {
            // Given
            // money object with no currency set

            // When
            String actualCurrency = money.getCurrency();

            // Then
            assertNull(actualCurrency);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should set and get both amount and currency successfully")
        void setAmountAndCurrency_WhenValidValuesProvided_ThenBothValuesAreSet() {
            // Given
            BigDecimal expectedAmount = new BigDecimal("250.75");
            String expectedCurrency = "EUR";

            // When
            money.setAmount(expectedAmount);
            money.setCurrency(expectedCurrency);

            // Then
            assertEquals(expectedAmount, money.getAmount());
            assertEquals(expectedCurrency, money.getCurrency());
        }

        @Test
        @DisplayName("Should handle multiple updates to amount and currency")
        void updateAmountAndCurrency_WhenUpdatedMultipleTimes_ThenLatestValuesAreSet() {
            // Given
            BigDecimal firstAmount = new BigDecimal("100.00");
            String firstCurrency = "USD";
            BigDecimal secondAmount = new BigDecimal("200.50");
            String secondCurrency = "GBP";

            // When
            money.setAmount(firstAmount);
            money.setCurrency(firstCurrency);
            money.setAmount(secondAmount);
            money.setCurrency(secondCurrency);

            // Then
            assertEquals(secondAmount, money.getAmount());
            assertEquals(secondCurrency, money.getCurrency());
        }

        @Test
        @DisplayName("Should maintain independence between amount and currency validation")
        void validation_WhenOneFieldValidAndOtherInvalid_ThenOnlyInvalidFieldThrowsException() {
            // Given
            BigDecimal validAmount = new BigDecimal("100.00");
            String validCurrency = "USD";

            // When & Then - Valid amount, null currency
            assertDoesNotThrow(() -> money.setAmount(validAmount));
            assertThrows(IllegalArgumentException.class, () -> money.setCurrency(null));

            // When & Then - Valid currency, null amount
            assertDoesNotThrow(() -> money.setCurrency(validCurrency));
            assertThrows(IllegalArgumentException.class, () -> money.setAmount(null));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very small decimal amounts")
        void setAmount_WhenVerySmallDecimalProvided_ThenSmallDecimalIsSet() {
            // Given
            BigDecimal verySmallAmount = new BigDecimal("0.0001");

            // When
            money.setAmount(verySmallAmount);
            BigDecimal actualAmount = money.getAmount();

            // Then
            assertEquals(verySmallAmount, actualAmount);
        }

        @Test
        @DisplayName("Should trim currency with whitespace")
        void setCurrency_WhenCurrencyWithWhitespaceProvided_ThenWhitespaceIsPreserved() {
            // Given
            String currencyWithSpaces = " USD ";

            // When
            money.setCurrency(currencyWithSpaces);
            String actualCurrency = money.getCurrency();

            // Then
            assertNotEquals(currencyWithSpaces, actualCurrency);
            assertEquals(currencyWithSpaces.trim(), actualCurrency);
        }

        @Test
        @DisplayName("Should handle setting same values multiple times")
        void setSameValues_WhenCalledMultipleTimes_ThenValuesRemainConsistent() {
            // Given
            BigDecimal amount = new BigDecimal("100.00");
            String currency = "USD";

            // When
            money.setAmount(amount);
            money.setCurrency(currency);
            money.setAmount(amount); // Set same value again
            money.setCurrency(currency); // Set same value again

            // Then
            assertEquals(amount, money.getAmount());
            assertEquals(currency, money.getCurrency());
        }
    }

    @Nested
    @DisplayName("Object State Tests")
    class ObjectStateTests {

        @Test
        @DisplayName("Should create Money object with null initial values")
        void constructor_WhenObjectCreated_ThenInitialValuesAreNull() {
            // Given & When
            Money newMoney = new Money();

            // Then
            assertNull(newMoney.getAmount());
            assertNull(newMoney.getCurrency());
        }

        @Test
        @DisplayName("Should maintain object state after partial initialization")
        void partialInitialization_WhenOnlyAmountSet_ThenAmountIsSetAndCurrencyIsNull() {
            // Given
            BigDecimal amount = new BigDecimal("150.00");

            // When
            money.setAmount(amount);

            // Then
            assertEquals(amount, money.getAmount());
            assertNull(money.getCurrency());
        }

        @Test
        @DisplayName("Should maintain object state after partial initialization")
        void partialInitialization_WhenOnlyCurrencySet_ThenCurrencyIsSetAndAmountIsNull() {
            // Given
            String currency = "EUR";

            // When
            money.setCurrency(currency);

            // Then
            assertEquals(currency, money.getCurrency());
            assertNull(money.getAmount());
        }
    }
}
