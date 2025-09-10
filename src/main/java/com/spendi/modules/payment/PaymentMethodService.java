/**
 * @file PaymentMethodService.java
 * @module modules/payment
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ! java imports
 */
import com.spendi.core.base.service.BaseRepositoryService;
import com.spendi.core.response.ServiceResponse;
import com.spendi.modules.user.UserService;

public class PaymentMethodService extends BaseRepositoryService<PaymentMethodRepository, PaymentMethodEntity> {

	private static volatile PaymentMethodService INSTANCE;

	public PaymentMethodService(PaymentMethodRepository repository) {
		super(PaymentMethodService.class.getSimpleName(), repository);
	}

	public static void init(PaymentMethodRepository repository) {
		synchronized (PaymentMethodService.class) {
			if (INSTANCE == null) {
				INSTANCE = new PaymentMethodService(repository);
			}
		}
	}

	public static PaymentMethodService getInstance() {
		PaymentMethodService ref = INSTANCE;
		if (ref == null)
			throw new IllegalStateException("PaymentMethodService not initialized");
		return ref;
	}

	/** Создать способ оплаты для пользователя и добавить id в пользователя. */
	public ServiceResponse<PaymentMethodEntity> createForUser(String requestId, String userId,
			PaymentMethodEntity entity) {
		var now = Instant.now();
		entity.id = new ObjectId();
		entity.userId = new ObjectId(userId);
		if (entity.system == null)
			entity.system = new PaymentMethodEntity.System();
		if (entity.system.meta == null)
			entity.system.meta = new PaymentMethodEntity.Meta();
		entity.system.status = entity.system.status == null
				? PaymentMethodEntity.EPaymentMethodStatus.Active
				: entity.system.status;
		entity.system.meta.createdAt = now;
		entity.system.meta.updatedAt = now;

		var created = super.createOne(entity);

		// append to user's finance.paymentMethodIds
		UserService users = UserService.getInstance();
		var user = users.getById(userId).getData();
		List<String> list = user.finance != null && user.finance.paymentMethodIds != null
				? user.finance.paymentMethodIds
				: List.of();
		var updated = new java.util.ArrayList<>(list);
		updated.add(entity.id.toHexString());
		Map<String, Object> updates = new HashMap<>();
		updates.put("finance.paymentMethodIds", updated);
		updates.put("system.meta.updatedAt", Date.from(now));
		users.updateById(userId, updates);

		return created;
	}

	/** Удалить способ оплаты и убрать его id из пользователя. */
	public ServiceResponse<String> deleteForUser(String requestId, String userId, String methodId) {
		// delete method
		var deleted = this.deleteById(methodId);

		// pull from user's list
		UserService users = UserService.getInstance();
		var user = users.getById(userId).getData();
		List<String> list = user.finance != null && user.finance.paymentMethodIds != null
				? user.finance.paymentMethodIds
				: List.of();
		var updated = new java.util.ArrayList<>(list);
		updated.remove(methodId);
		Map<String, Object> updates = new HashMap<>();
		updates.put("finance.paymentMethodIds", updated);
		updates.put("system.meta.updatedAt", Date.from(Instant.now()));
		users.updateById(userId, updates);

		return deleted;
	}

	/** Обновить поле order у метода оплаты. */
	public ServiceResponse<PaymentMethodEntity> updateOrder(String requestId, String userId, String methodId,
			int order) {
		// ensure belongs to user
		var pm = this.getById(methodId).getData();
		if (pm.userId == null || !pm.userId.toHexString().equals(userId)) {
			// если не совпадает, просто отдаём not found
			throw new com.spendi.core.exceptions.EntityNotFoundException(
					PaymentMethodEntity.class.getSimpleName(), Map.of("id", methodId, "userId", userId));
		}
		var updates = Map.<String, Object>of(
				"info.order", order,
				"system.meta.updatedAt", Date.from(Instant.now()));
		return this.updateById(methodId, updates);
	}
}
