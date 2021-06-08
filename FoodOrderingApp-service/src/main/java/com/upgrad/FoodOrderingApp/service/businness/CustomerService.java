package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.*;

@Service
public class CustomerService {

    @Autowired
    private CustomerDao customerDAO;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity registerCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {

        if (
                        !customerEntity.getFirstName().isEmpty() &&
                        !customerEntity.getEmail().isEmpty() &&
                        !customerEntity.getContactNumber().isEmpty() &&
                        !customerEntity.getPassword().isEmpty()
        ) {
            // Check if Contact Is Valid
            if(!isValidContactNumber(customerEntity.getContactNumber())) {
                System.out.println("!isValid: ");
                throw new SignUpRestrictedException(SGR_003.getCode(), SGR_003.getDefaultMessage());
            }

            // Check if Contact Is exists in database
            if (isPhoneNumberExist(customerEntity.getContactNumber())) {
                throw new SignUpRestrictedException(SGR_001.getCode(), SGR_001.getDefaultMessage());
            }

            // Check if Email is Valid (right format)
            if (!isValidEmail(customerEntity.getEmail())) {
                throw new SignUpRestrictedException(SGR_002.getCode(), SGR_002.getDefaultMessage());
            }

            if (!isAStrongPassword(customerEntity.getPassword()))
                throw new SignUpRestrictedException(SGR_004.getCode(), SGR_004.getDefaultMessage());


            String[] encryptedText = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
            customerEntity.setSalt(encryptedText[0]);
            customerEntity.setPassword(encryptedText[1]);
            return customerDAO.registerCustomer(customerEntity);
        } else {
            throw new SignUpRestrictedException(SGR_005.getCode(), SGR_005.getDefaultMessage());
        }

    }


    /**
     * Accepts contact number and password to verify customer
     * @return CustomerAuthEntity if authenticated else AuthenticationFailedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String contactNumber, String password) throws AuthenticationFailedException {
        CustomerEntity customerEntity = getCustomerByContactNumber(contactNumber);
        // If there is no customer then return it throws AuthenticationFailedException with
        // error code ATH-001
        if (customerEntity == null) {
            throw new AuthenticationFailedException(ATH_001.getCode(), ATH_001.getDefaultMessage());
        }
        final String encryptedPassword =
                PasswordCryptographyProvider.encrypt(password, customerEntity.getSalt());

        // if the encrypted password doesn't match with the fetched customer password throws
        // AuthenticationFailedException with code "ATH--002
        if (!encryptedPassword.equals(customerEntity.getPassword())) {
            throw new AuthenticationFailedException(ATH_002.getCode(), ATH_002.getDefaultMessage());
        }

        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
        CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
        customerAuthEntity.setUuid(UUID.randomUUID().toString());
        customerAuthEntity.setCustomer(customerEntity);
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime expiresAt = now.plusHours(8);
        customerAuthEntity.setLoginAt(now);
        customerAuthEntity.setExpiresAt(expiresAt);
        String accessToken = jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt);
        customerAuthEntity.setAccessToken(accessToken);

        customerDAO.createCustomerAuth(customerAuthEntity);
        return customerAuthEntity;
    }

    /* Method to logout customer based on access-token else would return exception that customer is not authorized without access=-token*/
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logoutCustomer(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity =  customerDAO.getCustomerAuthEntityByAceessToken(accessToken);

        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException(ATHR_001.getCode(), ATHR_001.getDefaultMessage());
        }
        // Checking if customer logout is not null
        if(customerAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException(ATHR_002.getCode(), ATHR_002.getDefaultMessage());
        }
        // If access token is expired
        if(customerAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_003.getCode(), ATHR_003.getDefaultMessage());
        }
        // If all well then set logout at now and return entity
        final ZonedDateTime now = ZonedDateTime.now();
        customerAuthEntity.setLogoutAt(now);
        customerAuthEntity = customerDAO.updateCustomerAuth(customerAuthEntity);
        return customerAuthEntity;
    }


// This returns Customer entity by getting accessToken as param
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity getCustomerEntityByAccessToken(String accessToken) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthEntity =  customerDAO.getCustomerAuthEntityByAceessToken(accessToken);

        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException(ATHR_001.getCode(), ATHR_001.getDefaultMessage());
        }
        // Checking if customer logout is not null
        if(customerAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException(ATHR_002.getCode(), ATHR_002.getDefaultMessage());
        }
        // If access token is expired
        if(customerAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_003.getCode(), ATHR_003.getDefaultMessage());
        }

        return customerAuthEntity.getCustomer();
    }

    /**
     * This method updates the customer details in database.
     *
     * @param customerEntity CustomerEntity object to update.
     * @return Updated CustomerEntity object.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(final CustomerEntity customerEntity) {
        return customerDAO.updateCustomer(customerEntity);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(String oldPassword, String newPassword, CustomerEntity customerEntity) throws UpdateCustomerException, AuthorizationFailedException {



        if(!isAStrongPassword(newPassword))
            throw new UpdateCustomerException(UCR_001.getCode(), UCR_001.getDefaultMessage());

        final String encrptedOldPass = passwordCryptographyProvider.encrypt(oldPassword, customerEntity.getSalt());

        if(!encrptedOldPass.equals(customerEntity.getPassword())) {
            throw new UpdateCustomerException(UCR_004.getCode(), UCR_004.getDefaultMessage());
        }

        final String newEncrptedPass = passwordCryptographyProvider.encrypt(newPassword, customerEntity.getSalt());
        customerEntity.setPassword(newEncrptedPass);
        customerDAO.updateCustomer(customerEntity);
        return customerEntity;
    }


    /**
     * Check's if the contact number is exists in database
     * @param contactNumber
     * @return true if number exists, else false
     *
     */
    private boolean isPhoneNumberExist(String contactNumber) {
        CustomerEntity customerEntity = getCustomerByContactNumber(contactNumber);
        if (customerEntity != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * To get customer entity by contact number
     *
     * @param contactNumber Customer's contact number
     * @return CustomerEntity with customer information
     */
    private CustomerEntity getCustomerByContactNumber(String contactNumber) {
        return customerDAO.getCustomerByContactNumber(contactNumber);
    }


    /**
     * Check if password strength meets minimum specifications
     *
     * @param password Customer's password
     * @return true if password meets minimum requirements else false
     */
    private boolean isAStrongPassword(String password) {
        Pattern p1 = Pattern.compile("^(?=.*\\d)(?=.*[A-Z])(?=.*\\W).*$");
        Matcher matcher = p1.matcher(password);
        return password.length() >= 8 && matcher.matches();
    }

    /**
     * Check's if customer's contact number is valid
     *
     * @param contactNumber Customer's contact number
     * @return true if contact number is numeric and or length 10
     */
    private boolean isValidContactNumber(String contactNumber) {
        Pattern pattern = Pattern.compile("^[1-9]+\\d{9}$");
        Matcher matcher = pattern.matcher(contactNumber);
        return matcher.matches();
    }

    /**
     * Check's if customer's email is valid
     *
     * @param email Customer's email
     * @return true is email format is correct else false
     */
    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.find();
    }

}
