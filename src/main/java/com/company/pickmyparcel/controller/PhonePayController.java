package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentInfo;
import com.company.pickmyparcel.model.DealPaymentStatus;
import com.company.pickmyparcel.service.DealServiceImpl;
import com.company.pickmyparcel.service.PaymentGatewayService;
import com.phonepe.sdk.pg.Env;
import com.phonepe.sdk.pg.common.http.PhonePeResponse;
import com.phonepe.sdk.pg.payments.PhonePePaymentClient;
import com.phonepe.sdk.pg.payments.models.requestV1.PgPayRequest;
import com.phonepe.sdk.pg.payments.models.responseV1.PgPayResponse;
import com.phonepe.sdk.pg.payments.models.responseV1.PgTransactionStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/phonePay")
public class PhonePayController {
    private static final Logger LOGGER = Logger.getLogger(PhonePayController.class.getName());
    private static final String VISHAL_CONTACT_NUMBER = "9960743366";
    private static final String PREM_CONTACT_NUMBER = "9404771200";


    @Autowired
    private DealServiceImpl dealService;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @PostMapping("/initiate")
    @ResponseBody
    public ResponseEntity<PhonePeResponse<PgPayResponse>> initiatePaymentUAT(@RequestBody Deal deal) throws IOException {
        PhonePeResponse<PgPayResponse> payResponse = new PhonePeResponse<>();
        LOGGER.info("****************** initiatePaymentUAT started for UAT flow ********************");
        try {
            String merchantId = "PGTESTPAYUAT105";
            String saltKey = "c45b52fe-f2c5-4ef6-a6b5-131aa89ed133";
            Integer saltIndex = 1;
            Env env = Env.UAT;
            boolean shouldPublishEvents = true;

            PhonePePaymentClient phonepeClient = new PhonePePaymentClient(merchantId, saltKey, saltIndex, env, shouldPublishEvents);
            Random random = new Random();
            String merchantTransactionId = "PMP" + random.nextInt(10000000);
            long amount = 100;
            String merchantUserId = "MUID" + random.nextInt(10000000);

            PgPayRequest pgPayRequest = PgPayRequest.PayPagePayRequestBuilder()
                    .amount(amount)
                    .merchantId(merchantId)
                    .merchantTransactionId(merchantTransactionId)
                    .redirectUrl("http://localhost:4200/dashboard/checkoutstatus?merchantTransactionId=" + merchantTransactionId)
                    .callbackUrl("http://localhost:4200/phonePay/callback")
                    .merchantUserId(merchantUserId)
                    .build();

            payResponse = phonepeClient.pay(pgPayRequest);
            Deal savedDeal = dealService.saveDealViaPhonePe(deal, DealPaymentStatus.PAYMENT_INITIATED);
            dealService.addDealPaymentInfo(savedDeal, merchantTransactionId, merchantTransactionId, DealPaymentStatus.PAYMENT_INITIATED.name());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warning("EXCEPTION OCCURRED IN :initiatePaymentUAT : " + e.getMessage());
        }
        return ResponseEntity.ok(payResponse);
    }

