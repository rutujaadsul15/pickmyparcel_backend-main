package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentStatus;
import com.company.pickmyparcel.model.PaymentGatewayOrderResponse;
import com.company.pickmyparcel.service.DealServiceImpl;
import com.paytm.pg.merchant.PaytmChecksum;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/paytm")
public class PaytmController {
    private static final Logger LOGGER = Logger.getLogger(PaytmController.class.getName());
    private static final String VISHAL_CONTACT_NUMBER = "9960743366";
    private static final String PREM_CONTACT_NUMBER = "9404771200";

    @Autowired
    private DealServiceImpl dealService;

    @PostMapping("/createOrder")
    @ResponseBody
    public ResponseEntity<PaymentGatewayOrderResponse> processOrder(@RequestBody Deal deal, @PathParam("upiId") String upiId) throws Exception {
        LOGGER.info("***********************************INSIDE processOrder() method, PROCESSING ORDER FOR UPI : " + upiId);
        PaymentGatewayOrderResponse paymentGatewayOrderResponse = new PaymentGatewayOrderResponse();
        Random random = new Random();
        JSONObject paytmParams = new JSONObject();
        JSONObject processTxnResponseBody = new JSONObject();
        String orderId = "PMP" + random.nextInt(10000000);
        JSONObject body = new JSONObject();
        body.put("requestType", "Payment");
        body.put("mid", "JBEzpy42288253468787");
        body.put("websiteName", "DEFAULT");
        body.put("orderId", orderId);
        body.put("callbackUrl", "localhost");

        JSONObject txnAmount = new JSONObject();
        txnAmount.put("value", "1.00");
        txnAmount.put("currency", "INR");

        JSONObject userInfo = new JSONObject();
        userInfo.put("custId", "CUST_002");
        body.put("txnAmount", txnAmount);
        body.put("userInfo", userInfo);

        //generate checksum
        String checksum = PaytmChecksum.generateSignature(body.toString(), "fO2vwcNCUf9PJTsS");

        JSONObject head = new JSONObject();
        head.put("signature", checksum);

        paytmParams.put("body", body);
        paytmParams.put("head", head);

        String post_data = paytmParams.toString();

        LOGGER.info("****************************CALLING initiateTransaction PAYTM API");
        URL url = new URL("https://securegw.paytm.in/theia/api/v1/initiateTransaction?mid=JBEzpy42288253468787&orderId=" + orderId);

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
            requestWriter.writeBytes(post_data);
            requestWriter.close();
            String responseData = "";
            InputStream is = connection.getInputStream();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
            if ((responseData = responseReader.readLine()) != null) {
                LOGGER.info("\nInit transaction Response: " + responseData);
            }
            responseReader.close();

            //read the txnToken
            JSONObject responseJSON = new JSONObject(responseData);
            JSONObject responseBody = new JSONObject(responseJSON.get("body").toString());
            String txnToken = responseBody.get("txnToken").toString();

            // validate VPA api
            paytmParams = new JSONObject();

            body = new JSONObject();
            body.put("vpa", upiId);

            head = new JSONObject();
            head.put("tokenType", "TXN_TOKEN");
            head.put("txnToken", txnToken);

            paytmParams.put("body", body);
            paytmParams.put("head", head);

            post_data = paytmParams.toString();

            /* for Staging */
//            url = new URL("https://securegw-stage.paytm.in/theia/api/v1/vpa/validate?mid=JBEzpy42288253468787&orderId=ORDERID_98928");

            /* for Production */
            LOGGER.info("***************************CALLING validate PAYTM API");
            url = new URL("https://securegw.paytm.in/theia/api/v1/vpa/validate?mid=JBEzpy42288253468787&orderId=" + orderId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            requestWriter = new DataOutputStream(connection.getOutputStream());
            requestWriter.writeBytes(post_data);
            requestWriter.close();
            responseData = "";
            is = connection.getInputStream();
            responseReader = new BufferedReader(new InputStreamReader(is));
            if ((responseData = responseReader.readLine()) != null) {
                LOGGER.info("\nValidate VPA API Response: " + responseData);
            }
            responseReader.close();


            // process transaction
            paytmParams = new JSONObject();

            body = new JSONObject();
            body.put("requestType", "NATIVE");
            body.put("mid", "JBEzpy42288253468787");
            body.put("orderId", orderId);
            body.put("paymentMode", "UPI");
            body.put("channelCode", "collect");
            body.put("payerAccount", upiId);

            head = new JSONObject();
            head.put("txnToken", txnToken);

            paytmParams.put("body", body);
            paytmParams.put("head", head);

            post_data = paytmParams.toString();


            /* for Staging */
//            url = new URL("https://securegw-stage.paytm.in/theia/api/v1/processTransaction?mid=JBEzpy42288253468787&orderId=ORDERID_98928");

            /* for Production */
            LOGGER.info("*************************************CALLING processTransaction PAYTM API");
            url = new URL("https://securegw.paytm.in/theia/api/v1/processTransaction?mid=JBEzpy42288253468787&orderId=" + orderId);


            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            requestWriter = new DataOutputStream(connection.getOutputStream());
            requestWriter.writeBytes(post_data);
            requestWriter.close();
            responseData = "";
            is = connection.getInputStream();
            responseReader = new BufferedReader(new InputStreamReader(is));
            if ((responseData = responseReader.readLine()) != null) {
                LOGGER.info("\n**************Process transaction Response*******************: " + responseData);
            }
            JSONObject processTxnResponseJson = new JSONObject(responseData);
            processTxnResponseBody = new JSONObject(processTxnResponseJson.get("body").toString());
            String processTxnResultInfo = processTxnResponseBody.get("resultInfo").toString();
            JSONObject processTxnResultInfoJson = new JSONObject(processTxnResultInfo);
            String processTxnStatus = processTxnResultInfoJson.get("resultStatus").toString();
            if ("F".equals(processTxnStatus)) {
                LOGGER.info("Process transaction FAILED WITH RESULT STATUS : F");
                throw new Exception(processTxnResultInfoJson.get("resultMsg").toString());
            }

            responseReader.close();


           /* System.out.print("\n**********************Enter any key**********************************************************************-");
            Scanner sc = new Scanner(System.in);
            sc.nextLine();*/

            // validate transaction status
            /* initialize an object */
            String transactionStatus = "PENDING";
            JSONObject finalResponseJson = new JSONObject();
            JSONObject finalResponseBody = new JSONObject();
            JSONObject resultInfoJson = new JSONObject();
            int maxAttempts = 10;
            int attemptCount = 0;
            int delay = 7000; // 5 seconds

            while (attemptCount < maxAttempts) {
                paytmParams = new JSONObject();

                /* body parameters */
                body = new JSONObject();

                /* Find your MID in your Paytm Dashboard at https://dashboard.paytm.com/next/apikeys */
                body.put("mid", "JBEzpy42288253468787");
                body.put("orderId", orderId);

                checksum = PaytmChecksum.generateSignature(body.toString(), "fO2vwcNCUf9PJTsS");
                /* head parameters */
                head = new JSONObject();

                /* put generated checksum value here */
                head.put("signature", checksum);

                /* prepare JSON string for request */
                paytmParams.put("body", body);
                paytmParams.put("head", head);
                post_data = paytmParams.toString();

                /* for Staging */
                //url = new URL("https://securegw-stage.paytm.in/v3/order/status");
                /* for Production */
                url = new URL("https://securegw.paytm.in/v3/order/status");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                requestWriter = new DataOutputStream(connection.getOutputStream());
                requestWriter.writeBytes(post_data);
                requestWriter.close();
                responseData = "";
                is = connection.getInputStream();
                responseReader = new BufferedReader(new InputStreamReader(is));
                if ((responseData = responseReader.readLine()) != null) {
                    LOGGER.info("********Transaction Status Response IN WHILE LOOP:************ " + responseData);
                }
                // System.out.append("Request: " + post_data);
                responseReader.close();
                finalResponseJson = new JSONObject(responseData);
                finalResponseBody = new JSONObject(finalResponseJson.get("body").toString());
                String resultInfo = finalResponseBody.get("resultInfo").toString();
                resultInfoJson = new JSONObject(resultInfo);
                transactionStatus = resultInfoJson.get("resultStatus").toString();
                if (transactionStatus != null && transactionStatus.equals("TXN_SUCCESS")) {
                    break; // Transaction is successful
                }
                Thread.sleep(delay);
                attemptCount++;
            }//end of while
            if ("TXN_SUCCESS".equals(transactionStatus)) {
                LOGGER.info("*********************INSIDE TXN SUCCESS********************************");
                String successOrderId = finalResponseBody.get("orderId").toString();
                String bankTxnId = finalResponseBody.get("bankTxnId").toString();
                String txnAmount2 = finalResponseBody.get("txnAmount").toString();

                paymentGatewayOrderResponse.setOrderId(successOrderId);
                paymentGatewayOrderResponse.setBankTxnId(bankTxnId); // IMPORTANT : IN SUCCESS CASE WE ARE STORING bankTxnId and FAILURE CASE WE ARE STORING txnId.
                paymentGatewayOrderResponse.setTxnAmount(txnAmount2);
                paymentGatewayOrderResponse.setResultStatus(transactionStatus);
                Deal savedDeal = dealService.saveDealViaPaytm(deal, DealPaymentStatus.PAID);
                String messageToCompany = "Order has been placed by sender : " + savedDeal.getSender().getSenderFirstName() + " " + savedDeal.getSender().getSenderLastName() + ", CONTACT NO : " + savedDeal.getSender().getSenderContactNo() + ", DEAL ID : " + savedDeal.getDealId() + ", ORDER ID : " + successOrderId + ", AT TIME : " + savedDeal.getCreatedDate().toString();
                //dealService.sendInternalSms(VISHAL_CONTACT_NUMBER, PREM_CONTACT_NUMBER, messageToCompany, "1707168672175600188");
                DealServiceImpl.sendNotificationToPickmyparcel_Internal(messageToCompany);
                paymentGatewayOrderResponse.setDealId(savedDeal.getDealId());
                dealService.addDealPaymentInfo(savedDeal, successOrderId, bankTxnId, DealPaymentStatus.PAID.name());
            } else {
                LOGGER.info("*********************INSIDE ELSE BLOCK WITH TRANSACTION STATUS FAILED**************** : " + transactionStatus);
                paymentGatewayOrderResponse.setOrderId(finalResponseBody.get("orderId").toString());
                paymentGatewayOrderResponse.setMessage(resultInfoJson.get("resultMsg").toString());
                paymentGatewayOrderResponse.setResultStatus(transactionStatus);
                Deal savedDeal = dealService.saveDealViaPaytm(deal, DealPaymentStatus.FAILED);
                dealService.addDealPaymentInfo(savedDeal, finalResponseBody.get("orderId").toString(), finalResponseBody.get("txnId").toString(), DealPaymentStatus.FAILED.name());
            }
        } catch (Exception exception) {
            LOGGER.info("*******************EXCEPTION OCCURRED WHILE PROCESSING ODER : " + exception.getMessage() + exception.getCause());
            exception.printStackTrace();
            PaymentGatewayOrderResponse errResponse = new PaymentGatewayOrderResponse();
            errResponse.setOrderId(processTxnResponseBody.get("orderId").toString());
            errResponse.setBankTxnId(processTxnResponseBody.get("txnId").toString());
            errResponse.setTxnAmount(processTxnResponseBody.get("txnAmount").toString());
            errResponse.setMessage(exception.getMessage());
            errResponse.setResultStatus("FAILED");
            Deal savedDeal = dealService.saveDealViaPaytm(deal, DealPaymentStatus.FAILED);
            errResponse.setDealId(savedDeal.getDealId());
            dealService.addDealPaymentInfo(savedDeal, processTxnResponseBody.get("orderId").toString(), processTxnResponseBody.get("txnId").toString(), DealPaymentStatus.FAILED.name());
            return ResponseEntity.ok(errResponse);
        }
        return ResponseEntity.ok(paymentGatewayOrderResponse);
    }

