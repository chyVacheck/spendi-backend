
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
import com.spendi.core.utils.InstantUtils;
import com.spendi.shared.model.meta.AuditMeta;
import com.spendi.shared.model.meta.MetaFields;

public class AuditMetaMapper extends BaseMetaMapper {
	private static final AuditMetaMapper INSTANCE = new AuditMetaMapper();

	private AuditMetaMapper() {
		super(AuditMetaMapper.class.getSimpleName());
	}

	protected AuditMetaMapper(String mapperName) {
		super(mapperName);
	}

	public static AuditMetaMapper getInstance() {
		return INSTANCE;
	}

	public Document toDocument(AuditMeta meta) {
		if (meta == null)
			return null;
		Document d = super.toDocument(meta);
		putIfNotNull(d, MetaFields.UPDATED_AT, meta.getUpdatedAt());
		putIfNotNull(d, MetaFields.UPDATED_BY, meta.getUpdatedBy());
		return d;
	}

	@Override
	public AuditMeta fromDocument(Document d) {
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