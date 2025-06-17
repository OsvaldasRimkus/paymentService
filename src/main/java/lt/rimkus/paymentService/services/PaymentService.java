package lt.rimkus.paymentService.services;

import lt.rimkus.paymentService.DTOs.CancelPaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentResponseDTO;
import lt.rimkus.paymentService.DTOs.GetNotCancelledPaymentsDTO;
import lt.rimkus.paymentService.DTOs.PaymentCancellationInfoDTO;
import lt.rimkus.paymentService.adapters.PaymentTypeValidationAdapter;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.factories.PaymentCreationFactory;
import lt.rimkus.paymentService.models.Money;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static lt.rimkus.paymentService.messages.OtherMessages.FAILED_TO_SEND_OUT_NOTIFICATION;
import static lt.rimkus.paymentService.messages.OtherMessages.FAILURE;
import static lt.rimkus.paymentService.messages.OtherMessages.SUCCESS;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CREATION_REQUEST_NULL;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.PAYMENT_DOES_NOT_EXIST;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.UNSUPPORTED_TYPE;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentCreationFactory paymentCreationFactory;
    @Autowired
    private PaymentTypeValidationAdapter paymentTypeValidationAdapter;
    @Autowired
    private PaymentCancellationService paymentCancellationService;
    @Autowired
    private NotificationProcessor notificationProcessor;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public CreatePaymentResponseDTO createPayment(CreatePaymentRequestDTO requestDTO, CreatePaymentResponseDTO responseDTO) {
        Payment newPayment = null;
        newPayment = validateAndCreatePayment(requestDTO, responseDTO, newPayment);
        if (responseDTO.getValidationErrors().isEmpty()) {
            assert newPayment != null;
            paymentRepository.save(newPayment);
            notifyServiceAndUpdatePaymentInDatabase(newPayment);
            responseDTO.setPaymentDTO(newPayment.convertToDTO());
        }
        return responseDTO;
    }

    public CancelPaymentResponseDTO cancelPayment(long id) {
        LocalDate dateOfCancellationRequest = LocalDate.now();
        LocalDateTime timeOfCancellationRequest = LocalDateTime.now();
        CancelPaymentResponseDTO responseDTO = new CancelPaymentResponseDTO();
        attemptPaymentCancellation(id, dateOfCancellationRequest, timeOfCancellationRequest, responseDTO);
        return responseDTO;
    }

    private void attemptPaymentCancellation(long id, LocalDate dateOfCancellationRequest, LocalDateTime timeOfCancellationRequest, CancelPaymentResponseDTO responseDTO) {
        try {
            Optional<Payment> payment = paymentRepository.findById(id);
            if (payment.isEmpty()) {
                throw new RequestValidationException(PAYMENT_DOES_NOT_EXIST);
            } else {
                Payment paymentToCancel = payment.get();
                paymentCancellationService.preparePaymentForCancellation(paymentToCancel, dateOfCancellationRequest, timeOfCancellationRequest);
                paymentRepository.save(paymentToCancel);
                responseDTO.setPaymentDTO(paymentToCancel.convertToDTO());
                responseDTO.setCancellationFee(new Money(paymentToCancel.getCancellationFee().getAmount(), paymentToCancel.getCancellationFee().getCurrency()));
            }
        } catch (RequestValidationException rve) {
            responseDTO.getValidationErrors().add(rve.getMessage());
        }
    }

    public List<Long> getNotCanceledPaymentIds(GetNotCancelledPaymentsDTO requestDTO) {
        BigDecimal minAmount = requestDTO.getMinAmount();
        BigDecimal maxAmount = requestDTO.getMaxAmount();
        if (!requestDTO.isFilter()) {
            minAmount = null;
            maxAmount = null;
        }
        return paymentRepository.getNotCancelledPaymentsWithinRange(minAmount, maxAmount);
    }

    public PaymentCancellationInfoDTO getPaymentCancellationDetails(Long id) {
        return paymentRepository.getPaymentCancellationDetails(id);
    }

    private Payment validateAndCreatePayment(CreatePaymentRequestDTO requestDTO, CreatePaymentResponseDTO responseDTO, Payment newPayment) {
        try {
            if (requestDTO == null) {
                throw new RequestValidationException(CREATION_REQUEST_NULL);
            }
            if (paymentTypeValidationAdapter.isPaymentTypeNotValid(requestDTO.getType())) {
                throw new RequestValidationException(UNSUPPORTED_TYPE + requestDTO.getType());
            }
            newPayment = paymentCreationFactory.createNewPayment(requestDTO);
        } catch (RequestValidationException rve) {
            responseDTO.getValidationErrors().add(rve.getMessage());
        }
        return newPayment;
    }

    protected void notifyServiceAndUpdatePaymentInDatabase(Payment newPayment) {
        try {
            CompletableFuture<String> notificationResult = notificationProcessor.notifyServiceAboutCreatedPayment(newPayment);
            notificationResult.thenAccept((result) -> {
                newPayment.setNotificationStatus(result != null && result.equals(SUCCESS) ? SUCCESS : FAILURE);
                // Updating payment with notification status in DB
                paymentRepository.save(newPayment);
            });
        } catch (RequestValidationException rve) {
            logger.warn(FAILED_TO_SEND_OUT_NOTIFICATION + "{}", newPayment.getType());
        }
    }
}
