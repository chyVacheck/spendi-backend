
/**
 * @file LifecycleMetaMapper.java
 * @module shared/mapper/meta
 *
 * Mapper для LifecycleMeta ⇄ Document. Добавляет soft-delete поля.
 *
 * @author Dmytro Shakh
 */

package com.spendi.shared.mapper.meta;

/**
 * ! lib imports
 */
import org.bson.Document;

/**
 * ! my imports
 */
import com.spendi.core.utils.InstantUtils;
import com.spendi.shared.model.meta.LifecycleMeta;
import com.spendi.shared.model.meta.MetaFields;

public final class LifecycleMetaMapper extends AuditMetaMapper {
	private static final LifecycleMetaMapper INSTANCE = new LifecycleMetaMapper();

	private LifecycleMetaMapper() {
		super(LifecycleMetaMapper.class.getSimpleName());
	}

	public static LifecycleMetaMapper getInstance() {
		return INSTANCE;
	}

	public Document toDocument(LifecycleMeta meta) {
		if (meta == null)
			return null;
		Document d = super.toDocument(meta);
		putIfNotNull(d, MetaFields.DELETED_AT, meta.getDeletedAt());
		putIfNotNull(d, MetaFields.DELETED_BY, meta.getDeletedBy());
		return d;
	}

	@Override
	public LifecycleMeta fromDocument(Document d) {
		if (d == null)
			return null;

		return LifecycleMeta.builder() // используем builder для создания экземпляра BaseMeta
				// получаем Instant из документа или null, если значение отсутствует
				.createdAt(InstantUtils.getInstantOrNull(d.get(MetaFields.CREATED_AT)))
				// читаем ObjectId из документа или null, если значение отсутствует
				.createdBy(readObjectId(d.get(MetaFields.CREATED_BY)))
				// получаем Instant из документа или null, если значение отсутствует
				.updatedAt(InstantUtils.getInstantOrNull(d.get(MetaFields.UPDATED_AT)))
				// читаем ObjectId из документа или null, если значение отсутствует
				.updatedBy(readObjectId(d.get(MetaFields.UPDATED_BY)))
				// получаем Instant из документа или null, если значение отсутствует
				.deletedAt(InstantUtils.getInstantOrNull(d.get(MetaFields.DELETED_AT)))
				// читаем ObjectId из документа или null, если значение отсутствует
				.deletedBy(readObjectId(d.get(MetaFields.DELETED_BY)))
				// собираем экземпляр LifecycleMeta
				.build();
	}

}