package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.ATHR_005;

@RestController
@CrossOrigin
public class AddressController {
    private static final Logger log = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AddressService addressService;

    @RequestMapping(method = RequestMethod.POST, path = "/address")
    public ResponseEntity<?> saveAddress(@RequestHeader("authorization")String authorization, @Valid @RequestBody SaveAddressRequest saveAddressRequest) {
        String accessToken;
        AddressEntity addressEntity = new AddressEntity();
        CustomerEntity customerEntity;
        try {
            accessToken = authorization.split("Bearer ")[1];
            if(accessToken == null) {
                throw new AuthorizationFailedException(ATHR_005.getCode(), ATHR_005.getDefaultMessage());
            }
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATHR_005.getCode()).message(ATHR_005.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.FORBIDDEN);
        }

        try {
            customerEntity = customerService.getCustomerEntityByAccessToken(accessToken);

            if(saveAddressRequest != null) {
                addressEntity.setUuid(UUID.randomUUID().toString());
                addressEntity.setCity(saveAddressRequest.getCity());
                addressEntity.setLocality(saveAddressRequest.getLocality());
                addressEntity.setPincode(saveAddressRequest.getPincode());
                addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
                addressEntity.setActive(1);
            }

            addressEntity.setState(addressService.getStateByUuid(saveAddressRequest.getStateUuid()));

        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (AddressNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        try {
            addressEntity = addressService.saveAddress(addressEntity, customerEntity);

        } catch (SaveAddressException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<SaveAddressResponse>(new SaveAddressResponse().id(addressEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED"), HttpStatus.CREATED);
    }

    /**
     * This module retrieves all the saved addresses of a customer.
     *
     * @param authorization customer login access token in 'Bearer <access-token>' format.
     * @return ResponseEntity<AddressListResponse> type object along with HttpStatus as OK.
     */
    @RequestMapping(method = RequestMethod.GET, path = "/address/customer")
    public ResponseEntity<?> getAllAddress(@RequestHeader("authorization")String authorization) {

        String accessToken;
        List<AddressEntity> addressEntityList;
        CustomerEntity customerEntity;
        try {
            accessToken = authorization.split("Bearer ")[1];
            if(accessToken == null) {
                throw new AuthorizationFailedException(ATHR_005.getCode(), ATHR_005.getDefaultMessage());
            }
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATHR_005.getCode()).message(ATHR_005.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.FORBIDDEN);
        }

        try {
            // Getting customer by access token
            customerEntity = customerService.getCustomerEntityByAccessToken(accessToken);
            // Getting all address by customer
            addressEntityList = addressService.getAllAddress(customerEntity);
        }  catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        AddressListResponse addressListResponse = new AddressListResponse();

        if (!addressEntityList.isEmpty()) {
            for (AddressEntity addressEntity : addressEntityList) {
                AddressList addressResponseList =
                        new AddressList()
                                .id(UUID.fromString(addressEntity.getUuid()))
                                .flatBuildingName(addressEntity.getFlatBuilNo())
                                .city(addressEntity.getCity())
                                .pincode(addressEntity.getPincode())
                                .locality(addressEntity.getLocality())
                                .state(
                                        new AddressListState()
                                                .id(UUID.fromString(addressEntity.getState().getUuid()))
                                                .stateName(addressEntity.getState().getState_name()));
                addressListResponse.addAddressesItem(addressResponseList);
            }
        } else {
            List<AddressList> addresses = Collections.emptyList();
            addressListResponse.addresses(addresses);
        }

        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }

    /**
     * This module delete customer address
     *
     * @param addressId Address uuid is used to fetch the correct address.
     * @param authorization customer login access token in 'Bearer <access-token>' format.
     * @return ResponseEntity<DeleteAddResponse> with HttpStatus as OK
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/address/{address_id}")
    public ResponseEntity<?> deleteAddressByUuid(@RequestHeader("authorization")String authorization, @PathVariable("address_id")String addressId) {
        String accessToken;
        CustomerEntity customerEntity;
        AddressEntity addressEntity;
        AddressEntity deletedAddress;
        try {
            accessToken = authorization.split("Bearer ")[1];
            if(accessToken == null) {
                throw new AuthorizationFailedException(ATHR_005.getCode(), ATHR_005.getDefaultMessage());
            }
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATHR_005.getCode()).message(ATHR_005.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.FORBIDDEN);
        }

        try {
            // Getting customer by access token
            customerEntity = customerService.getCustomerEntityByAccessToken(accessToken);
            // get address by uuid and customer
            addressEntity = addressService.getAddressByUuidAndCustomer(addressId, customerEntity);
        }  catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (AddressNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        deletedAddress = addressService.deleteAddress(addressEntity);

        return new ResponseEntity<DeleteAddressResponse>(new DeleteAddressResponse().id(UUID.fromString(deletedAddress.getUuid())).status("ADDRESS DELETED SUCCESSFULLY"), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/states")
    public ResponseEntity<StatesListResponse> getAllStates() {

        final List<StateEntity> statesLists = addressService.getAllStates();

        final StatesListResponse statesListResponse = new StatesListResponse();
        for (StateEntity statesEntity : statesLists) {
            StatesList states =
                    new StatesList()
                            .id(UUID.fromString(statesEntity.getUuid()))
                            .stateName(statesEntity.getState_name());
            statesListResponse.addStatesItem(states);
        }
        return new ResponseEntity<StatesListResponse>(statesListResponse, HttpStatus.OK);
    }

}
