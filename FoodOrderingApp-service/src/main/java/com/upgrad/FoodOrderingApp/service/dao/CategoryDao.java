package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CategoryDao {

    @PersistenceContext
    private EntityManager entityManager;

    /* This method is to list all the categories available */
    public List<CategoryEntity> getAllCategories() {
        try {
            return entityManager.createNamedQuery("getAllCategoriesOrderedByName", CategoryEntity.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * This method fetches all CategoryEntity from db for given restaurant
     *
     * @param restaurantUuid
     * @return List of categoryEntity
     */
    public List<CategoryEntity> getCategoriesByRestaurant(final String restaurantUuid) {
        try {
            return entityManager
                    .createNamedQuery("getCategoriesByRestaurant", CategoryEntity.class)
                    .setParameter("restaurantUuid", restaurantUuid)
                    .getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public CategoryEntity getCategoryByUuid(String uuid) {
        try {
            return entityManager.createNamedQuery("categoryByUuid", CategoryEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }




}
