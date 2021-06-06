package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.*;

@Service
public class CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private RestaurantDao restaurantDao;

    /* Method to get the list of categories of restaurant based on restaurants UUID */
    public List<CategoryEntity> getCategoriesByRestaurant(String restaurantUuid) {
        List<CategoryEntity> categoryEntities = categoryDao.getCategoriesByRestaurant(restaurantUuid);

        return categoryEntities;
    }

    /*Get category for given category uuid
     *
     * @param uuid of category
     * @return CategoryEntity for given uuid
     * */
    public CategoryEntity getCategoryByUuid(String categoryUuid) throws CategoryNotFoundException {
        if (categoryUuid.equals("")) {
            throw new CategoryNotFoundException(CNF_001.getCode(), CNF_001.getDefaultMessage());
        }

        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(categoryUuid);

        if (categoryEntity == null) {
            throw new CategoryNotFoundException(CNF_002.getCode(), CNF_002.getDefaultMessage());
        }

        return categoryEntity;
    }

    /*Get all categories
     *
     *
     * @return List of CategoryEntity
     * */
    public List<CategoryEntity> getAllCategoriesOrderedByName() {
        List<CategoryEntity> categoryEntities = categoryDao.getAllCategoriesOrderedByName();
        return categoryEntities;
    }
}
