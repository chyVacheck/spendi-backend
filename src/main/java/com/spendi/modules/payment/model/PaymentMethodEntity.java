
/**
 * @file PaymentMethodEntity.java
 * @module modules/payment/model
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment.model;

/**
 * ! lib imports
 */
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.bson.types.ObjectId;

/**
 * ! java imports
 */
import java.util.HashMap;
import java.util.Map;

/**
 * Сущность метода оплаты.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodEntity {

	/**
	 * Идентификатор метода оплаты.
	 */
	private ObjectId id;

	/**
	 * Идентификатор пользователя.
	 */
	private ObjectId userId;

	/**
	 * Информация о методе оплаты.
	 */
	private PaymentMethodInfo info;

	/**
	 * Информация о методе оплаты.
	 */
	private PaymentMethodDetails details;

	/**
	 * Системная информация о методе оплаты.
	 */
	private PaymentMethodSystem system;

	public String getHexId() {
		return id.toHexString();
	}

	/**
	 * Возвращает публичные данные метода оплаты.
	 * 
	 * @return Карта публичных данных.
	 */
	public Map<String, Object> getPublicData() {
		Map<String, Object> out = new HashMap<>(4);
		out.put("id", id.toHexString());
		out.put("info", info);
		out.put("details", details);
		out.put("system", system);

		return out;
	}
}
