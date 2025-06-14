package lt.rimkus.paymentService.models;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.MoneyDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.DTOs.TYPE3PaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class TYPE3PaymentTest {

    private TYPE3Payment type3Payment;
    private CreatePaymentRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        type3Payment = new TYPE3Payment();
        type3Payment.setType("TYPE3");
        type3Payment.setMoney(new Money());
        type3Payment.getMoney().setAmount(BigDecimal.TEN);
        type3Payment.getMoney().setCurrency("USD");
        type3Payment.setDebtor_iban("Debtor_IBAN");
        type3Payment.setCreditor_iban("Creditor_IBAN");

        requestDTO = new CreatePaymentRequestDTO();
        requestDTO.setType("TYPE3");
        requestDTO.setMoney(new MoneyDTO());
        requestDTO.getMoney().setAmount(BigDecimal.TEN);
        requestDTO.getMoney().setCurrency("USD");
        requestDTO.setDebtor_iban("Debtor_IBAN");
        requestDTO.setCreditor_iban("Creditor_IBAN");
    }

    @Test
    @DisplayName("Should validate entity creation request successfully when all validations pass")
    void validateEntityCreationRequest_WhenAllValidationsPass_ThenNoExceptionThrown() {
        // Given
        requestDTO.setCreditorBankBIC("Bank BIC");

        // When & Then
        assertDoesNotThrow(() -> type3Payment.validateEntityCreationRequest(requestDTO));
    }

    @Test
    @DisplayName("Should throw RequestValidationException when super validation fails")
    void validateEntityCreationRequest_WhenSuperValidationFails_ThenThrowException() throws RequestValidationException {
        // Given
        TYPE3Payment spyPayment = spy(type3Payment);
        doThrow(new RequestValidationException("Super validation failed"))
                .when((Payment) spyPayment).validateEntityCreationRequest(requestDTO);

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> spyPayment.validateEntityCreationRequest(requestDTO)
        );
        assertEquals("Super validation failed", exception.getMessage());
    }

    @Test
    @DisplayName("Should pass validation when creditor bank BIC is provided")
    void validateTypeSpecificRequirements_WhenCreditorBankBICProvided_ThenValidationPasses() {
        // Given
        requestDTO.setCreditorBankBIC("BANK BIC");

        // When & Then
        assertDoesNotThrow(() -> type3Payment.validateTypeSpecificRequirements(requestDTO));
    }

    @Test
    @DisplayName("Should throw RequestValidationException when creditor bank BIC is null")
    void validateTypeSpecificRequirements_WhenCreditorBankBICIsNull_ThenThrowException() {
        // Given
        requestDTO.setCreditorBankBIC(null);

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> type3Payment.validateTypeSpecificRequirements(requestDTO)
        );
        assertEquals("TYPE3 payment requires creditor bank BIC to be provided", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw RequestValidationException when creditor bank BIC is empty string")
    void validateTypeSpecificRequirements_WhenCreditorBankBICIsEmpty_ThenThrowException() {
        // Given
        requestDTO.setCreditorBankBIC("");

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> type3Payment.validateTypeSpecificRequirements(requestDTO)
        );
        assertEquals("TYPE3 payment requires creditor bank BIC to be provided", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw RequestValidationException when creditor bank BIC is whitespace only")
    void validateTypeSpecificRequirements_WhenCreditorBankBICIsWhitespace_ThenThrowException() {
        // Given
        requestDTO.setCreditorBankBIC("   ");

        // When & Then
        RequestValidationException exception = assertThrows(
                RequestValidationException.class,
                () -> type3Payment.validateTypeSpecificRequirements(requestDTO)
        );
        assertEquals("TYPE3 payment requires creditor bank BIC to be provided", exception.getMessage());
    }

    @Test
    @DisplayName("Should populate entity data correctly")
    void populateEntityData_WhenCalledWithValidDTO_ThenPopulatesAllFields() {
        // Given
        String expectedBIC = "Bank BIC";
        requestDTO.setCreditorBankBIC(expectedBIC);
        TYPE3Payment spyPayment = spy(type3Payment);

        // When
        spyPayment.populateEntityData(requestDTO);

        // Then
        verify((Payment) spyPayment).populateCommonData(requestDTO);
        assertEquals(expectedBIC, spyPayment.getCreditorBankBIC());
    }

    @Test
    @DisplayName("Should convert to DTO correctly")
    void convertToDTO_WhenEntityHasData_ThenReturnsCorrectDTO() {
        // Given
        String expectedBIC = "CHASUS33";
        type3Payment.setCreditorBankBIC(expectedBIC);
        TYPE3Payment spyPayment = spy(type3Payment);

        // When
        PaymentDTO result = spyPayment.convertToDTO();

        // Then
        assertNotNull(result);
        assertInstanceOf(TYPE3PaymentDTO.class, result);
        TYPE3PaymentDTO type3DTO = (TYPE3PaymentDTO) result;
        assertEquals(expectedBIC, type3DTO.getCreditorBankBIC());
        verify((Payment) spyPayment).populateCommonDTOData(type3DTO);
    }

    @Test
    @DisplayName("Should set and get creditor bank BIC correctly")
    void setAndGetCreditorBankBIC_WhenCalledWithValidValue_ThenStoresAndReturnsCorrectly() {
        // Given
        String expectedBIC = "BNPAFRPP";

        // When
        type3Payment.setCreditorBankBIC(expectedBIC);
        String actualBIC = type3Payment.getCreditorBankBIC();

        // Then
        assertEquals(expectedBIC, actualBIC);
    }

    @Test
    @DisplayName("Should handle null creditor bank BIC in getter/setter")
    void setAndGetCreditorBankBIC_WhenCalledWithNull_ThenHandlesNullCorrectly() {
        // Given
        String nullBIC = null;

        // When
        type3Payment.setCreditorBankBIC(nullBIC);
        String actualBIC = type3Payment.getCreditorBankBIC();

        // Then
        assertNull(actualBIC);
    }

    @Test
    @DisplayName("Should convert to DTO with null BIC")
    void convertToDTO_WhenCreditorBankBICIsNull_ThenDTOHasNullBIC() {
        // Given
        type3Payment.setCreditorBankBIC(null);
        TYPE3Payment spyPayment = spy(type3Payment);

        // When
        PaymentDTO result = spyPayment.convertToDTO();

        // Then
        assertNotNull(result);
        assertInstanceOf(TYPE3PaymentDTO.class, result);
        TYPE3PaymentDTO type3DTO = (TYPE3PaymentDTO) result;
        assertNull(type3DTO.getCreditorBankBIC());
    }

    @Test
    @DisplayName("Should maintain inheritance behavior")
    void inheritance_WhenInstanceCreated_ThenIsInstanceOfPayment() {
        // Given & When
        TYPE3Payment payment = new TYPE3Payment();

        // Then
        assertInstanceOf(Payment.class, payment);
    }
}