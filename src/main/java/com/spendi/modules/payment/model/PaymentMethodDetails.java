
/**
 * @file PaymentMethodDetails.java
 * @module modules/payment/model
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.model;

public sealed interface PaymentMethodDetails permits BankDetails, CardDetails, WalletDetails {

}
