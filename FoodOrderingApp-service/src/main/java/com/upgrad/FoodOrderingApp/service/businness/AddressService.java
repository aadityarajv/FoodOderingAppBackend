package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.common.UnexpectedException;
import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.nimbus.State;

import java.util.ArrayList;
import java.util.List;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.*;


@Service
public class AddressService {

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private StateDao stateDao;

    /**
     * This method implements the logic for 'saving the address' endpoint.
     *
     * @param addressEntity new address will be created from given AddressEntity object.
     * @param customerEntity saves the address of the given customer.
     * @return AddressEntity object.
     * @throws SaveAddressException exception if any of the validation fails on customer details.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(
            final AddressEntity addressEntity, final CustomerEntity customerEntity)
            throws SaveAddressException {
        if (addressEntity.getActive() != null
                && addressEntity.getLocality() != null
                && !addressEntity.getLocality().isEmpty()
                && addressEntity.getCity() != null
                && !addressEntity.getCity().isEmpty()
                && addressEntity.getFlatBuilNo() != null
                && !addressEntity.getFlatBuilNo().isEmpty()
                && addressEntity.getPincode() != null
                && !addressEntity.getPincode().isEmpty()
                && addressEntity.getState() != null) {
            if (!isValidPinCode(addressEntity.getPincode())) {
                throw new SaveAddressException(SAR_002.getCode(), SAR_002.getDefaultMessage());
            }

            AddressEntity createdCustomerAddress = addressDao.createCustomerAddress(addressEntity);

            CustomerAddressEntity createdCustomerAddressEntity = new CustomerAddressEntity();
            createdCustomerAddressEntity.setCustomer(customerEntity);
            createdCustomerAddressEntity.setAddress(createdCustomerAddress);
            customerAddressDao.createCustomerAddress(createdCustomerAddressEntity);
            return createdCustomerAddress;
        } else {
            throw new SaveAddressException(SAR_001.getCode(), SAR_001.getDefaultMessage());
        }
    }

    /**
     * Returns all the addresses of a given customer.
     *
     * @param customerEntity Customer whose addresses are to be returned.
     * @return List<AddressEntity> object.
     */
    public List<AddressEntity> getAllAddress(final CustomerEntity customerEntity) {
        List<AddressEntity> addressEntityList = new ArrayList<>();
        List<CustomerAddressEntity> customerAddressEntityList =
                addressDao.customerAddressByCustomer(customerEntity);
        if (customerAddressEntityList != null || !customerAddressEntityList.isEmpty()) {
            customerAddressEntityList.forEach(
                    customerAddressEntity -> addressEntityList.add(customerAddressEntity.getAddress()));
        }
        return addressEntityList;
    }

    /**
     * This method for getting the Address using address uuid.
     *
     * @param addressId Address UUID.
     * @param customerEntity Customer whose addresses has to be fetched.
     * @return AddressEntity object.
     * @throws AddressNotFoundException If any validation on address fails.
     * @throws AuthorizationFailedException If any validation on customer fails.
     */
    public AddressEntity getAddressByUuidAndCustomer(String addressId, CustomerEntity customerEntity)
            throws AddressNotFoundException, AuthorizationFailedException {
        try { // Throw error if addressId is null
            if (addressId == null) {
                throw new AddressNotFoundException(ANF_005.getCode(), ANF_005.getDefaultMessage());
            }
            AddressEntity address = addressDao.getAddressByAddressId(addressId);
            if (address == null) { // Throw error if address not found matching addressId
                throw new AddressNotFoundException(ANF_003.getCode(), ANF_003.getDefaultMessage());
            }
            CustomerAddressEntity customerAddress =
                    customerAddressDao.customerAddressByAddress(address);
            if (customerAddress == null) { // Throw error if address doesn't belong to logged in customer
                throw new AuthorizationFailedException(ATHR_004.getCode(), ATHR_004.getDefaultMessage());
            }
            return address;
        } catch (NullPointerException npe) { // Throw error if unexpected error
            throw new UnexpectedException(GEN_001, npe);
        }
    }

    /**
     * Deletes given address from database
     *
     * @param addressEntity Address to delete.
     * @return AddressEntity type object.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity deleteAddress(final AddressEntity addressEntity) {
        return addressDao.deleteAddress(addressEntity);
    }



    /**
     * Returns state for a given UUID
     *
     * @param stateUuid UUID of the state entity
     * @return StateEntity object.
     * @throws AddressNotFoundException If given uuid does not exist in database.
     */
    public StateEntity getStateByUuid(String stateUuid) throws AddressNotFoundException {
        StateEntity stateEntity = stateDao.getStateByUuid(stateUuid);
        if(stateEntity == null) {
            throw new AddressNotFoundException(ANF_002.getCode(), ANF_002.getDefaultMessage());
        }
        return stateEntity;
    }


    /**
     * This method implements the logic to get All the States from database.
     *
     * @return List<StateEntity> object.
     */
    public List<StateEntity> getAllStates() {
        return stateDao.getAllStates();
    }


    private boolean isValidPinCode(String pincode) {
        if (pincode.length() != 6) {
            return false;
        }
        for (int i = 0; i < pincode.length(); i++) {
            if (!Character.isDigit(pincode.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
