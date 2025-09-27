
/**
 * @file PaymentMethodRepository.java
 * @module modules/payment
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

/**
 * ! java imports
 */
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.modules.payment.model.PaymentMethodEntity;

public class PaymentMethodRepository extends BaseRepository<PaymentMethodEntity> {

	public static final String COLLECTION = "payment_methods";

	public PaymentMethodRepository(MongoDatabase db) {
		super(PaymentMethodRepository.class.getSimpleName(), PaymentMethodEntity.class, db, COLLECTION,
				PaymentMethodMapper.getInstance());
		ensureIndexes();
	}

	private void ensureIndexes() {
		// Поиск по userId частый
		collection.createIndex(Indexes.ascending("userId"));
		// Условная уникальность имени в рамках пользователя (не строго обязательно)
		collection.createIndex(Indexes.ascending("userId", "info.name"), new IndexOptions().unique(false));
	}

	public List<PaymentMethodEntity> findByUserId(String userId, int page, int limit) {
		return this.findMany("userId", new ObjectId(userId), page, limit);
	}
}
