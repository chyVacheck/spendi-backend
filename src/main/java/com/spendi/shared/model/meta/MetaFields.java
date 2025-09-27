
/**
 * @file MetaFields.java
 * @module shared/model/meta
 * @description Common BSON field keys for metadata objects (BaseMeta, AuditMeta, LifecycleMeta).
 *
 * @author Dmytro Shakh
 */

package com.spendi.shared.model.meta;

/**
 * Common BSON field keys for metadata objects (BaseMeta, AuditMeta, LifecycleMeta).
 */
public final class MetaFields {
	private MetaFields() {}

	public static final String META = "meta";

	public static final String CREATED_AT = "createdAt";
	public static final String CREATED_BY = "createdBy";

	public static final String UPDATED_AT = "updatedAt";
	public static final String UPDATED_BY = "updatedBy";

	public static final String DELETED_AT = "deletedAt";
	public static final String DELETED_BY = "deletedBy";
}