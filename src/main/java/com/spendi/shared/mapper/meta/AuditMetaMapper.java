
/**
 * @file AuditMetaMapper.java
 * @module shared/mapper/meta
 *
 * Mapper для AuditMeta ⇄ Document. Наслаивает поля поверх BaseMeta.
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
import com.spendi.core.base.BaseMapper;
import com.spendi.core.types.DocMapper;
import com.spendi.core.utils.InstantUtils;
import com.spendi.shared.model.meta.AuditMeta;
import com.spendi.shared.model.meta.MetaFields;

public class AuditMetaMapper extends BaseMapper<AuditMeta> implements DocMapper<AuditMeta> {
	private static final AuditMetaMapper INSTANCE = new AuditMetaMapper();
	private static final BaseMetaMapper BASE = BaseMetaMapper.getInstance();

	private AuditMetaMapper() {
		super(AuditMetaMapper.class.getSimpleName(), AuditMeta.class, MetaFields.META);
	}

	public static AuditMetaMapper getInstance() {
		return INSTANCE;
	}

	public Document toDocument(AuditMeta meta) {
		if (meta == null)
			return null;
		Document d = BASE.toDocument(meta); // базовые поля
		putIfNotNull(d, MetaFields.UPDATED_AT, meta.getUpdatedAt());
		putIfNotNull(d, MetaFields.UPDATED_BY, meta.getUpdatedBy());
		return d;
	}

	@Override
	public AuditMeta toEntity(Document d) {
		if (d == null)
			return null;

		return AuditMeta.builder() // используем builder для создания экземпляра BaseMeta
				// получаем Instant из документа или null, если значение отсутствует
				.createdAt(InstantUtils.getInstantOrNull(d.get(MetaFields.CREATED_AT)))
				// читаем ObjectId из документа или null, если значение отсутствует
				.createdBy(readObjectId(d.get(MetaFields.CREATED_BY)))
				// получаем Instant из документа или null, если значение отсутствует
				.updatedAt(InstantUtils.getInstantOrNull(d.get(MetaFields.UPDATED_AT)))
				// читаем ObjectId из документа или null, если значение отсутствует
				.updatedBy(readObjectId(d.get(MetaFields.UPDATED_BY)))
				// собираем экземпляр AuditMeta
				.build();
	}

}