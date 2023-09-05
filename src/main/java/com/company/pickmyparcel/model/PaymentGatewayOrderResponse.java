package com.company.pickmyparcel.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class PaymentGatewayOrderResponse {
	Integer dealId;
	String bankTxnId;
	String orderId;
	String txnAmount;
	String resultStatus;
	String message;
}
