package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.ItemDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.ItemNotFoundException;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.INF_003;

@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    private CategoryDao categoryDao;

    /**
     * This method gets Items for a given category in a restaurant
     *
     * @param resturantUuid Restaurant whose items are to be queried, categoryUuid Category to be
     *     queried.
     * @return List of ItemEntity
     */
    public List<ItemEntity> getItemsByCategoryAndRestaurant(String resturantUuid, String categoryUUID) {
        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(resturantUuid);
        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(categoryUUID);
        List<ItemEntity> restaurantItemEntityList = new ArrayList<ItemEntity>();

        for (ItemEntity restaurantItemEntity : restaurantEntity.getItems()) {
            for (ItemEntity categoryItemEntity : categoryEntity.getItems()) {
                if (restaurantItemEntity.getUuid().equals(categoryItemEntity.getUuid())) {
                    restaurantItemEntityList.add(restaurantItemEntity);
                }
            }
        }
        restaurantItemEntityList.sort(Comparator.comparing(ItemEntity::getItemName));

        return restaurantItemEntityList;
    }


    public ItemEntity getItemByUuid(String itemUUID) throws ItemNotFoundException {
        ItemEntity item = itemDao.getItemByUuid(itemUUID);
        if (item == null) {
            throw new ItemNotFoundException(INF_003.getCode(), INF_003.getDefaultMessage());
        }
        return item;
    }

    /**
     * This method gets top five popular items of a restaurant.
     *
     * @param restaurantEntity Restaurant whose top five items are to be queried.
     * @return top five items
     */
    public List<ItemEntity> getItemsByPopularity(RestaurantEntity restaurantEntity) {
        List<ItemEntity> itemEntityList = new ArrayList<>();
        for (OrderEntity orderEntity : orderDao.getOrdersByRestaurant(restaurantEntity)) {
            for (OrderItemEntity orderItemEntity : orderDao.getItemsByOrder(orderEntity)) {
                itemEntityList.add(orderItemEntity.getItem());
            }
        }

        // count all with map
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (ItemEntity itemEntity : itemEntityList) {
            Integer count = map.get(itemEntity.getUuid());
            map.put(itemEntity.getUuid(), (count == null) ? 1 : count + 1);
        }

        // sort map
        Map<String, Integer> treeMap = new TreeMap<String, Integer>(map);
        List<ItemEntity> sortedItemEntityList = new ArrayList<ItemEntity>();
        for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
            sortedItemEntityList.add(itemDao.getItemByUuid(entry.getKey()));
        }
        Collections.reverse(sortedItemEntityList);

        return sortedItemEntityList;
    }
}
