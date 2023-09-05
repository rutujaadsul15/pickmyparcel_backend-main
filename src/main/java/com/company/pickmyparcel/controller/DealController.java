package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentInfo;
import com.company.pickmyparcel.model.DealResponse;
import com.company.pickmyparcel.model.requests.SearchDealsRequest;
import com.company.pickmyparcel.service.DealServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/deals")
@CrossOrigin(origins = "*")
public class DealController {
    private static final Logger LOGGER = Logger.getLogger(DealController.class.getName());
    @Autowired
    private DealServiceImpl dealServiceimpl;

    @PostMapping("/submitDeal")
    //todo : use preAuthorise Annotation. to authenticate roles and remove mapping from securityConfig file
    public ResponseEntity<Deal> saveDeal(@RequestBody @Valid Deal deal) {
        Deal deal1 = dealServiceimpl.saveDeal(deal);
        return ResponseEntity.ok(deal1);
    }

    @PostMapping("/calculateDealDistanceAndDealTotal")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<Deal> calculateDealDistanceAndDealTotal(@RequestBody @Valid Deal deal) {
        Deal deal1 = dealServiceimpl.calculateDealDistanceAndDealTotal(deal);
        return ResponseEntity.ok(deal1);
    }

    @PostMapping("/searchDeals")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<List<Deal>> searchDeals(@RequestBody @Valid SearchDealsRequest searchDealsRequest) {
        List<Deal> deals = dealServiceimpl.searchDeals(searchDealsRequest);
        return ResponseEntity.ok(deals);
    }


    //This api will be called by CARRIER when he/she wants to send request to sender and confirm the deal
    @PostMapping("/requestSenderForConfirmation")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<Deal> acceptDeal(@RequestBody @Valid Deal dealConfirmationRequest) {
        LOGGER.info("Request received for /requestSenderForConfirmation with Deal : " + dealConfirmationRequest);
        Deal deal = dealServiceimpl.requestSenderForConfirmation(dealConfirmationRequest);
        return ResponseEntity.ok(deal);
    }

    @GetMapping("/getdealbydealid")
    //todo : use preAuthorise Annotation. to authenticate roles
    public Deal getDealByDealId(@RequestParam("dealId") Integer dealId) {
        return dealServiceimpl.getDealByDealId(dealId);
    }

    @GetMapping("/getSubmittedDealsByContactNumber")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<DealResponse> getSubmittedDealsByContactNumber(@RequestParam("contactNumber") String contactNumber) {
        DealResponse submittedDealsByContactNumber = dealServiceimpl.getSubmittedDealsByContactNumber(contactNumber);
        return ResponseEntity.ok(submittedDealsByContactNumber);
    }

    @GetMapping("/getPickupDealsByContactNumber")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<DealResponse> getPickupDealsByContactNumber(@RequestParam("contactNumber") String contactNumber) {
        DealResponse pickupDealsByContactNumber = dealServiceimpl.getPickupDealsByContactNumber(contactNumber);
        return ResponseEntity.ok(pickupDealsByContactNumber);
    }

    //This api will be called when sender will confirm the deal or cancel the created deal
    @GetMapping("/updateParcelStatus")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<Deal> updateParcelStatus(@RequestParam("dealId") Integer dealId, String parcelStatus) {
        LOGGER.info("Request received for updateParcelStatus with dealId : " + dealId + " parcelStatus : " + parcelStatus);
        Deal deal = dealServiceimpl.updateParcelStatus(dealId, parcelStatus);
        if (deal != null) {
            return ResponseEntity.ok(deal);
        }
        return ResponseEntity.internalServerError().body(null);
    }

    @GetMapping("/verifyOTPAndCompleteDeal")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<Boolean> verifyOTPAndCompleteDeal(@RequestParam("dealId") Integer dealId, @RequestParam("otp") String otp) {
        LOGGER.info("Request received for /verifyOTPAndCompleteDeal with dealId : " + dealId + " otp : " + otp);
        boolean result = dealServiceimpl.verifyReceiverOTP(dealId, otp);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.internalServerError().body(false);
        }
    }

    @GetMapping("/getDealPaymentInfo")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<DealPaymentInfo> getDealPaymentInfo(@RequestParam("dealId") Integer dealId) {
        LOGGER.info("Request received for /getDealPaymentInfo with dealId : " + dealId);
        DealPaymentInfo dealPaymentInfo = dealServiceimpl.getDealPaymentInfoByDealId(dealId);
        if (dealPaymentInfo != null) {
            return ResponseEntity.ok(dealPaymentInfo);
        } else {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    //This api will be called in RAZORPAY payment gateway flow
    /*@PostMapping("/updateDealPaymentStatus")
    todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<Boolean> updateDealPaymentStatus(@RequestBody @Valid RazorPayPaymentOrderDetails paymentOrderDetails) {
        boolean status = dealServiceimpl.updateDealPaymentStatus(paymentOrderDetails);
        if (status) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }*/
}
