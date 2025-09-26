
/**
 * @file EntityMetaMapper.java
 * @module shared/mapper
 * 
 * @author Dmytro Shakh
 */

package com.spendi.shared.mapper;

/**
 * ! lib imports
 */
import org.bson.Document;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMapper;
import com.spendi.core.utils.InstantUtils;
import com.spendi.shared.model.EntityMeta;

public class EntityMetaMapper extends BaseMapper {
	private final static EntityMetaMapper mapper = new EntityMetaMapper();

	public EntityMetaMapper() {
		super(EntityMetaMapper.class.getSimpleName());
	}

	public static EntityMetaMapper getInstance() {
		return mapper;
	}

	public Document toDocument(EntityMeta meta) {
		Document doc = new Document();
		doc.put("createdAt", meta.getCreatedAt());
		doc.put("createdBy", meta.getCreatedBy());
		doc.put("updatedAt", meta.getUpdatedAt());
		doc.put("updatedBy", meta.getUpdatedBy());
		doc.put("deletedAt", meta.getDeletedAt());
		return doc;
	}

	public EntityMeta fromDocument(Document dm) {
		EntityMeta meta = new EntityMeta();
		meta.setCreatedAt(InstantUtils.getInstantOrNull(dm.get("createdAt")));
		meta.setCreatedBy(dm.getObjectId("createdBy"));
		meta.setUpdatedAt(InstantUtils.getInstantOrNull(dm.get("updatedAt")));
		meta.setUpdatedBy(dm.getObjectId("createdBy"));

		meta.setDeletedAt(InstantUtils.getInstantOrNull(dm.get("deletedAt")));
		return meta;
	}
}
