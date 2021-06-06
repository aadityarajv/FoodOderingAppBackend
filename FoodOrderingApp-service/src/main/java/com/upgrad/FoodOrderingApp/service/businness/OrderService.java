package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CouponDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderDao;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.*;



@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private CouponDao couponDao;


    /**
     * This method contains business logic to get coupon details by coupon name.
     *
     * @param couponName
     * @return CouponEntity
     * @throws CouponNotFoundException if coupon with that name doesn't exist in database.
     */
    public CouponEntity getCouponByCouponName(String couponName) throws CouponNotFoundException {
        if(couponName == null) {
            throw new CouponNotFoundException(CPF_004.getCode(), CPF_004.getDefaultMessage());
        }

        CouponEntity couponEntity = couponDao.getCouponByCouponName(couponName);
        if (couponEntity == null) {
            throw new CouponNotFoundException(CPF_001.getCode(), CPF_001.getDefaultMessage());
        }

        return couponEntity;
    }

    /**
     * Fetches the orders of the customer in a sorted manner with latest order being on the top.
     *
     * @param customerUUID customer whose orders are to be fetched.
     * @return list of orders made by customer
     */
    public List<OrderEntity> getOrdersByCustomers(String customerUUID) {
        return orderDao.getOrdersByCustomers(customerUUID);
    }

    /**
     * This method get coupon details by coupon id.
     *
     * @param couponUUID
     * @return
     * @throws CouponNotFoundException if coupon with that id doesn't exist in database.
     */
    public CouponEntity getCouponByUuid(String couponUUID) throws CouponNotFoundException {

        CouponEntity couponEntity = couponDao.getCouponByUuid(couponUUID);

        if (couponEntity == null) {
            throw new CouponNotFoundException(CPF_002.getCode(), CPF_002.getDefaultMessage());
        }

        return couponEntity;
    }

    /**
     * Persists the order in the database.
     *
     * @param order Order to be persisted.
     * @return Persisted order.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderEntity saveOrder(OrderEntity order) {
        try {
            return orderDao.saveOrder(order);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Persists the Order Item.
     *
     * @param orderItemEntity Order Item to be persisted.
     * @return persisted order item.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderItemEntity saveOrderItem(OrderItemEntity orderItemEntity) {
        try {
            return orderDao.saveOrderItem(orderItemEntity);
        } catch (Exception ex) {
            return null;
        }
    }
}
