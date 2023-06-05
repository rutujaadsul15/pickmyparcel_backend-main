package com.company.pickmyparcel.service;


import com.company.pickmyparcel.model.FutureTrip;
import com.company.pickmyparcel.model.User;
import com.company.pickmyparcel.model.UserRole;
import com.company.pickmyparcel.model.requests.FutureTripRequest;
import com.company.pickmyparcel.repository.DealRepository;
import com.company.pickmyparcel.repository.FutureTripRepository;
import com.company.pickmyparcel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private FutureTripRepository futureTripRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private DealServiceImpl dealService;

    @Override
    public User registerUser(User user) {
        Optional<User> dbUser = userRepository.findById(user.getContactNo());
        if (dbUser.isPresent()) {
            throw new RuntimeException("User already registered with this contact number");
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(UserRole.USER);
            return userRepository.save(user);
        }
    }

    @Override
    public User getUserByContactNumber(String contactNo) {
        Optional<User> dbUser = userRepository.findById(contactNo);
        if (dbUser.isPresent()) {
            return dbUser.get();
        } else {
            throw new RuntimeException("User does not exist with contact number : " + contactNo);
        }
    }

    @Override
        public Boolean addFutureTrips(FutureTripRequest futureTripRequest, String userContactNo) {
        FutureTrip futureTrip = new FutureTrip();

        futureTrip.setPickupPlaceId(futureTripRequest.getPickUpLocation().getPlaceId());
        futureTrip.setPickupAddressLine(futureTripRequest.getPickUpLocation().getAddressLine());
        futureTrip.setPickupLatitude(futureTripRequest.getPickUpLocation().getLatitude());
        futureTrip.setPickupLongitude(futureTripRequest.getPickUpLocation().getLongitude());
        futureTrip.setPickupCity(futureTripRequest.getPickUpLocation().getCity());
        futureTrip.setPickupState(futureTripRequest.getPickUpLocation().getState());
        futureTrip.setPickupPinCode(futureTripRequest.getPickUpLocation().getPinCode());
        //set drop location details
        futureTrip.setDropPlaceId(futureTripRequest.getDropLocation().getPlaceId());
        futureTrip.setDropAddressLine(futureTripRequest.getDropLocation().getAddressLine());
        futureTrip.setDropLatitude(futureTripRequest.getDropLocation().getLatitude());
        futureTrip.setDropLongitude(futureTripRequest.getDropLocation().getLongitude());
        futureTrip.setDropCity(futureTripRequest.getDropLocation().getCity());
        futureTrip.setDropState(futureTripRequest.getDropLocation().getState());
        futureTrip.setDropPinCode(futureTripRequest.getDropLocation().getPinCode());
        futureTrip.setDateAndTime(futureTripRequest.getDateAndTime());
        futureTrip.setCarrierContactNo(futureTripRequest.getCarrierContactNo());
        futureTrip.setCarrierFirstName(futureTripRequest.getCarrierFirstName());
        futureTrip.setCarrierLastName(futureTripRequest.getCarrierLastName());

        LinkedHashSet<String> onRoutePinCodes  = dealService.findOnRouteVillages(futureTripRequest.getPickUpLocation().getLatitude(), futureTripRequest.getPickUpLocation().getLongitude(),
                futureTripRequest.getDropLocation().getLatitude(), futureTripRequest.getDropLocation().getLongitude());
        futureTrip.setOnRoutePinCodes(String.join(",", onRoutePinCodes));
        User user = getUserByContactNumber(userContactNo);

        if (user != null) {
            List<FutureTrip> futureTripsList = user.getFutureTripsList();
            futureTripsList.add(futureTrip);
            user.setFutureTripsList(futureTripsList);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public Boolean deleteFutureTrip(Integer futureTripId, String userContactNo) {
        Optional<User> dbUser = userRepository.findById(userContactNo);
        if (dbUser.isPresent()) {
            User user = dbUser.get();
            List<FutureTrip> futureTripsList = user.getFutureTripsList();
            if (futureTripsList != null && futureTripsList.size() > 0) {
                futureTripsList.removeIf(trip -> Objects.equals(trip.getFutureTripId(), futureTripId));
                user.setFutureTripsList(futureTripsList);
                userRepository.save(user);
                return true;
            } else {
                return false;
            }
        } else {
            throw new RuntimeException("User does not exist with contact number : " + userContactNo);
        }
    }

    @Override
    public List<FutureTrip> getFutureTripByPinCodes(String pickupPinCode, String dropPinCode) {
        return futureTripRepository.findByOnRoutePinCodesContainingAndOnRoutePinCodesContaining(pickupPinCode, dropPinCode);
    }
    @Override
    public FutureTrip getFutureTripById(Integer futureTripId) {
        Optional<FutureTrip> futureTripRepositoryById = futureTripRepository.findById(futureTripId);
        if (futureTripRepositoryById.isEmpty()) {
            System.out.println("No future trip exist with given id");
        } else {
            System.out.println("Future trip is :  ");
        }
        return futureTripRepositoryById.get();
    }


/*    public List<Deal> searchFutureDeals(FutureTripRequest futureTripRequest) {
        List<Deal> dealList = dealRepository.findAll();
        LinkedHashSet<String> onRoutePinCodes = findOnRouteVillages(futureTripRequest);
        boolean isPickupPinCodeExist = onRoutePinCodes.contains(futureTripRequest.getPickUpLocation().getPinCode());
        boolean isDropPinCodeExist = onRoutePinCodes.contains(futureTripRequest.getDropLocation().getPinCode());
        if (isPickupPinCodeExist && isDropPinCodeExist) {
            return dealList.stream()
                    .filter(deal -> deal.getPickUpLocation().getPinCode().equals(futureTripRequest.getPickUpLocation().getPinCode()))
                    .filter(deal -> deal.getDropLocation().getPinCode().equals(futureTripRequest.getDropLocation().getPinCode()))
                    .filter(deal -> deal.getParcel().getParcelStatus().equals(ParcelStatus.PENDING_FOR_CARRIER_ACCEPTANCE))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }*/
}
