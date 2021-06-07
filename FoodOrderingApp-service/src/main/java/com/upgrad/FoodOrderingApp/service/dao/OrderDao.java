package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Repository
public class OrderDao {

    @PersistenceContext
    private EntityManager entityManager;


    /**
     * Fetches the orders of the customer in a sorted manner with latest order being on the top. *
     *
     * @param customerUUID customer whose orders are to be fetched. * @return list of orders made by
     *     customer
     * @return list of orders made by customer.
     */
    public List<OrderEntity> getOrdersByCustomers(String customerUUID) {
        List<OrderEntity> ordersByCustomer =
                entityManager
                        .createNamedQuery("getOrdersByCustomer", OrderEntity.class)
                        .setParameter("customerUUID", customerUUID)
                        .getResultList();
        if (ordersByCustomer != null) {
            return ordersByCustomer;
        }
        return Collections.emptyList();
    }

    public OrderEntity saveOrder(OrderEntity order) {
        entityManager.persist(order);
        return order;
    }

    /**
     * Order item that is to be persisted in the database.
     *
     * @param orderItemEntity
     * @return persisted order item.
     */
    public OrderItemEntity saveOrderItem(OrderItemEntity orderItemEntity) {
        entityManager.persist(orderItemEntity);
        return orderItemEntity;
    }

    /* Method to fetch Orders based on restaurant object */
    public List<OrderEntity> getOrdersByRestaurant(RestaurantEntity restaurantEntity) {
        try {
            return entityManager.createNamedQuery("ordersByRestaurant", OrderEntity.class).setParameter("restaurant", restaurantEntity).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /* Method to fetch items based on order object*/
    public List<OrderItemEntity> getItemsByOrder(OrderEntity orderEntity) {
        try {
            return entityManager.createNamedQuery("itemsByOrder", OrderItemEntity.class).setParameter("orderEntity", orderEntity).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
