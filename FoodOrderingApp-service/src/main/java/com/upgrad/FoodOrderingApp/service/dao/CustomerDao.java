package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerEntity registerCustomer(CustomerEntity customerEntity) {
        entityManager.persist(customerEntity);
        return customerEntity;
    }

    public CustomerEntity getCustomerByContactNumber(String contactNumber) {
        try {
            return entityManager.createNamedQuery("customerByContactNumber", CustomerEntity.class)
                    .setParameter("contact_number", contactNumber).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /*Method to create new cutomer authorixation from custauthentity*/
    public CustomerAuthEntity createCustomerAuth(CustomerAuthEntity customerAuthEntity) {
        entityManager.persist(customerAuthEntity);
        return customerAuthEntity;
    }

    public CustomerAuthEntity getCustomerAuthEntityByAceessToken(String accessToken) {
        try {
            return entityManager.createNamedQuery("customerAuthByToken", CustomerAuthEntity.class)
                    .setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

     // This method updates the customer details in the database.
    public CustomerEntity updateCustomer(final CustomerEntity customerEntity) {
        entityManager.merge(customerEntity);
        return customerEntity;
    }

    // This method updates the customers logout time in the database.
    public CustomerAuthEntity updateCustomerAuth(final CustomerAuthEntity updatedCustomerAuthEntity) {
        return entityManager.merge(updatedCustomerAuthEntity);
    }
}
