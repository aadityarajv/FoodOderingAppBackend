package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ErrorResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.api.model.ItemListResponse;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.*;

@RestController
@CrossOrigin
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RestaurantService restaurantService;

    /**
     * This method gets top five popular items of a restaurant based on number of times it is ordered.
     *
     * @param restaurantUuid UUID for the restaurant
     * @return ItemListResponse
     *
     */
    @RequestMapping(method = RequestMethod.GET, path = "/item/restaurant/{restaurant_id}")
    public ResponseEntity<?> getPopularItemByRestaurant(@PathVariable("restaurant_id")String restaurantUuid) {

        RestaurantEntity restaurantEntity;
        try {
            restaurantEntity = restaurantService.restaurantByUuid(restaurantUuid);
        } catch (RestaurantNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }

        List<ItemEntity> itemEntities = itemService.getItemsByPopularity(restaurantEntity);

        ItemListResponse itemListResponse = new ItemListResponse();
        for (ItemEntity entity : itemEntities) {
            ItemList itemList = new ItemList();
            itemList.setId(UUID.fromString(entity.getUuid()));
            itemList.setItemName(entity.getItemName());

            itemList.setItemType(ItemList.ItemTypeEnum.fromValue(entity.getType().getValue()));
            itemList.setPrice(entity.getPrice());
            itemListResponse.add(itemList);
        }
        return new ResponseEntity<>(itemListResponse, HttpStatus.OK);



    }

}
