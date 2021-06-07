package com.upgrad.FoodOrderingApp.api.controller;

import aj.org.objectweb.asm.TypePath;
import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.ATHR_005;

@RestController
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AddressService addressService;


    @RequestMapping(method = RequestMethod.GET, path = "/order/coupon/{coupon_name}")
    public ResponseEntity<?> getCouponByCouponName(@RequestHeader("authorization")String authorization,
                                                   @PathVariable("coupon_name")String couponName) {
        String accessToken;
        CustomerEntity customerEntity;
        CouponEntity couponEntity;
        try {
            accessToken = authorization.split("Bearer ")[1];
            if(accessToken == null) {
                throw new AuthorizationFailedException(ATHR_005.getCode(), ATHR_005.getDefaultMessage());
            }
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATHR_005.getCode()).message(ATHR_005.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        }

        try {
            customerEntity = customerService.getCustomerEntityByAccessToken(accessToken);
            // Get coupon by coupon name
            couponEntity = orderService.getCouponByCouponName(couponName);

        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (CouponNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse()
                .id(UUID.fromString(couponEntity.getUuid()))
                .couponName(couponEntity.getCouponName())
                .percent(couponEntity.getPercent());

        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse, HttpStatus.OK);
    }

    /**
     * Fetch the orders of the customer.
     *
     * @param authorization Bearer <access-token>
     * @return CustomerOrderResponse
     *
     */
    @RequestMapping(method = RequestMethod.GET, path = "/order")
    public ResponseEntity<?> getCustomerOrders(@RequestHeader("authorization") final String authorization) {

        String accessToken;
        CustomerEntity customerEntity;
        List<OrderEntity> customersOrder = new ArrayList<>();
        try {
            accessToken = authorization.split("Bearer ")[1];
            if(accessToken == null) {
                throw new AuthorizationFailedException(ATHR_005.getCode(), ATHR_005.getDefaultMessage());
            }
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATHR_005.getCode()).message(ATHR_005.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        }

        try {
            customerEntity = customerService.getCustomerEntityByAccessToken(accessToken);
            // Get all the orders of the customer.
            customersOrder = orderService.getOrdersByCustomers(customerEntity.getUuid());

        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }
        List<OrderList> orders = new ArrayList<>();
        for (OrderEntity orderEntity : customersOrder) {
            OrderList order = new OrderList();
            order.setId(UUID.fromString(orderEntity.getUuid()));
            order.setDate(orderEntity.getDate().toString());
            order.setBill(new BigDecimal(orderEntity.getBill()));
            order.setDiscount(new BigDecimal(orderEntity.getDiscount()));
            order.setCustomer(getOrderListCustomer(orderEntity.getCustomer()));
            order.setCoupon(getOrderListCoupon(orderEntity.getCoupon()));
            order.setAddress(getOrderListAddress(orderEntity.getAddress()));
            order.setPayment(getOrderListPayment(orderEntity.getPayment()));
            List<OrderItemEntity> orderItems = orderEntity.getOrderItems();
            order.setItemQuantities(getItemQuantityResponseList(orderItems));
            orders.add(order);
        }

        CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse();
        customerOrderResponse.setOrders(orders);

        return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse, HttpStatus.OK);

    }


    /**
     * To save the customer order if it is valid.
     *
     * @param authorization Bearer <access-token>
     * @param saveOrderRequest Contains the order details.
     * @return SaveOrderResponse
     *
     */
    @RequestMapping(method = RequestMethod.POST, path = "/order")
    public ResponseEntity<?> createOrder(@RequestHeader("authorization") final String authorization,
                                         @Valid @RequestBody SaveOrderRequest saveOrderRequest) {

        String accessToken;
        CustomerEntity customerEntity;
        CouponEntity couponEntity;
        PaymentEntity paymentEntity;
        AddressEntity addressEntity;
        RestaurantEntity restaurantEntity;
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        try {
            accessToken = authorization.split("Bearer ")[1];
            if(accessToken == null) {
                throw new AuthorizationFailedException(ATHR_005.getCode(), ATHR_005.getDefaultMessage());
            }
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATHR_005.getCode()).message(ATHR_005.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.FORBIDDEN);
        }

        try {
            customerEntity = customerService.getCustomerEntityByAccessToken(accessToken);
            // Get coupon by coupon uuid
            couponEntity = orderService.getCouponByUuid(saveOrderRequest.getCouponId().toString());
            // Get payment method by uuid
            paymentEntity = paymentService.getPaymentByUuid(saveOrderRequest.getPaymentId().toString());
            // Get address by Uuid and customer
            addressEntity = addressService.getAddressByUuidAndCustomer(saveOrderRequest.getAddressId(), customerEntity);
            // Get restaurant by uuid
            restaurantEntity = restaurantService.restaurantByUuid(saveOrderRequest.getRestaurantId().toString());

            List<ItemQuantity> itemQuantities = saveOrderRequest.getItemQuantities();
            for (ItemQuantity itemQuantity : itemQuantities) {
                ItemEntity itemEntity = itemService.getItemByUuid(itemQuantity.getItemId().toString());
                OrderItemEntity orderItem = new OrderItemEntity();
                orderItem.setItem(itemEntity);
                orderItem.setPrice(itemQuantity.getPrice());
                orderItem.setQuantity(itemQuantity.getQuantity());

                orderItemEntities.add(orderItem);
            }

        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (CouponNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (PaymentMethodNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (AddressNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (RestaurantNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (ItemNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }


        OrderEntity order = new OrderEntity();
        order.setUuid(UUID.randomUUID().toString());
        order.setBill(saveOrderRequest.getBill().doubleValue());
        order.setDiscount(saveOrderRequest.getDiscount().doubleValue());
        order.setDate(ZonedDateTime.now());
        order.setAddress(addressEntity);
        order.setCoupon(couponEntity);
        order.setPayment(paymentEntity);
        order.setRestaurant(restaurantEntity);
        order.setCustomer(customerEntity);
        order.setOrderItems(Collections.emptyList());

        order = orderService.saveOrder(order);

        if (order != null) {
            for (OrderItemEntity orderItemEntity : orderItemEntities) {
                orderItemEntity.setOrder(order);
                orderService.saveOrderItem(orderItemEntity);
            }
        }

        SaveOrderResponse saveOrderResponse = new SaveOrderResponse().id(order.getUuid().toString()).status("ORDER SUCCESSFULLY PLACED");

        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);

    }







    private List<ItemQuantityResponse> getItemQuantityResponseList(List<OrderItemEntity> orderItems) {

        List<ItemQuantityResponse> list = new ArrayList<>();

        for (OrderItemEntity orderItemEntity : orderItems) {

            ItemQuantityResponseItem itemQuantityResponseItem = new ItemQuantityResponseItem()
                    .id(UUID.fromString(orderItemEntity.getItem().getUuid()))
                    .itemName(orderItemEntity.getItem().getItemName())
                    .itemPrice(orderItemEntity.getItem().getPrice())
                    .type(ItemQuantityResponseItem.TypeEnum.fromValue(orderItemEntity.getItem().getType().getValue()));

            ItemQuantityResponse itemQuantityResponse = new ItemQuantityResponse()
                    .item(itemQuantityResponseItem)
                    .quantity(orderItemEntity.getQuantity())
                    .price(orderItemEntity.getPrice());

            list.add(itemQuantityResponse);
        }

        return list;
    }

    private OrderListPayment getOrderListPayment(PaymentEntity payment) {
        return new OrderListPayment()
                .id(UUID.fromString(payment.getUuid()))
                .paymentName(payment.getPaymentName());
    }

    private OrderListAddress getOrderListAddress(AddressEntity address) {

        return new OrderListAddress()
                .id(UUID.fromString(address.getUuid()))
                .flatBuildingName(address.getFlatBuilNo())
                .locality(address.getLocality())
                .city(address.getCity())
                .pincode(address.getPincode())
                .state(orderListAddressState(address));
    }

    private OrderListAddressState orderListAddressState(AddressEntity address) {
        return new OrderListAddressState()
                .id(UUID.fromString(address.getState().getUuid()))
                .stateName(address.getState().getState_name());
    }


    private OrderListCoupon getOrderListCoupon(CouponEntity coupon) {
        return new OrderListCoupon()
                .id(UUID.fromString(coupon.getUuid()))
                .couponName(coupon.getCouponName())
                .percent(coupon.getPercent());
    }

    private OrderListCustomer getOrderListCustomer(CustomerEntity customer) {

        return new OrderListCustomer()
                .id(UUID.fromString(customer.getUuid()))
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .emailAddress(customer.getEmail())
                .contactNumber(customer.getContactNumber());

    }

}