    @GetMapping("/checkPaymentStatus")
    public ResponseEntity<PhonePeResponse<PgTransactionStatusResponse>> checkPaymentStatusUAT
            (@RequestParam("merchantTransactionId") String merchantTransactionId) {
        LOGGER.info("****************** checkPaymentStatusUAT for UAT ********************");
        PhonePeResponse<PgTransactionStatusResponse> paymentStatusResponse = new PhonePeResponse<>();
        try {
            String merchantId = "PGTESTPAYUAT105";
            String saltKey = "c45b52fe-f2c5-4ef6-a6b5-131aa89ed133";
            Integer saltIndex = 1;
            Env env = Env.UAT;
            boolean shouldPublishEvents = true;
            String status = "";
            int maxAttempts = 10;
            int attemptCount = 0;
            int delay = 7000; // 7 seconds
            while ((!status.equals("PAYMENT_SUCCESS") && !status.equals("PAYMENT_ERROR")) && attemptCount < maxAttempts) {
                PhonePePaymentClient phonepeClient = new PhonePePaymentClient(merchantId, saltKey, saltIndex, env, shouldPublishEvents);
                paymentStatusResponse = phonepeClient.checkStatus(merchantTransactionId);
                status = paymentStatusResponse.getCode();
                Thread.sleep(delay);
                attemptCount++;
            }
            LOGGER.info("PgTransactionStatusResponse : " + paymentStatusResponse);
            Deal deal = new Deal();
            DealPaymentInfo dealPaymentInfo = dealService.getDealPaymentInfoByOrderId(merchantTransactionId);
            if ("PAYMENT_SUCCESS".equals(status)) {
                if (!"PAID".equals(dealPaymentInfo.getPaymentStatus())) {
                    LOGGER.info("Payment is successful, updating payment status to PAID in deal and payment info for transaction id : " + merchantTransactionId);
                    deal = dealService.saveDealAndPaymentInfo(merchantTransactionId, dealPaymentInfo, DealPaymentStatus.PAID);
                    String messageToCompany = "Order has been placed by sender : " + deal.getSender().getSenderFirstName() + " " + deal.getSender().getSenderLastName()
                            + ", CONTACT NO : " + deal.getSender().getSenderContactNo() + ", DEAL ID : " + deal.getDealId()
                            + ", ORDER ID : " + merchantTransactionId + " ORDER AMOUNT RS : " + deal.getDealTotal() + ", AT TIME : " + deal.getCreatedDate().toString();
                    //DealServiceImpl.sendNotificationToPickmyparcel_Internal(messageToCompany);
                    paymentStatusResponse.getData().setAmount(deal.getDealTotal().longValue());
                } else {
                    LOGGER.info("dealPaymentInfo is already marked as PAID");
                }
            } else {
                if (!"FAILED".equals(dealPaymentInfo.getPaymentStatus())) {
                    LOGGER.warning("Payment is failed, updating payment status to FAILED in deal and payment info for transaction id : " + merchantTransactionId);
                    deal = dealService.saveDealAndPaymentInfo(merchantTransactionId, dealPaymentInfo, DealPaymentStatus.FAILED);
                    String messageToCompany = "Payment has been failed for user : " + deal.getSender().getSenderFirstName() + " " + deal.getSender().getSenderLastName()
                            + ", CONTACT NO : " + deal.getSender().getSenderContactNo() + ", DEAL ID : " + deal.getDealId()
                            + ", ORDER ID : " + merchantTransactionId + " ORDER AMOUNT RS : " + deal.getDealTotal() + ", AT TIME : " + deal.getCreatedDate().toString();
                    //DealServiceImpl.sendNotificationToPickmyparcel_Internal(messageToCompany);
                } else {
                    LOGGER.info("dealPaymentInfo is already marked as FAILED");
                }
                paymentStatusResponse.getData().setAmount(deal.getDealTotal().longValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warning("Exception occurred in : checkPaymentStatusUAT for merchantTransactionId : " + merchantTransactionId + " " + e.getMessage());
            DealPaymentInfo dealPaymentInfo = dealService.getDealPaymentInfoByOrderId(merchantTransactionId);
            if (!"FAILED".equals(dealPaymentInfo.getPaymentStatus())) {
                Deal deal = dealService.saveDealAndPaymentInfo(merchantTransactionId, dealPaymentInfo, DealPaymentStatus.FAILED);
                String messageToCompany = "Payment has been failed for user : " + deal.getSender().getSenderFirstName() + " " + deal.getSender().getSenderLastName()
                        + ", CONTACT NO : " + deal.getSender().getSenderContactNo() + ", DEAL ID : " + deal.getDealId()
                        + ", ORDER ID : " + merchantTransactionId + ", AT TIME : " + deal.getCreatedDate().toString();
                // DealServiceImpl.sendNotificationToPickmyparcel_Internal(messageToCompany);
            } else {
                LOGGER.info("dealPaymentInfo is already marked as FAILED");
            }
        }
        return ResponseEntity.ok(paymentStatusResponse);
    }

    @PostMapping("/initiateProd")
    @ResponseBody
    public ResponseEntity<PhonePeResponse<PgPayResponse>> initiatePaymentProd(@RequestBody Deal deal) throws
            IOException {
        PhonePeResponse<PgPayResponse> payResponse = new PhonePeResponse<>();
        try {
            String merchantId = "PICKMYPARCELONLINE";
            String saltKey = "dac925b6-54fa-4ee1-a84a-5c9907c1f032";
            Integer saltIndex = 1;
            Env env = Env.PROD;
            boolean shouldPublishEvents = true;
            LOGGER.info("****************** initiatePaymentProd started for PROD ************************");
            PhonePePaymentClient phonepeClient = new PhonePePaymentClient(merchantId, saltKey, saltIndex, env, shouldPublishEvents);
            Random random = new Random();
            String merchantTransactionId = "PMP" + random.nextInt(10000000);
            long amount = 100;
            String merchantUserId = "MUID" + random.nextInt(10000000);

            PgPayRequest pgPayRequest = PgPayRequest.PayPagePayRequestBuilder()
                    .amount(amount)
                    .merchantId(merchantId)
                    .merchantUserId(merchantUserId)
                    .merchantTransactionId(merchantTransactionId)
                    .redirectUrl("https://www.pickmyparcel.in/dashboard/checkoutstatus?merchantTransactionId=" + merchantTransactionId)
                    .callbackUrl("https://www.pickmyparcel.in/phonePay/callback")
                    .build();

            payResponse = phonepeClient.pay(pgPayRequest);
            Deal savedDeal = dealService.saveDealViaPhonePe(deal, DealPaymentStatus.PAYMENT_INITIATED);
            dealService.addDealPaymentInfo(savedDeal, merchantTransactionId, merchantTransactionId, DealPaymentStatus.PAYMENT_INITIATED.name());
            LOGGER.info("INITIATION RESPONSE : " + payResponse);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warning("EXCEPTION OCCURRED IN :initiatePaymentProd : " + e.getMessage());
        }
        return ResponseEntity.ok(payResponse);
    }

    @GetMapping("/checkPaymentStatusProd")
    public ResponseEntity<PhonePeResponse<PgTransactionStatusResponse>> checkPaymentStatusProd
            (@RequestParam("merchantTransactionId") String merchantTransactionId) {
        PhonePeResponse<PgTransactionStatusResponse> paymentStatusResponse = new PhonePeResponse<>();
        LOGGER.info("****************** checkPaymentStatusProd started for PROD ************************");
        try {
            String merchantId = "PICKMYPARCELONLINE";
            String saltKey = "dac925b6-54fa-4ee1-a84a-5c9907c1f032";
            Integer saltIndex = 1;
            Env env = Env.PROD;
            boolean shouldPublishEvents = true;
            String status = "";
            LOGGER.info("CHECKING PAYMENT STATUS FOR PHONE PAY PROD : checkPaymentStatusProd : " + merchantTransactionId + "salt key : " + saltKey + "env : " + env + "env : " + env + "merchantId : " + merchantId);

            int maxAttempts = 10;
            int attemptCount = 0;
            int delay = 7000; // 7 seconds

            while ((!status.equals("PAYMENT_SUCCESS") && !status.equals("PAYMENT_ERROR")) && attemptCount < maxAttempts) {
                PhonePePaymentClient phonepeClient = new PhonePePaymentClient(merchantId, saltKey, saltIndex, env, shouldPublishEvents);
                paymentStatusResponse = phonepeClient.checkStatus(merchantTransactionId);
                status = paymentStatusResponse.getCode();
                Thread.sleep(delay);
                attemptCount++;
            }
            LOGGER.info("PgTransactionStatusResponse : " + paymentStatusResponse);
            Deal deal = new Deal();
            DealPaymentInfo dealPaymentInfo = dealService.getDealPaymentInfoByOrderId(merchantTransactionId);
            if ("PAYMENT_SUCCESS".equals(status)) {
                if (!"PAID".equals(dealPaymentInfo.getPaymentStatus())) {
                    LOGGER.info("Payment is successful, updating payment status to PAID in deal and payment info for transaction id : " + merchantTransactionId);
                    deal = dealService.saveDealAndPaymentInfo(merchantTransactionId, dealPaymentInfo, DealPaymentStatus.PAID);
                    String messageToCompany = "Order has been placed by sender : " + deal.getSender().getSenderFirstName() + " " + deal.getSender().getSenderLastName()
                            + ", CONTACT NO : " + deal.getSender().getSenderContactNo() + ", DEAL ID : " + deal.getDealId()
                            + ", ORDER ID : " + merchantTransactionId + " ORDER AMOUNT RS : " + deal.getDealTotal() + ", AT TIME : " + deal.getCreatedDate().toString();
                    DealServiceImpl.sendNotificationToPickmyparcel_Internal(messageToCompany);
                    paymentStatusResponse.getData().setAmount(deal.getDealTotal().longValue());
                } else {
                    LOGGER.info("dealPaymentInfo is already marked as PAID");
                }
            } else {
                if (!"FAILED".equals(dealPaymentInfo.getPaymentStatus())) {
                    LOGGER.warning("Payment is failed, updating payment status to FAILED in deal and payment info for transaction id : " + merchantTransactionId);
                    deal = dealService.saveDealAndPaymentInfo(merchantTransactionId, dealPaymentInfo, DealPaymentStatus.FAILED);
                    String messageToCompany = "Payment has been failed for user : " + deal.getSender().getSenderFirstName() + " " + deal.getSender().getSenderLastName()
                            + ", CONTACT NO : " + deal.getSender().getSenderContactNo() + ", DEAL ID : " + deal.getDealId()
                            + ", ORDER ID : " + merchantTransactionId + " ORDER AMOUNT RS : " + deal.getDealTotal() + ", AT TIME : " + deal.getCreatedDate().toString();
                    DealServiceImpl.sendNotificationToPickmyparcel_Internal(messageToCompany);
                } else {
                    LOGGER.info("dealPaymentInfo is already marked as FAILED");
                }
                paymentStatusResponse.getData().setAmount(deal.getDealTotal().longValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warning("Exception occurred in : checkPaymentStatusProd for merchantTransactionId : " + merchantTransactionId + " " + e.getMessage());
            DealPaymentInfo dealPaymentInfo = dealService.getDealPaymentInfoByOrderId(merchantTransactionId);
            if (!"FAILED".equals(dealPaymentInfo.getPaymentStatus())) {
                Deal deal = dealService.saveDealAndPaymentInfo(merchantTransactionId, dealPaymentInfo, DealPaymentStatus.FAILED);
                String messageToCompany = "Payment has been failed for user : " + deal.getSender().getSenderFirstName() + " " + deal.getSender().getSenderLastName()
                        + ", CONTACT NO : " + deal.getSender().getSenderContactNo() + ", DEAL ID : " + deal.getDealId()
                        + ", ORDER ID : " + merchantTransactionId + ", AT TIME : " + deal.getCreatedDate().toString();
                DealServiceImpl.sendNotificationToPickmyparcel_Internal(messageToCompany);
            } else {
                LOGGER.info("dealPaymentInfo is already marked as FAILED");
            }
        }
        return ResponseEntity.ok(paymentStatusResponse);
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handlePhonePeCallback(
            @RequestHeader("X-VERIFY") String verificationHeader,
            @RequestBody String base64EncodedPayload) {
        try {
            LOGGER.info("************** Received request for handlePhonePeCallback verificationHeader : " + verificationHeader + " base64EncodedPayload " + base64EncodedPayload);
            boolean isVerified = paymentGatewayService.verifyCallback(verificationHeader, base64EncodedPayload);

            if (isVerified) {
                String decodedPayload = new String(java.util.Base64.getDecoder().decode(base64EncodedPayload));
                LOGGER.info("Received PhonePe Callback Decoded Payload: " + decodedPayload);
                //here we will update status in db for that perticular transaction
                return ResponseEntity.ok("Callback handled successfully");
            } else {
                LOGGER.info("isVerified false");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Callback verification failed");
            }
        } catch (Exception e) {
            LOGGER.warning("Exception occurred while handling callback" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Callback verification failed");
        }
    }
}
