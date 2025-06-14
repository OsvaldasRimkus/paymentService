package lt.rimkus.paymentService.services;

import lt.rimkus.paymentService.DTOs.CreatePaymentRequestDTO;
import lt.rimkus.paymentService.DTOs.CreatePaymentResponseDTO;
import lt.rimkus.paymentService.adapters.PaymentTypeValidationAdapter;
import lt.rimkus.paymentService.exceptions.RequestValidationException;
import lt.rimkus.paymentService.factories.PaymentCreationFactory;
import lt.rimkus.paymentService.models.Payment;
import lt.rimkus.paymentService.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static lt.rimkus.paymentService.messages.ValidationErrorMessages.CREATION_REQUEST_NULL;
import static lt.rimkus.paymentService.messages.ValidationErrorMessages.UNSUPPORTED_TYPE;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentCreationFactory paymentCreationFactory;
    @Autowired
    private PaymentTypeValidationAdapter paymentTypeValidationAdapter;


    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public CreatePaymentResponseDTO createPayment(CreatePaymentRequestDTO requestDTO, CreatePaymentResponseDTO responseDTO) {
        Payment newPayment = null;
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
        if (responseDTO.getValidationErrors().isEmpty()) {
            assert newPayment != null;
            paymentRepository.save(newPayment);
            responseDTO.setPaymentDTO(newPayment.convertToDTO());
        }
        return responseDTO;
    }
}
