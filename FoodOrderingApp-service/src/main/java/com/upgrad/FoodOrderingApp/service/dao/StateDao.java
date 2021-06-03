package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class StateDao {

    @PersistenceContext
    private EntityManager entityManager;

    // Get State By uuid
    public StateEntity getStateByUuid(String stateUuid) {
        try {
            return entityManager.createNamedQuery("getStateByUuid", StateEntity.class)
                    .setParameter("stateUuid", stateUuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * This method fetch all states from database.
     *
     * @return List<StateEntity> object.
     */
    public List<StateEntity> getAllStates() {
        return entityManager.createNamedQuery("getAllStates", StateEntity.class).getResultList();
    }

}
