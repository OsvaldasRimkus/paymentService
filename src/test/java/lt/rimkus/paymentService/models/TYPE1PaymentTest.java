package lt.rimkus.paymentService.models;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.MoneyDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TYPE1PaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@DisplayName("TYPE1Payment Tests")
class TYPE1PaymentTest {

    private TYPE1Payment type1Payment;
    private CreatePaymentRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        type1Payment = new TYPE1Payment();
        type1Payment.setType("TYPE1");
        type1Payment.setMoney(new Money());
        type1Payment.getMoney().setAmount(BigDecimal.TEN);
        type1Payment.getMoney().setCurrency("EUR");
        type1Payment.setDebtor_iban("Debtor_IBAN");
        type1Payment.setCreditor_iban("Creditor_IBAN");

        requestDTO = new CreatePaymentRequestDTO();
        requestDTO.setType("TYPE1");
        requestDTO.setMoney(new MoneyDTO());
        requestDTO.getMoney().setAmount(BigDecimal.TEN);
        requestDTO.getMoney().setCurrency("EUR");
        requestDTO.setDebtor_iban("Debtor_IBAN");
        requestDTO.setCreditor_iban("Creditor_IBAN");
    }

    @Nested
    @DisplayName("Details Property Tests")
    class DetailsPropertyTests {

        @Test
        @DisplayName("Should set and get details correctly")
        void givenValidDetails_whenSettingAndGetting_thenDetailsAreCorrect() {
            // Given
            String expectedDetails = "Payment for order #12345";

            // When
            type1Payment.setDetails(expectedDetails);
            String actualDetails = type1Payment.getDetails();

            // Then
            assertEquals(expectedDetails, actualDetails);
        }

        @Test
        @DisplayName("Should handle null details")
        void givenNullDetails_whenSettingDetails_thenDetailsAreNull() {
            // Given
            String nullDetails = null;

            // When
            type1Payment.setDetails(nullDetails);
            String actualDetails = type1Payment.getDetails();

            // Then
            assertNull(actualDetails);
        }

        @Test
        @DisplayName("Should handle empty string details")
        void givenEmptyStringDetails_whenSettingDetails_thenDetailsAreEmpty() {
            // Given
            String emptyDetails = "";

            // When
            type1Payment.setDetails(emptyDetails);
            String actualDetails = type1Payment.getDetails();

            // Then
            assertEquals("", actualDetails);
        }
    }

    @Nested
    @DisplayName("Type Specific Validation Tests")
    class TypeSpecificValidationTests {

        @Test
        @DisplayName("Should pass validation when details are provided")
        void givenRequestWithValidDetails_whenValidating_thenNoExceptionIsThrown() {
            // Given
            requestDTO.setDetails("Valid payment details");

            // When & Then
            assertDoesNotThrow(() -> type1Payment.validateTypeSpecificRequirements(requestDTO));
        }

        @Test
        @DisplayName("Should throw exception when details are null")
        void givenRequestWithNullDetails_whenValidating_thenThrowsRequestValidationException() {
            // Given
            requestDTO.setDetails(null);

            // When
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> type1Payment.validateTypeSpecificRequirements(requestDTO)
            );

            // Then
            assertEquals("TYPE1 payment requires details to be provided", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when details are empty string")
        void givenRequestWithEmptyDetails_whenValidating_thenThrowsRequestValidationException() {
            // Given
            requestDTO.setDetails("");

            // When
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> type1Payment.validateTypeSpecificRequirements(requestDTO)
            );

            // Then
            assertEquals("TYPE1 payment requires details to be provided", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when details contain only whitespace")
        void givenRequestWithWhitespaceOnlyDetails_whenValidating_thenThrowsRequestValidationException() {
            // Given
            requestDTO.setDetails("   \t\n   ");

            // When
            RequestValidationException exception = assertThrows(
                    RequestValidationException.class,
                    () -> type1Payment.validateTypeSpecificRequirements(requestDTO)
            );

            // Then
            assertEquals("TYPE1 payment requires details to be provided", exception.getMessage());
        }

        @Test
        @DisplayName("Should pass validation when details have leading/trailing whitespace but content")
        void givenRequestWithDetailsHavingWhitespaceButContent_whenValidating_thenNoExceptionIsThrown() {
            // Given
            requestDTO.setDetails("  Valid details with whitespace  ");

            // When & Then
            assertDoesNotThrow(() -> type1Payment.validateTypeSpecificRequirements(requestDTO));
        }

        @Test
        @DisplayName("Should use StringUtils.isEmpty for validation")
        void givenRequestWithEmptyDetails_whenValidating_thenStringUtilsIsUsed() {
            // Given
            requestDTO.setDetails("");

            try (MockedStatic<StringUtils> stringUtilsMock = Mockito.mockStatic(StringUtils.class)) {
                stringUtilsMock.when(() -> StringUtils.isEmpty("")).thenReturn(true);

                // When
                RequestValidationException exception = assertThrows(
                        RequestValidationException.class, () -> type1Payment.validateTypeSpecificRequirements(requestDTO)
                );

                // Then
                assertEquals("TYPE1 payment requires details to be provided", exception.getMessage());
                stringUtilsMock.verify(() -> StringUtils.isEmpty(""));
            }
        }
    }

    @Nested
    @DisplayName("Entity Data Population Tests")
    class EntityDataPopulationTests {

        @Test
        @DisplayName("Should populate entity data from request DTO")
        void givenValidRequestDTO_whenPopulatingEntityData_thenEntityIsPopulatedCorrectly() {
            // Given
            String expectedDetails = "Order payment for #67890";
            requestDTO.setDetails(expectedDetails);
            TYPE1Payment spyPayment = spy(type1Payment);

            // When
            spyPayment.populateEntityData(requestDTO);

            // Then
            assertEquals(expectedDetails, spyPayment.getDetails());
            verify(spyPayment).populateCommonData(requestDTO);
        }

        @Test
        @DisplayName("Should handle null details in request DTO")
        void givenRequestDTOWithNullDetails_whenPopulatingEntityData_thenDetailsAreNull() {
            // Given
            requestDTO.setDetails(null);
            TYPE1Payment spyPayment = spy(type1Payment);

            // When
            spyPayment.populateEntityData(requestDTO);

            // Then
            assertNull(spyPayment.getDetails());
            verify(spyPayment).populateCommonData(requestDTO);
        }

        @Test
        @DisplayName("Should handle empty details in request DTO")
        void givenRequestDTOWithEmptyDetails_whenPopulatingEntityData_thenDetailsAreEmpty() {
            // Given
            String emptyDetails = "";
            requestDTO.setDetails(emptyDetails);
            TYPE1Payment spyPayment = spy(type1Payment);

            // When
            spyPayment.populateEntityData(requestDTO);

            // Then
            assertEquals(emptyDetails, spyPayment.getDetails());
            verify(spyPayment).populateCommonData(requestDTO);
        }
    }

    @Nested
    @DisplayName("DTO Conversion Tests")
    class DTOConversionTests {

        @Test
        @DisplayName("Should convert to DTO with all data populated")
        void givenPopulatedEntity_whenConvertingToDTO_thenDTOIsCorrectlyPopulated() {
            // Given
            String expectedDetails = "Subscription payment details";
            type1Payment.setDetails(expectedDetails);
            TYPE1Payment spyPayment = spy(type1Payment);

            // When
            PaymentDTO result = spyPayment.convertToDTO();

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE1PaymentDTO.class, result);

            TYPE1PaymentDTO type1DTO = (TYPE1PaymentDTO) result;
            assertEquals(expectedDetails, type1DTO.getDetails());
            verify(spyPayment).populateCommonDTOData(type1DTO);
        }

        @Test
        @DisplayName("Should convert to DTO when details are null")
        void givenEntityWithNullDetails_whenConvertingToDTO_thenDTOHasNullDetails() {
            // Given
            type1Payment.setDetails(null);
            TYPE1Payment spyPayment = spy(type1Payment);

            // When
            PaymentDTO result = spyPayment.convertToDTO();

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE1PaymentDTO.class, result);

            TYPE1PaymentDTO type1DTO = (TYPE1PaymentDTO) result;
            assertNull(type1DTO.getDetails());
            verify(spyPayment).populateCommonDTOData(type1DTO);
        }

        @Test
        @DisplayName("Should convert to DTO when details are empty")
        void givenEntityWithEmptyDetails_whenConvertingToDTO_thenDTOHasEmptyDetails() {
            // Given
            String emptyDetails = "";
            type1Payment.setDetails(emptyDetails);
            TYPE1Payment spyPayment = spy(type1Payment);

            // When
            PaymentDTO result = spyPayment.convertToDTO();

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE1PaymentDTO.class, result);

            TYPE1PaymentDTO type1DTO = (TYPE1PaymentDTO) result;
            assertEquals(emptyDetails, type1DTO.getDetails());
            verify(spyPayment).populateCommonDTOData(type1DTO);
        }

        @Test
        @DisplayName("Should return TYPE1PaymentDTO instance")
        void givenAnyEntityState_whenConvertingToDTO_thenReturnsCorrectDTOType() {
            // Given
            type1Payment.setDetails("Any details");

            // When
            PaymentDTO result = type1Payment.convertToDTO();

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE1PaymentDTO.class, result);
            assertInstanceOf(PaymentDTO.class, result);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete flow from request to DTO")
        void givenValidRequest_whenProcessingCompleteFlow_thenAllOperationsSucceedAndDTOIsCorrect() {
            // Given
            String testDetails = "Complete flow test details";
            requestDTO.setDetails(testDetails);
            TYPE1Payment spyPayment = spy(type1Payment);

            // When - Validate
            assertDoesNotThrow(() -> spyPayment.validateTypeSpecificRequirements(requestDTO));

            // When - Populate
            spyPayment.populateEntityData(requestDTO);

            // When - Convert
            PaymentDTO resultDTO = spyPayment.convertToDTO();

            // Then
            assertEquals(testDetails, spyPayment.getDetails());
            assertInstanceOf(TYPE1PaymentDTO.class, resultDTO);
            assertEquals(testDetails, ((TYPE1PaymentDTO) resultDTO).getDetails());

            // Verify all super method calls
            verify(spyPayment).populateCommonData(requestDTO);
            verify(spyPayment).populateCommonDTOData(any(TYPE1PaymentDTO.class));
        }

        @Test
        @DisplayName("Should fail validation but still allow population and conversion")
        void givenInvalidRequest_whenBypassingValidationAndProcessing_thenPopulationAndConversionStillWork() {
            // Given
            requestDTO.setDetails(null);
            TYPE1Payment spyPayment = spy(type1Payment);

            // When - Validation should fail
            assertThrows(RequestValidationException.class, () -> spyPayment.validateTypeSpecificRequirements(requestDTO));

            // When - But population and conversion should still work
            spyPayment.populateEntityData(requestDTO);
            PaymentDTO resultDTO = spyPayment.convertToDTO();

            // Then
            assertNull(spyPayment.getDetails());
            assertInstanceOf(TYPE1PaymentDTO.class, resultDTO);
            assertNull(((TYPE1PaymentDTO) resultDTO).getDetails());
        }
    }
}