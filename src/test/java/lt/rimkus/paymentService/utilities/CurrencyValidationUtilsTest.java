package lt.rimkus.paymentService.utilities;

import lt.rimkus.paymentService.enums.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyValidationUtilsTest {

    @ParameterizedTest
    @EnumSource(Currency.class)
    void testIsCurrencyNotValid_WithValidCurrencyCodes_ShouldReturnFalse(Currency currency) {
        // When
        boolean result = CurrencyValidationUtils.isCurrencyNotValid(currency.getCode());

        // Then
        assertFalse(result, "Currency code '" + currency.getCode() + "' should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INVALID_CURRENCY",
            "XYZ",
            "ABC",
            "123",
            "eur",  // case sensitivity test if codes are uppercase
            "usd",  // case sensitivity test if codes are uppercase
            "EUR ",  // with trailing space
            " EUR",  // with leading space
            "FAKE_CURRENCY"
    })
    void testIsCurrencyNotValid_WithInvalidCurrencyCodes_ShouldReturnTrue(String invalidCode) {
        // When
        boolean result = CurrencyValidationUtils.isCurrencyNotValid(invalidCode);

        // Then
        assertTrue(result, "Currency code '" + invalidCode + "' should be invalid");
    }

    @ParameterizedTest
    @NullSource
    void testIsCurrencyNotValid_WithNullCurrencyCode_ShouldReturnTrue(String nullCode) {
        // When
        boolean result = CurrencyValidationUtils.isCurrencyNotValid(nullCode);

        // Then
        assertTrue(result, "Null currency code should be invalid");
    }

    @ParameterizedTest
    @EmptySource
    void testIsCurrencyNotValid_WithEmptyCurrencyCode_ShouldReturnTrue(String emptyCode) {
        // When
        boolean result = CurrencyValidationUtils.isCurrencyNotValid(emptyCode);

        // Then
        assertTrue(result, "Empty currency code should be invalid");
    }

    @Test
    void testIsCurrencyNotValid_WithWhitespaceOnlyCode_ShouldReturnTrue() {
        // Given
        String whitespaceCode = "   ";

        // When
        boolean result = CurrencyValidationUtils.isCurrencyNotValid(whitespaceCode);

        // Then
        assertTrue(result, "Whitespace-only currency code should be invalid");
    }

    @Test
    void testPaymentTypeCurrencyMappingCompleteness() {

        // TYPE1 should only allow EUR
        assertFalse(CurrencyValidationUtils.isCurrencyNotValidForPaymentType("EUR", "TYPE1"));
        assertTrue(CurrencyValidationUtils.isCurrencyNotValidForPaymentType("USD", "TYPE1"));

        // TYPE2 should only allow USD
        assertFalse(CurrencyValidationUtils.isCurrencyNotValidForPaymentType("USD", "TYPE2"));
        assertTrue(CurrencyValidationUtils.isCurrencyNotValidForPaymentType("EUR", "TYPE2"));

        // TYPE3 should allow both EUR and USD
        assertFalse(CurrencyValidationUtils.isCurrencyNotValidForPaymentType("EUR", "TYPE3"));
        assertFalse(CurrencyValidationUtils.isCurrencyNotValidForPaymentType("USD", "TYPE3"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCurrencyPaymentTypeCombinations")
    void testIsCurrencyNotValidForPaymentType_InvalidCombinations_ShouldReturnTrue(
            String currencyCode, String paymentTypeCode) {
        // When
        boolean result = CurrencyValidationUtils.isCurrencyNotValidForPaymentType(currencyCode, paymentTypeCode);

        // Then
        assertTrue(result,
                String.format("Currency '%s' should be invalid for payment type '%s'",
                        currencyCode, paymentTypeCode));
    }

    private static Stream<Arguments> provideInvalidCurrencyPaymentTypeCombinations() {
        return Stream.of(
                Arguments.of(null, "TYPE1"),
                Arguments.of("EUR", null),
                Arguments.of(null, null),
                Arguments.of("", "TYPE1"),
                Arguments.of("EUR", ""),
                Arguments.of("EUR", "TYPE2"),
                Arguments.of("VALID", "TYPE2"),
                Arguments.of("EUR", "VALID"),
                Arguments.of("EUR", "TYPE4"),
                Arguments.of("USD", "TYPE4"),
                Arguments.of("eur", "TYPE1"),
                Arguments.of("EUR", "type1")
        );
    }

    @Test
    void testBothMethods_ConsistentResults() {
        // Test that both methods handle null/invalid inputs consistently
        String[] testCases = {null, "", "   ", "INVALID", "123", "!@#$%"};

        for (String testCase : testCases) {
            // Test currency validation
            assertDoesNotThrow(() -> {
                CurrencyValidationUtils.isCurrencyNotValid(testCase);
            }, "isCurrencyNotValid should not throw exception for: " + testCase);

            // Test currency-payment type validation
            assertDoesNotThrow(() -> {
                CurrencyValidationUtils.isCurrencyNotValidForPaymentType(testCase, "TYPE1");
                CurrencyValidationUtils.isCurrencyNotValidForPaymentType("EUR", testCase);
            }, "isCurrencyNotValidForPaymentType should not throw exception for: " + testCase);
        }
    }
}
