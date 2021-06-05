package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.*;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantDao restaurantDao;

    public List<RestaurantEntity> restaurantByRating() {
        return restaurantDao.restaurantsByRating();
    }

    /**
     * Gets restaurants in DB based on search string.
     *
     * @return List of RestaurantEntity
     */
    public List<RestaurantEntity> restaurantsByName(final String search)
            throws RestaurantNotFoundException {
        if (search == null || search.isEmpty()) {
            throw new RestaurantNotFoundException(RNF_003.getCode(), RNF_003.getDefaultMessage());
        }

        List<RestaurantEntity> relevantRestaurantEntities = restaurantDao.restaurantsByName(search);

        return relevantRestaurantEntities;
    }

    /**
     * Gets all the restaurants in DB based on Category Uuid
     *
     * @return List of RestaurantEntity
     */
    public List<RestaurantEntity> restaurantByCategory(final String categoryUuid)
            throws CategoryNotFoundException {
        if (categoryUuid == null) {
            throw new CategoryNotFoundException(CNF_001.getCode(), CNF_001.getDefaultMessage());
        }

        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantByCategory(categoryUuid);
        if (restaurantEntities == null) {
            throw new CategoryNotFoundException(CNF_002.getCode(), CNF_002.getDefaultMessage());
        }

        return restaurantEntities;
    }

    /**
     * This method gets the restaurant details.
     *
     * @param uuid UUID of the restaurant.
     * @return
     * @throws RestaurantNotFoundException if restaurant with UUID doesn't exist in the database.
     */
    public RestaurantEntity restaurantByUuid(String uuid) throws RestaurantNotFoundException {
        RestaurantEntity restaurant = restaurantDao.restaurantByUuid(uuid);
        if (restaurant == null) {
            throw new RestaurantNotFoundException(RNF_001.getCode(), RNF_001.getDefaultMessage());
        }
        return restaurant;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurantEntity, Double customerRating) throws InvalidRatingException {
        if(customerRating < 1 || customerRating > 5) {
            throw new InvalidRatingException(IRE_001.getCode(), IRE_001.getDefaultMessage());
        }

        // calculate new average rating.
        Double newAverageRating =
                ((restaurantEntity.getCustomerRating()) * ((double) restaurantEntity.getNumberCustomersRated())
                        + customerRating)
                        / ((double) restaurantEntity.getNumberCustomersRated() + 1);

        restaurantEntity.setCustomerRating(newAverageRating);
        // update the number of customers who gave rating
        restaurantEntity.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated() + 1);

        return restaurantDao.updateRestaurantEntity(restaurantEntity);
    }
}
