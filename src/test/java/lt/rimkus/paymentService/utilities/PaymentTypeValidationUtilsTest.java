package lt.rimkus.paymentService.utilities;

import lt.rimkus.paymentService.enums.PaymentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTypeValidationUtilsTest {

    @ParameterizedTest
    @EnumSource(PaymentType.class)
    void testIsPaymentTypeNotValid_WithAllValidEnumValues_ShouldReturnFalse(PaymentType paymentType) {
        // When
        boolean result = PaymentTypeValidationUtils.isPaymentTypeNotValid(paymentType.getCode());

        // Then
        assertFalse(result,
                "Payment type code '" + paymentType.getCode() + "' should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INVALID_CODE",
            "UNKNOWN",
            "WRONG_TYPE",
            "ABC123",
            "type1",  // case sensitivity test if codes are uppercase
            "TYPE1 ",  // with trailing space
            " TYPE1",  // with leading space
            "TYPE_INVALID"
    })
    void testIsPaymentTypeNotValid_WithInvalidPaymentTypeCodes_ShouldReturnTrue(String invalidCode) {
        // When
        boolean result = PaymentTypeValidationUtils.isPaymentTypeNotValid(invalidCode);

        // Then
        assertTrue(result,
                "Payment type code '" + invalidCode + "' should be invalid");
    }

    @ParameterizedTest
    @NullSource
    void testIsPaymentTypeNotValid_WithNullPaymentTypeCode_ShouldReturnTrue(String nullCode) {
        // When
        boolean result = PaymentTypeValidationUtils.isPaymentTypeNotValid(nullCode);

        // Then
        assertTrue(result, "Null payment type code should be invalid");
    }

    @ParameterizedTest
    @EmptySource
    void testIsPaymentTypeNotValid_WithEmptyPaymentTypeCode_ShouldReturnTrue(String emptyCode) {
        // When
        boolean result = PaymentTypeValidationUtils.isPaymentTypeNotValid(emptyCode);

        // Then
        assertTrue(result, "Empty payment type code should be invalid");
    }

    @Test
    void testIsPaymentTypeNotValid_WithWhitespaceOnlyCode_ShouldReturnTrue() {
        // Given
        String whitespaceCode = "   ";

        // When
        boolean result = PaymentTypeValidationUtils.isPaymentTypeNotValid(whitespaceCode);

        // Then
        assertTrue(result, "Whitespace-only payment type code should be invalid");
    }

    @Test
    void testIsPaymentTypeNotValid_CaseSensitivityTest_ShouldReturnTrue() {
        // Test with lowercase version of valid codes
        for (PaymentType paymentType : PaymentType.values()) {
            String lowercaseCode = paymentType.getCode().toLowerCase();

            // When
            boolean result = PaymentTypeValidationUtils.isPaymentTypeNotValid(lowercaseCode);

            // Then
            assertTrue(result,
                    "Lowercase version '" + lowercaseCode + "' of valid code '" +
                            paymentType.getCode() + "' should be invalid");
        }
    }

    @Test
    void testIsPaymentTypeNotValid_WithMixedCaseValidCode() {
        // Test with mixed case version
        for (PaymentType paymentType : PaymentType.values()) {
            String originalCode = paymentType.getCode();
            if (originalCode.length() > 1) {
                String mixedCaseCode = originalCode.substring(0, 1).toLowerCase() +
                        originalCode.substring(1).toUpperCase();

                // When
                boolean result = PaymentTypeValidationUtils.isPaymentTypeNotValid(mixedCaseCode);

                // Then
                assertTrue(result,
                        "Mixed case version '" + mixedCaseCode + "' of valid code '" +
                                originalCode + "' should be invalid");
            }
        }
    }

    @Test
    void testIsPaymentTypeNotValid_MethodNeverThrowsException() {
        // Test that the method handles all edge cases gracefully without throwing exceptions
        String[] testCases = {
                null,
                "",
                "   ",
                "INVALID",
                "123",
                "!@#$%",
                "a".repeat(1000), // very long string
                "\n\t\r",
                "unicode_测试"
        };

        for (String testCase : testCases) {
            // When & Then
            assertDoesNotThrow(() -> {
                PaymentTypeValidationUtils.isPaymentTypeNotValid(testCase);
            }, "Method should not throw exception for input: " + testCase);
        }
    }

    @Test
    void testIsPaymentTypeNotValid_ConsistentResults() {
        // Test that multiple calls with the same input return consistent results
        String validCode = PaymentType.values()[0].getCode();
        String invalidCode = "DEFINITELY_INVALID";

        // Test valid code consistency
        boolean firstValidResult = PaymentTypeValidationUtils.isPaymentTypeNotValid(validCode);
        boolean secondValidResult = PaymentTypeValidationUtils.isPaymentTypeNotValid(validCode);
        assertEquals(firstValidResult, secondValidResult,
                "Method should return consistent results for valid codes");

        // Test invalid code consistency
        boolean firstInvalidResult = PaymentTypeValidationUtils.isPaymentTypeNotValid(invalidCode);
        boolean secondInvalidResult = PaymentTypeValidationUtils.isPaymentTypeNotValid(invalidCode);
        assertEquals(firstInvalidResult, secondInvalidResult,
                "Method should return consistent results for invalid codes");
    }

}