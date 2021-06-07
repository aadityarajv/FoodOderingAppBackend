package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.PaymentDao;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.exception.PaymentMethodNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.PNF_002;

@Service
public class PaymentService {

    @Autowired
    private PaymentDao paymentDao;


    /**
     * This method gets the payment record from database based on the UUID.
     *
     * @param paymentUUID UUID of the payment that is to be fetched
     * @return
     */
    public PaymentEntity getPaymentByUuid(String paymentUUID) throws PaymentMethodNotFoundException {
        PaymentEntity payment = paymentDao.getPaymentByUuid(paymentUUID);
        if (payment == null) {
            throw new PaymentMethodNotFoundException(PNF_002.getCode(), PNF_002.getDefaultMessage());
        }
        return payment;
    }

    /**
     * This method gets all the payment methods
     *
     * @return
     */
    public List<PaymentEntity> getAllPaymentMethods() {
        return paymentDao.getAllPaymentMethods();
    }
}
