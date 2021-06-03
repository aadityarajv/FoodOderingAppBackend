package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerAddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void createCustomerAddress(CustomerAddressEntity createdCustomerAddressEntity) {
        entityManager.persist(createdCustomerAddressEntity);
    }

    /**
     * fetches the address of a customer using givne address.
     *
     * @param address address to fetch.
     * @return CustomerAddressEntity type object.
     */
    public CustomerAddressEntity customerAddressByAddress(final AddressEntity address) {
        try {
            return entityManager
                    .createNamedQuery("customerAddressByAddress", CustomerAddressEntity.class)
                    .setParameter("address", address)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