    public static String removePMP(String input) {
        if (input.contains("PMP")) {
            return input.replace("PMP", "");
        } else {
            return input;
        }
    }

    @GetMapping("/getOrderStatus")
    public ResponseEntity<String> getOrderStatus(@RequestParam("orderId") String orderId) throws Exception {
        LOGGER.info("INSIDE getOrderStatus(), REQUEST RECEIVED FOR ORDER ID :  " + orderId);
        JSONObject paytmParams = new JSONObject();
        JSONObject body = new JSONObject();
        JSONObject head = new JSONObject();
        String transactionStatus = "PENDING";
        JSONObject finalResponseJson = new JSONObject();
        JSONObject finalResponseBody = new JSONObject();
        JSONObject resultInfoJson = new JSONObject();
        //while ("PENDING".equals(transactionStatus)) {
        paytmParams = new JSONObject();

        /* body parameters */
        body = new JSONObject();

        /* Find your MID in your Paytm Dashboard at https://dashboard.paytm.com/next/apikeys */
        body.put("mid", "JBEzpy42288253468787");

        /* Enter your order id which needs to be check status for */
        body.put("orderId", orderId
        );
        String checksum = PaytmChecksum.generateSignature(body.toString(), "fO2vwcNCUf9PJTsS");
        /* head parameters */
        head = new JSONObject();

        /* put generated checksum value here */
        head.put("signature", checksum);

        /* prepare JSON string for request */
        paytmParams.put("body", body);
        paytmParams.put("head", head);
        String post_data = paytmParams.toString();

        /* for Staging */
        //url = new URL("https://securegw-stage.paytm.in/v3/order/status");
        /* for Production */
        URL url = new URL("https://securegw.paytm.in/v3/order/status");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
        requestWriter.writeBytes(post_data);
        requestWriter.close();
        String responseData = "";
        InputStream is = connection.getInputStream();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
        if ((responseData = responseReader.readLine()) != null) {
            LOGGER.info("\nTransaction Status Response in getOrderStatus(): " + responseData);
        }
        // System.out.append("Request: " + post_data);
        responseReader.close();
        finalResponseJson = new JSONObject(responseData);
        finalResponseBody = new JSONObject(finalResponseJson.get("body").toString());
        String resultInfo = finalResponseBody.get("resultInfo").toString();
        resultInfoJson = new JSONObject(resultInfo);
        transactionStatus = resultInfoJson.get("resultStatus").toString();
        return ResponseEntity.ok(transactionStatus);
    }
}
