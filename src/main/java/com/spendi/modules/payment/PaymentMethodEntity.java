/**
 * @file PaymentMethodEntity.java
 * @module modules/payment
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ! java imports
 */
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethodEntity {

	public ObjectId id;
	@NotNull
	public ObjectId userId;

	@NotNull
	public Info info;
	@NotNull
	public Details details;
	@NotNull
	public System system;

	public static class Info {
		@NotNull
		public EPaymentMethodType type;
		@NotBlank
		@Size(max = 80)
		public String name;
		@NotBlank
		@Size(min = 3, max = 3)
		public String currency; // ISO 4217
		public Integer order; // UI order
		public List<String> tags;
	}

	public static class Details {
		public Card card;
		public Bank bank;
		public Wallet wallet;
	}

	public static class Card {
		public String brand;
		public String last4;
		public Integer expMonth; // 1-12
		public Integer expYear; // YYYY
	}

	public static class Bank {
		public String bankName;
		public String accountMasked; // e.g. "UA** **** 1234"
	}

	public static class Wallet {
		public String provider; // PayPal/Revolut/...
		public String handle; // identifier/nickname
	}

	public static class System {
		@NotNull
		public EPaymentMethodStatus status;
		@NotNull
		public Meta meta;
	}

	public static class Meta {
		public Instant createdAt;
		public Instant updatedAt;
		public Instant archivedAt;
	}

	public enum EPaymentMethodType {
		Cash, Card, BankAccount, EWallet, Other
	}

	public enum EPaymentMethodStatus {
		Active, Archived, Deleted
	}

	public Map<String, Object> getPublicData() {
		Map<String, Object> out = new HashMap<>(6);
		out.put("id", id.toHexString());
		Map<String, Object> i = new HashMap<>(5);
		i.put("type", info.type);
		i.put("name", info.name);
		i.put("currency", info.currency);
		i.put("order", info.order);
		i.put("tags", info.tags);
		out.put("info", i);

		Map<String, Object> d = new HashMap<>(3);
		d.put("card", details.card);
		d.put("bank", details.bank);
		d.put("wallet", details.wallet);
		out.put("details", d);

		Map<String, Object> s = new HashMap<>(2);
		s.put("status", system.status);
		Map<String, Object> m = new HashMap<>(3);
		m.put("createdAt", system.meta.createdAt);
		m.put("updatedAt", system.meta.updatedAt);
		m.put("archivedAt", system.meta.archivedAt);
		s.put("meta", m);
		out.put("system", s);

		return out;
	}
}
