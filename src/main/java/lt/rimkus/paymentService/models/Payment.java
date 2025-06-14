package lt.rimkus.paymentService.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.MoneyDTO;
import lt.rimkus.paymentService.DTOs.PaymentDTO;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.utilities.CurrencyValidationUtils;
import lt.rimkus.paymentService.utilities.PaymentTypeValidationUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

import static lt.rimkus.paymentService.messages.OtherMessages.CREDITOR_IBAN_NOT_NULL;
import static lt.rimkus.paymentService.messages.OtherMessages.DEBTOR_IBAN_NOT_NULL;
import static lt.rimkus.paymentService.messages.OtherMessages.TYPE_NOT_NULL;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.AMOUNT_MANDATORY;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CREDITOR_IBAN_MANDATORY;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CURRENCY_MANDATORY;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CURRENCY_NOT_SUPPORTED;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.DEBTOR_IBAN_MANDATORY;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.INCORRECT_AMOUNT_VALUE;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.MONEY_MISSING;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.TYPE_MANDATORY;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.TYPE_NOT_COMPATIBLE_WITH_CURRENCY;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.UNSUPPORTED_TYPE;

@Entity
@Table(name = "payments")
public abstract class Payment implements Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String type;
    @Embedded
    private Money money;
    @Column(nullable = false)
    private String debtor_iban;
    @Column(nullable = false)
    private String creditor_iban;

    public Payment() {
    }

    @Override
    public void validateEntityCreationRequest(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        validateCommonMandatoryFields(requestDTO);
        validateTypeSpecificRequirements(requestDTO);
    }

    private void validateCommonMandatoryFields(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        validatePaymentType(requestDTO);
        validateAmount(requestDTO);
        validateCurrency(requestDTO);
        validateIBANs(requestDTO);
    }

    private void validatePaymentType(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        String paymentType = requestDTO.getType();
        if (StringUtils.isEmpty(paymentType)) {
            throw new RequestValidationException(TYPE_MANDATORY);
        }
        if (PaymentTypeValidationUtils.isPaymentTypeNotValid(paymentType)) {
            throw new RequestValidationException(UNSUPPORTED_TYPE + paymentType);
        }
    }

    private void validateAmount(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        if (requestDTO.getMoney() == null) {
            throw new RequestValidationException(MONEY_MISSING);
        }
        if (requestDTO.getMoney().getAmount() == null || !(requestDTO.getMoney().getAmount().compareTo(BigDecimal.ZERO) > 0)) {
            throw new RequestValidationException(AMOUNT_MANDATORY);
        }
        if (requestDTO.getMoney().getAmount().scale() > 2) {
            throw new RequestValidationException(INCORRECT_AMOUNT_VALUE);
        }
    }

    private void validateCurrency(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        String currency = requestDTO.getMoney().getCurrency();
        String paymentType = requestDTO.getType();
        if (StringUtils.isEmpty(currency)) {
            throw new RequestValidationException(CURRENCY_MANDATORY);
        }
        if (CurrencyValidationUtils.isCurrencyNotValid(currency)) {
            throw new RequestValidationException(CURRENCY_NOT_SUPPORTED + currency);
        }
        if (CurrencyValidationUtils.isCurrencyNotValidForPaymentType(currency, paymentType)) {
            throw new RequestValidationException(paymentType + TYPE_NOT_COMPATIBLE_WITH_CURRENCY + currency);
        }
    }

    private void validateIBANs(CreatePaymentRequestDTO requestDTO) throws RequestValidationException {
        if (StringUtils.isEmpty(requestDTO.getDebtor_iban())) {
            throw new RequestValidationException(DEBTOR_IBAN_MANDATORY);
        }
        if (StringUtils.isEmpty(requestDTO.getCreditor_iban())) {
            throw new RequestValidationException(CREDITOR_IBAN_MANDATORY);
        }
    }

    void populateCommonData(CreatePaymentRequestDTO requestDTO) {
        this.setType(requestDTO.getType());
        this.setMoney(new Money());
        this.getMoney().setCurrency(requestDTO.getMoney().getCurrency());
        this.getMoney().setAmount(requestDTO.getMoney().getAmount());
        this.setDebtor_iban(requestDTO.getDebtor_iban());
        this.setCreditor_iban(requestDTO.getCreditor_iban());
    }

    /**
     * Populates the common entity values for the purposes of creating a response DTO
     */
    void populateCommonDTOData(PaymentDTO dto) {
        dto.setId(this.getId());
        dto.setType(this.getType());
        dto.setMoney(new MoneyDTO());
        dto.getMoney().setAmount(this.getMoney().getAmount());
        dto.getMoney().setCurrency(this.getMoney().getCurrency());
        dto.setDebtor_iban(this.getDebtor_iban());
        dto.setCreditor_iban(this.getCreditor_iban());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null) {
            throw new IllegalArgumentException(TYPE_NOT_NULL);
        }
        this.type = type;
    }

    public Money getMoney() {
        return money;
    }

    public void setMoney(Money money) {
        this.money = money;
    }

    public String getDebtor_iban() {
        return debtor_iban;
    }

    public void setDebtor_iban(String debtor_iban) {
        if (debtor_iban == null) {
            throw new IllegalArgumentException(DEBTOR_IBAN_NOT_NULL);
        }
        this.debtor_iban = debtor_iban;
    }

    public String getCreditor_iban() {
        return creditor_iban;
    }

    public void setCreditor_iban(String creditor_iban) {
        if (creditor_iban == null) {
            throw new IllegalArgumentException(CREDITOR_IBAN_NOT_NULL);
        }
        this.creditor_iban = creditor_iban;
    }
}
