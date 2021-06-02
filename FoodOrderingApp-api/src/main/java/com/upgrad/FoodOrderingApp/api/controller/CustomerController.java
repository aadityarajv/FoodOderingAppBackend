package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ErrorResponse;
import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
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
    public ResponseEntity<?> signup(@Valid @RequestBody SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {

        if (
                signupCustomerRequest.getFirstName().equals("") ||
                        signupCustomerRequest.getEmailAddress().equals("") ||
                        signupCustomerRequest.getContactNumber().equals("") ||
                        signupCustomerRequest.getPassword().equals("")
        ) {
            throw new SignUpRestrictedException(SGR_005.getCode(), SGR_005.getDefaultMessage());
        }

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassword(signupCustomerRequest.getPassword());

        CustomerEntity res = null;
        try {
            res = customerService.registerCustomer(customerEntity);
        } catch (SignUpRestrictedException e) {
            ErrorResponse errorResponse = new ErrorResponse().code(e.getCode()).message(e.getErrorMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<SignupCustomerResponse>(
                new SignupCustomerResponse().id(res.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED"), HttpStatus.CREATED);

    }


    @RequestMapping(method = RequestMethod.POST, path = "/customer/login")
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") String authorization) throws AuthenticationFailedException {
        //TypeResolutionContext.Basic dXNlcm5hbWU6cGFzc3dvcmQ =
        //above is a sample encoded text where the username is "username" and password is "password" separated by a ":"
        String[] decodedArray;

        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            String decodedText = new String(decode);
            decodedArray = decodedText.split(":");
            if(decodedArray.length != 2) throw new Exception();
        } catch (Exception e) {
            throw new AuthenticationFailedException(ATH_003.getCode(), ATH_003.getDefaultMessage());
        }

        CustomerAuthEntity customerAuthEntity = customerService.authenticate(decodedArray[0], decodedArray[1]);
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

}
