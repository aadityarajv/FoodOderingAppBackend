package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.PaymentListResponse;
import com.upgrad.FoodOrderingApp.api.model.PaymentResponse;
import com.upgrad.FoodOrderingApp.service.businness.PaymentService;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * This API endpoint gets all the payment methods
     *
     * @return Payment methods
     */
    @RequestMapping(method = RequestMethod.GET, path = "/payment")
    public ResponseEntity<?> getAllPaymentMethods() {

        List<PaymentEntity> paymentMathods = paymentService.getAllPaymentMethods();
        PaymentListResponse paymentListResponse = new PaymentListResponse();
        for (PaymentEntity paymentMethod : paymentMathods) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setId(UUID.fromString(paymentMethod.getUuid()));
            paymentResponse.setPaymentName(paymentMethod.getPaymentName());
            paymentListResponse.addPaymentMethodsItem(paymentResponse);
        }

        return new ResponseEntity<>(paymentListResponse, HttpStatus.OK);
    }

}
