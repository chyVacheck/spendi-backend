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
import java.util.Map;

import com.spendi.core.base.database.MongoUpdateBuilder;
/**
 * ! java imports
 */
import com.spendi.core.base.service.BaseRepositoryService;
import com.spendi.core.response.ServiceResponse;
import com.spendi.modules.payment.cmd.PaymentMethodCreateCmd;
import com.spendi.modules.payment.model.EPaymentMethodStatus;
import com.spendi.modules.payment.model.PaymentMethodEntity;
import com.spendi.modules.payment.model.PaymentMethodInfo;
import com.spendi.modules.payment.model.PaymentMethodSystem;
import com.spendi.shared.model.meta.LifecycleMeta;

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

	/**
	 * Создать способ оплаты для пользователя.
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param dto       данные для создания способа оплаты
	 * @return созданный способ оплаты
	 */
	public ServiceResponse<PaymentMethodEntity> createOne(String requestId, String userId, PaymentMethodCreateCmd cmd) {

		final PaymentMethodEntity e = new PaymentMethodEntity();
		e.setId(new ObjectId());
		e.setUserId(new ObjectId(userId));

		// info
		final var srcInfo = cmd.getInfo();
		final var info = new PaymentMethodInfo();
		info.setType(srcInfo.getType());
		info.setName(srcInfo.getName());
		info.setCurrency(srcInfo.getCurrency());
		info.setOrder(srcInfo.getOrder());
		info.setTags(srcInfo.getTags());

		// details (уже полиморфный объект: CardDetails | BankDetails | WalletDetails)
		e.setDetails(cmd.getDetails());

		// system
		final var sys = new PaymentMethodSystem();
		sys.setStatus(EPaymentMethodStatus.ACTIVE);
		sys.setMeta(new LifecycleMeta());
		e.setSystem(sys);

		return super.createOne(e);
	}

	/**
	 * Обновить поле order у метода оплаты.
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param methodId  строковый ObjectId способа оплаты
	 * @param order     новый порядок способа оплаты
	 * @return обновленный способ оплаты
	 */
	public ServiceResponse<PaymentMethodEntity> updateOrder(String requestId, String userId, String methodId,
			int order) {
		// ensure belongs to user
		var pm = this.getById(methodId).getData();

		// если не совпадает, просто отдаём not found
		if (!pm.getUserId().toHexString().equals(userId)) {
			throw new com.spendi.core.exceptions.EntityNotFoundException(PaymentMethodEntity.class.getSimpleName(),
					Map.of("id", methodId, "userId", userId));
		}

		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.set("info.order", order);
		updateBuilder.currentDate("system.meta.updatedAt");

		var updated = this.updateById(methodId, updateBuilder.build());

		return ServiceResponse.updated(updated.getData());
	}
}
