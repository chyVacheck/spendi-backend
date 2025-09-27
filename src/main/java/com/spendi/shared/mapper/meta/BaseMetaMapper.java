
/**
 * @file BaseMetaMapper.java
 * @module shared/mapper/meta
 *
 * Mapper для BaseMeta ⇄ Document.
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
import com.spendi.shared.model.meta.BaseMeta;
import com.spendi.shared.model.meta.MetaFields;

public class BaseMetaMapper extends BaseMapper<BaseMeta> implements DocMapper<BaseMeta> {
	private static final BaseMetaMapper INSTANCE = new BaseMetaMapper();

	private BaseMetaMapper() {
		super(BaseMetaMapper.class.getSimpleName(), BaseMeta.class, MetaFields.META);
	}

	public static BaseMetaMapper getInstance() {
		return INSTANCE;
	}

	public Document toDocument(BaseMeta meta) {
		if (meta == null)
			return null;
		Document d = new Document();
		putIfNotNull(d, MetaFields.CREATED_AT, meta.getCreatedAt());
		putIfNotNull(d, MetaFields.CREATED_BY, meta.getCreatedBy());
		return d;
	}

	public BaseMeta toEntity(Document d) {
		if (d == null)
			return null;

		return BaseMeta.builder() // используем builder для создания экземпляра BaseMeta
				// получаем Instant из документа или null, если значение отсутствует
				.createdAt(InstantUtils.getInstantOrNull(d.get(MetaFields.CREATED_AT)))
				// читаем ObjectId из документа или null, если значение отсутствует
				.createdBy(readObjectId(d.get(MetaFields.CREATED_BY)))
				// собираем экземпляр BaseMeta
				.build();
	}

}