/**
 * @file UserFinance.java
 * @module modules/user/model
 * @description Финансовые ссылки пользователя: дефолтный аккаунт и набор методов оплаты.
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user.model;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * ! java imports
 */
import java.util.Set;

/**
 * Класс, представляющий финансовые данные пользователя. Содержит ссылки на дефолтный аккаунт и набор идентификаторов
 * методов оплаты.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserFinance {

	/**
	 * Идентификатор аккаунта по умолчанию для пользователя.
	 */
	private ObjectId defaultAccountId;

	/**
	 * Набор идентификаторов методов оплаты, связанных с пользователем. По умолчанию инициализируется пустым набором и
	 * никогда не бывает null.
	 */
	@Builder.Default
	private Set<ObjectId> paymentMethodIds = Set.of(); // никогда не null

	/**
	 * Возвращает неизменяемый набор идентификаторов методов оплаты пользователя. Если внутренний набор paymentMethodIds
	 * равен null, возвращает пустой набор.
	 * 
	 * @return Неизменяемый Set<ObjectId> идентификаторов методов оплаты.
	 */
	public Set<ObjectId> getPaymentMethodIds() {
		return paymentMethodIds == null ? Set.of() : Set.copyOf(paymentMethodIds);
	}

	/**
	 * Устанавливает набор идентификаторов методов оплаты для пользователя. Если переданный набор равен null,
	 * устанавливает пустой набор.
	 * 
	 * @param ids Набор ObjectId методов оплаты.
	 */
	public void setPaymentMethodIds(Set<ObjectId> ids) {
		this.paymentMethodIds = (ids == null) ? Set.of() : Set.copyOf(ids);
	}

	/**
	 * Проверяет, принадлежит ли указанный метод оплаты пользователю.
	 * 
	 * @param pmId ObjectId метода оплаты для проверки.
	 * @return true, если метод оплаты принадлежит пользователю, иначе false.
	 */
	public boolean hasPaymentMethod(ObjectId pmId) {
		return getPaymentMethodIds().contains(pmId);
	}
}