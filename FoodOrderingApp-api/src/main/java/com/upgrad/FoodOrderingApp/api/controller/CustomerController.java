package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.*;

@RestController
@CrossOrigin
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    /**
     * Sign up endpoint
     * @param signupCustomerRequest contains all required attributes for sign up customer to the portal
     * @return customerResponse along with CREATED httpStatus
     * @throws SignUpRestrictedException if customer enters wrong or duplicate data while registration
     */

    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupCustomerRequest signupCustomerRequest) {

        CustomerEntity customerEntity = new CustomerEntity();

        try {

            if(signupCustomerRequest.getFirstName().isEmpty() || signupCustomerRequest.getContactNumber().isEmpty()
                    || signupCustomerRequest.getEmailAddress().isEmpty() || signupCustomerRequest.getPassword().isEmpty()
            ) {
                throw new SignUpRestrictedException(SGR_005.getCode(), SGR_005.getDefaultMessage());
            }
            customerEntity.setUuid(UUID.randomUUID().toString());
            customerEntity.setFirstName(signupCustomerRequest.getFirstName());
            customerEntity.setLastName(signupCustomerRequest.getLastName());
            customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
            customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
            customerEntity.setPassword(signupCustomerRequest.getPassword());

            customerEntity = customerService.registerCustomer(customerEntity);


        } catch (SignUpRestrictedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }


        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(customerEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);

    }

    /**
     * Login method accepts Basic username:password in Base64 decoded format
     * @param authorization - basic
     * @return after successful authentication this return access-token
     * @throws AuthenticationFailedException
     */

    @RequestMapping(method = RequestMethod.POST, path = "/customer/login")
    public ResponseEntity<?> login(@RequestHeader("authorization") String authorization)  {
        //TypeResolutionContext.Basic dXNlcm5hbWU6cGFzc3dvcmQ =
        //above is a sample encoded text where the username is "username" and password is "password" separated by a ":"
        String[] decodedArray;
        CustomerAuthEntity customerAuthEntity = null;
        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            String decodedText = new String(decode);
            decodedArray = decodedText.split(":");
            if(decodedArray.length != 2)
            {
                throw new AuthenticationFailedException(ATH_004.getCode(), ATH_004.getDefaultMessage());
            }
        } catch (AuthenticationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATH_004.getCode()).message(ATH_004.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        }

        try {
            customerAuthEntity = customerService.authenticate(decodedArray[0], decodedArray[1]);
        } catch (AuthenticationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(customerAuthEntity.getCustomer().getUuid());
        loginResponse.setFirstName(customerAuthEntity.getCustomer().getFirstName());
        loginResponse.setLastName(customerAuthEntity.getCustomer().getLastName());
        loginResponse.setContactNumber(customerAuthEntity.getCustomer().getContactNumber());
        loginResponse.setEmailAddress(customerAuthEntity.getCustomer().getEmail());
        loginResponse.setMessage("LOGGED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthEntity.getAccessToken());
        List<String> header = new ArrayList<>();
        header.add("access-token");
        headers.setAccessControlExposeHeaders(header);

        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/customer/logout")
    public ResponseEntity<?> customerLogout(@RequestHeader("authorization")String authorization) {
        String accessToken;
        CustomerAuthEntity customerAuthEntity;
        try {
            accessToken = authorization.split("Bearer ")[1];
            if(accessToken == null) {
                throw new AuthorizationFailedException(ATHR_005.getCode(), ATHR_005.getDefaultMessage());
            }
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATHR_005.getCode()).message(ATHR_005.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.FORBIDDEN);
        }
        try {
            customerAuthEntity = customerService.logoutCustomer(accessToken);

        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        LogoutResponse logoutResponse = new LogoutResponse().id(customerAuthEntity.getCustomer().getUuid()).message("LOGGED OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/customer")
    public ResponseEntity<?> updateCustomer(@RequestHeader("authorization")String authorization, @Valid @RequestBody UpdateCustomerRequest updateCustomerRequest) {
        String accessToken;
        CustomerEntity customerEntity;
        if(updateCustomerRequest.getFirstName().equals("")) {
            ErrorResponse errorResponse = new ErrorResponse().code(UCR_002.getCode()).message(UCR_002.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        }
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
            return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        }



        try {
            customerEntity = customerService.getCustomerEntityByAccessToken(accessToken);
            // Updating customer fields
            customerEntity.setFirstName(updateCustomerRequest.getFirstName());
            if(!updateCustomerRequest.getLastName().equals("")) {
                customerEntity.setLastName(updateCustomerRequest.getLastName());
            }
            customerEntity = customerService.updateCustomer(customerEntity);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<UpdateCustomerResponse>(new UpdateCustomerResponse()
        .id(customerEntity.getUuid()).firstName(customerEntity.getFirstName()).lastName(customerEntity.getLastName())
                .status("CUSTOMER DETAILS UPDATED SUCCESSFULLY"), HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.PUT, path = "/customer/password")
    public ResponseEntity<?> updateCustomerPassword(@RequestHeader("authorization")String authorization, @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {



        String accessToken;
        CustomerEntity customerEntity;
        try {

            // Check if provided password are not null
            if(updatePasswordRequest.getOldPassword().length() == 0 || updatePasswordRequest.getNewPassword().length() == 0) {
                throw new UpdateCustomerException(UCR_003.getCode(), UCR_003.getDefaultMessage());
            }

            accessToken = authorization.split("Bearer ")[1];
            if(accessToken == null) {
                throw new AuthorizationFailedException(ATHR_005.getCode(), ATHR_005.getDefaultMessage());
            }
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(ATHR_005.getCode()).message(ATHR_005.getDefaultMessage());
            return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        } catch (UpdateCustomerException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        try {
            customerEntity = customerService.getCustomerEntityByAccessToken(accessToken);

            customerEntity = customerService.updateCustomerPassword(updatePasswordRequest.getOldPassword(), updatePasswordRequest.getNewPassword(), customerEntity);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (UpdateCustomerException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<UpdatePasswordResponse>(new UpdatePasswordResponse()
                .id(customerEntity.getUuid())
                .status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY"), HttpStatus.OK);
    }

}
