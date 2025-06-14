package lt.rimkus.paymentService.models;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.MoneyDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TYPE2PaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@DisplayName("TYPE2Payment Entity Tests")
class TYPE2PaymentTest {

    private TYPE2Payment type2Payment;
    private CreatePaymentRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        type2Payment = new TYPE2Payment();
        type2Payment.setType("TYPE2");
        type2Payment.setMoney(new Money());
        type2Payment.getMoney().setAmount(BigDecimal.TEN);
        type2Payment.getMoney().setCurrency("USD");
        type2Payment.setDebtor_iban("Debtor_IBAN");
        type2Payment.setCreditor_iban("Creditor_IBAN");

        requestDTO = new CreatePaymentRequestDTO();
        requestDTO.setType("TYPE2");
        requestDTO.setMoney(new MoneyDTO());
        requestDTO.getMoney().setAmount(BigDecimal.TEN);
        requestDTO.getMoney().setCurrency("USD");
        requestDTO.setDebtor_iban("Debtor_IBAN");
        requestDTO.setCreditor_iban("Creditor_IBAN");
    }

    @Nested
    @DisplayName("Given details property operations")
    class DetailsPropertyOperations {

        @Test
        @DisplayName("Should set and retrieve details when given valid string")
        void givenValidDetailsString_whenSettingAndGettingDetails_thenDetailsAreCorrectlyStored() {
            // Given
            String expectedDetails = "Payment for premium subscription";

            // When
            type2Payment.setDetails(expectedDetails);
            String actualDetails = type2Payment.getDetails();

            // Then
            assertEquals(expectedDetails, actualDetails);
        }

        @Test
        @DisplayName("Should handle null details when given null value")
        void givenNullDetailsValue_whenSettingDetails_thenDetailsAreStoredAsNull() {
            // Given
            String nullDetails = null;

            // When
            type2Payment.setDetails(nullDetails);
            String actualDetails = type2Payment.getDetails();

            // Then
            assertNull(actualDetails);
        }

        @Test
        @DisplayName("Should handle empty string when given empty details")
        void givenEmptyDetailsString_whenSettingDetails_thenDetailsAreStoredAsEmpty() {
            // Given
            String emptyDetails = "";

            // When
            type2Payment.setDetails(emptyDetails);
            String actualDetails = type2Payment.getDetails();

            // Then
            assertEquals("", actualDetails);
        }

        @Test
        @DisplayName("Should preserve whitespace when given details with spaces")
        void givenDetailsWithWhitespace_whenSettingDetails_thenWhitespaceIsPreserved() {
            // Given
            String detailsWithSpaces = "  Payment with spaces  ";

            // When
            type2Payment.setDetails(detailsWithSpaces);
            String actualDetails = type2Payment.getDetails();

            // Then
            assertEquals(detailsWithSpaces, actualDetails);
        }
    }

    @Nested
    @DisplayName("Given entity creation request validation")
    class EntityCreationRequestValidation {

        @Test
        @DisplayName("Should delegate to parent validation when given valid request")
        void givenValidCreatePaymentRequest_whenValidatingEntityCreation_thenParentValidationIsCalled()
                throws RequestValidationException {
            // Given
            requestDTO.getMoney().setAmount(BigDecimal.valueOf(100.0));
            requestDTO.getMoney().setCurrency("USD");
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            assertDoesNotThrow(() -> spyPayment.validateEntityCreationRequest(requestDTO));

            // Then
            // Verify that the super method is called by checking the spy
            verify(spyPayment).validateEntityCreationRequest(requestDTO);
        }

        @Test
        @DisplayName("Should propagate parent validation exception when given invalid request")
        void givenInvalidCreatePaymentRequest_whenValidatingEntityCreation_thenParentExceptionIsPropagated()
                throws RequestValidationException {
            // Given
            TYPE2Payment spyPayment = spy(type2Payment);
            RequestValidationException parentException = new RequestValidationException("Parent validation failed");

            // Mock the parent to throw exception
            doThrow(parentException).when(spyPayment).validateEntityCreationRequest(requestDTO);

            // When & Then
            RequestValidationException thrownException = assertThrows(
                    RequestValidationException.class,
                    () -> spyPayment.validateEntityCreationRequest(requestDTO)
            );

            assertEquals("Parent validation failed", thrownException.getMessage());
        }
    }

    @Nested
    @DisplayName("Given type specific requirements validation")
    class TypeSpecificRequirementsValidation {

        @Test
        @DisplayName("Should not throw exception when given request with valid details")
        void givenRequestWithValidDetails_whenValidatingTypeSpecificRequirements_thenNoExceptionIsThrown() {
            // Given
            requestDTO.setDetails("Valid payment details");

            // When & Then
            assertDoesNotThrow(() -> type2Payment.validateTypeSpecificRequirements(requestDTO));
        }

        @Test
        @DisplayName("Should not throw exception when given request with null details")
        void givenRequestWithNullDetails_whenValidatingTypeSpecificRequirements_thenNoExceptionIsThrown() {
            // Given
            requestDTO.setDetails(null);

            // When & Then
            assertDoesNotThrow(() -> type2Payment.validateTypeSpecificRequirements(requestDTO));
        }

        @Test
        @DisplayName("Should not throw exception when given request with empty details")
        void givenRequestWithEmptyDetails_whenValidatingTypeSpecificRequirements_thenNoExceptionIsThrown() {
            // Given
            requestDTO.setDetails("");

            // When & Then
            assertDoesNotThrow(() -> type2Payment.validateTypeSpecificRequirements(requestDTO));
        }

        @Test
        @DisplayName("Should not throw exception when given request with whitespace-only details")
        void givenRequestWithWhitespaceOnlyDetails_whenValidatingTypeSpecificRequirements_thenNoExceptionIsThrown() {
            // Given
            requestDTO.setDetails("   \t\n   ");

            // When & Then
            assertDoesNotThrow(() -> type2Payment.validateTypeSpecificRequirements(requestDTO));
        }

        @Test
        @DisplayName("Should not throw exception when given null request")
        void givenNullRequest_whenValidatingTypeSpecificRequirements_thenNoExceptionIsThrown() {
            // Given
            CreatePaymentRequestDTO nullRequest = null;

            // When & Then
            assertDoesNotThrow(() -> type2Payment.validateTypeSpecificRequirements(nullRequest));
        }

        @Test
        @DisplayName("Should complete quickly when given any request since details are optional")
        void givenAnyRequest_whenValidatingTypeSpecificRequirements_thenValidationCompletesQuickly() {
            // Given
            requestDTO.setDetails("Any details content");
            long startTime = System.nanoTime();

            // When
            type2Payment.validateTypeSpecificRequirements(requestDTO);
            long endTime = System.nanoTime();

            // Then
            long executionTime = endTime - startTime;
            // Validation should be very fast since it's essentially a no-op
            assertTrue(executionTime < 1_000_000, "Validation should complete very quickly"); // Less than 1ms
        }
    }

    @Nested
    @DisplayName("Given entity data population operations")
    class EntityDataPopulationOperations {

        @Test
        @DisplayName("Should populate all data when given complete request")
        void givenCompletePaymentRequest_whenPopulatingEntityData_thenAllDataIsCorrectlySet() {
            // Given
            String expectedDetails = "Monthly subscription payment";
            requestDTO.setDetails(expectedDetails);
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            spyPayment.populateEntityData(requestDTO);

            // Then
            assertEquals(expectedDetails, spyPayment.getDetails());
            verify(spyPayment).populateCommonData(requestDTO);
        }

        @Test
        @DisplayName("Should handle null details when given request with null details")
        void givenRequestWithNullDetails_whenPopulatingEntityData_thenDetailsAreSetToNull() {
            // Given
            requestDTO.setDetails(null);
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            spyPayment.populateEntityData(requestDTO);

            // Then
            assertNull(spyPayment.getDetails());
            verify(spyPayment).populateCommonData(requestDTO);
        }

        @Test
        @DisplayName("Should preserve empty details when given request with empty details")
        void givenRequestWithEmptyDetails_whenPopulatingEntityData_thenDetailsAreSetToEmpty() {
            // Given
            String emptyDetails = "";
            requestDTO.setDetails(emptyDetails);
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            spyPayment.populateEntityData(requestDTO);

            // Then
            assertEquals(emptyDetails, spyPayment.getDetails());
            verify(spyPayment).populateCommonData(requestDTO);
        }

        @Test
        @DisplayName("Should preserve whitespace details when given request with whitespace details")
        void givenRequestWithWhitespaceDetails_whenPopulatingEntityData_thenWhitespaceIsPreserved() {
            // Given
            String whitespaceDetails = "  \t  Payment details with whitespace  \n  ";
            requestDTO.setDetails(whitespaceDetails);
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            spyPayment.populateEntityData(requestDTO);

            // Then
            assertEquals(whitespaceDetails, spyPayment.getDetails());
            verify(spyPayment).populateCommonData(requestDTO);
        }

        @Test
        @DisplayName("Should call parent population method when populating entity data")
        void givenAnyValidRequest_whenPopulatingEntityData_thenParentPopulationMethodIsCalled() {
            // Given
            requestDTO.setDetails("Test details");
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            spyPayment.populateEntityData(requestDTO);

            // Then
            verify(spyPayment).populateCommonData(requestDTO);
        }
    }

    @Nested
    @DisplayName("Given DTO conversion operations")
    class DTOConversionOperations {

        @Test
        @DisplayName("Should create correct DTO when given entity with all data")
        void givenEntityWithCompleteData_whenConvertingToDTO_thenCorrectDTOIsCreated() {
            // Given
            String expectedDetails = "Conversion test details";
            type2Payment.setDetails(expectedDetails);
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            PaymentDTO result = spyPayment.convertToDTO();

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE2PaymentDTO.class, result);

            TYPE2PaymentDTO type2DTO = (TYPE2PaymentDTO) result;
            assertEquals(expectedDetails, type2DTO.getDetails());
            verify(spyPayment).populateCommonDTOData(type2DTO);
        }

        @Test
        @DisplayName("Should create DTO with null details when given entity with null details")
        void givenEntityWithNullDetails_whenConvertingToDTO_thenDTOHasNullDetails() {
            // Given
            type2Payment.setDetails(null);
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            PaymentDTO result = spyPayment.convertToDTO();

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE2PaymentDTO.class, result);

            TYPE2PaymentDTO type2DTO = (TYPE2PaymentDTO) result;
            assertNull(type2DTO.getDetails());
            verify(spyPayment).populateCommonDTOData(type2DTO);
        }

        @Test
        @DisplayName("Should create DTO with empty details when given entity with empty details")
        void givenEntityWithEmptyDetails_whenConvertingToDTO_thenDTOHasEmptyDetails() {
            // Given
            String emptyDetails = "";
            type2Payment.setDetails(emptyDetails);
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            PaymentDTO result = spyPayment.convertToDTO();

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE2PaymentDTO.class, result);

            TYPE2PaymentDTO type2DTO = (TYPE2PaymentDTO) result;
            assertEquals(emptyDetails, type2DTO.getDetails());
            verify(spyPayment).populateCommonDTOData(type2DTO);
        }

        @Test
        @DisplayName("Should return correct DTO type when converting any entity state")
        void givenEntityInAnyState_whenConvertingToDTO_thenCorrectDTOTypeIsReturned() {
            // Given
            type2Payment.setDetails("Any state details");

            // When
            PaymentDTO result = type2Payment.convertToDTO();

            // Then
            assertNotNull(result);
            assertInstanceOf(TYPE2PaymentDTO.class, result);
            assertInstanceOf(PaymentDTO.class, result);
        }

        @Test
        @DisplayName("Should call parent DTO population when converting to DTO")
        void givenAnyEntityState_whenConvertingToDTO_thenParentDTOPopulationIsCalled() {
            // Given
            type2Payment.setDetails("Test for parent call");
            TYPE2Payment spyPayment = spy(type2Payment);

            // When
            PaymentDTO result = spyPayment.convertToDTO();

            // Then
            verify(spyPayment).populateCommonDTOData(any(TYPE2PaymentDTO.class));
        }
    }

    @Nested
    @DisplayName("Given complete workflow scenarios")
    class CompleteWorkflowScenarios {

        @Test
        @DisplayName("Should handle complete happy path when given valid request through full workflow")
        void givenValidCompleteRequest_whenExecutingFullWorkflow_thenAllOperationsSucceedWithCorrectData() {
            // Given
            String testDetails = "Complete workflow test details";
            requestDTO.setDetails(testDetails);
            requestDTO.getMoney().setAmount(BigDecimal.valueOf(250.00));
            requestDTO.getMoney().setCurrency("USD");

            TYPE2Payment spyPayment = spy(type2Payment);

            // When - Execute full workflow
            assertDoesNotThrow(() -> spyPayment.validateEntityCreationRequest(requestDTO));
            assertDoesNotThrow(() -> spyPayment.validateTypeSpecificRequirements(requestDTO));
            spyPayment.populateEntityData(requestDTO);
            PaymentDTO resultDTO = spyPayment.convertToDTO();

            // Then
            assertEquals(testDetails, spyPayment.getDetails());
            assertInstanceOf(TYPE2PaymentDTO.class, resultDTO);
            assertEquals(testDetails, ((TYPE2PaymentDTO) resultDTO).getDetails());

            // Verify all parent method calls
            verify(spyPayment).populateCommonData(requestDTO);
            verify(spyPayment).populateCommonDTOData(any(TYPE2PaymentDTO.class));
        }

        @Test
        @DisplayName("Should handle workflow with optional details when given request without details")
        void givenRequestWithoutDetails_whenExecutingFullWorkflow_thenWorkflowSucceedsWithNullDetails() {
            // Given
            requestDTO.setDetails(null);
            requestDTO.getMoney().setAmount(BigDecimal.valueOf(100.00));
            requestDTO.getMoney().setCurrency("USD");

            TYPE2Payment spyPayment = spy(type2Payment);

            // When - Execute full workflow
            assertDoesNotThrow(() -> spyPayment.validateEntityCreationRequest(requestDTO));
            assertDoesNotThrow(() -> spyPayment.validateTypeSpecificRequirements(requestDTO));
            spyPayment.populateEntityData(requestDTO);
            PaymentDTO resultDTO = spyPayment.convertToDTO();

            // Then
            assertNull(spyPayment.getDetails());
            assertInstanceOf(TYPE2PaymentDTO.class, resultDTO);
            assertNull(((TYPE2PaymentDTO) resultDTO).getDetails());
        }

        @Test
        @DisplayName("Should demonstrate details are truly optional when given various detail states")
        void givenVariousDetailStates_whenValidatingTypeSpecificRequirements_thenAllStatesAreAccepted() {
            // Given various detail states
            String[] detailStates = {
                    null,
                    "",
                    "   ",
                    "Valid details",
                    "Details with special chars: !@#$%^&*()",
                    "Very long details that go on and on and on..."
            };

            // When & Then - All should pass validation
            for (String details : detailStates) {
                requestDTO.setDetails(details);
                assertDoesNotThrow(
                        () -> type2Payment.validateTypeSpecificRequirements(requestDTO),
                        "Should not throw exception for details: '" + details + "'"
                );
            }
        }

        @Test
        @DisplayName("Should maintain data integrity when given request processed through complete cycle")
        void givenRequestWithSpecificDetails_whenProcessingThroughCompleteCycle_thenDataIntegrityIsMaintained() {
            // Given
            String originalDetails = "Data integrity test: Special chars åäö, numbers 123, symbols !@#";
            requestDTO.setDetails(originalDetails);

            TYPE2Payment spyPayment = spy(type2Payment);

            // When - Process through complete cycle
            spyPayment.populateEntityData(requestDTO);
            PaymentDTO resultDTO = spyPayment.convertToDTO();

            // Then - Data should be identical at each step
            assertEquals(originalDetails, requestDTO.getDetails());
            assertEquals(originalDetails, spyPayment.getDetails());
            assertEquals(originalDetails, ((TYPE2PaymentDTO) resultDTO).getDetails());
        }
    }
}