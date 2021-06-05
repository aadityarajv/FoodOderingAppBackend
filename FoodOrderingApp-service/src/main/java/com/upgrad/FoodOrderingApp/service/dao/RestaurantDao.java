package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class RestaurantDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * This method gets lists of all restaurants
     *
     * @param
     * @return List of RestaurantEntity
     */
    public List<RestaurantEntity> restaurantsByRating() {
        return entityManager
                .createNamedQuery("restaurantsByRating", RestaurantEntity.class)
                .getResultList();
    }

    public RestaurantEntity getRestaurantByUUID(String uuid) {
        try {
            return entityManager.createNamedQuery("restaurantByUUID", RestaurantEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * This method gets lists of all restaurants by Search string
     *
     * @param searchString
     * @return List of RestaurantEntity
     */
    public List<RestaurantEntity> restaurantsByName(final String searchString) {
        return entityManager
                .createNamedQuery("getRestaurantByName", RestaurantEntity.class)
                .setParameter("searchString", "%" + searchString + "%")
                .getResultList();
    }

    /**
     * This method gets restaurants by Category
     *
     * @param categoryUuid
     * @return List of restaurantEntity
     */
    public List<RestaurantEntity> restaurantByCategory(final String categoryUuid) {

        return entityManager
                .createNamedQuery("restaurantByCategory", RestaurantEntity.class)
                .setParameter("categoryUuid", categoryUuid)
                .getResultList();
    }

    /**
     * Fetch the restaurant based on UUID.
     *
     * @param uuid
     * @return RestaurantEntity if found in database else null.
     */
    public RestaurantEntity restaurantByUuid(String uuid) {
        try {
            return entityManager
                    .createNamedQuery("restaurantByUUID", RestaurantEntity.class)
                    .setParameter("uuid", uuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /* To update restaurant details and returns restaurant entity */

    public RestaurantEntity updateRestaurantEntity(RestaurantEntity restaurantEntity) {
        return entityManager.merge(restaurantEntity);
    }
}
