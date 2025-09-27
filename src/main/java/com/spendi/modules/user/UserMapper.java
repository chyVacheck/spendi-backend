
/**
 * @file UserMapper.java
 * @module modules/user
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.user;

/**
 * ! lib imports
 */
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * ! java imports
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMapper;
import com.spendi.core.types.DocMapper;
import com.spendi.modules.auth.dto.RegisterDto;
import com.spendi.modules.user.cmd.UserCreateCmd;
import com.spendi.modules.user.model.UserEntity;
import com.spendi.modules.user.model.UserFinance;
import com.spendi.modules.user.model.UserProfile;
import com.spendi.modules.user.model.UserSecurity;
import com.spendi.modules.user.model.UserSystem;
import com.spendi.shared.mapper.meta.LifecycleMetaMapper;
import com.spendi.shared.model.meta.LifecycleMeta;

public class UserMapper extends BaseMapper<UserEntity> implements DocMapper<UserEntity> {

	private final static UserMapper mapper = new UserMapper();
	private static final LifecycleMetaMapper META = LifecycleMetaMapper.getInstance();

	public UserMapper() {
		super(UserMapper.class.getSimpleName(), UserEntity.class, "users");
	}

	public static UserMapper getInstance() {
		return mapper;
	}

	/**
	 * ? === === === MAPPING === === ===
	 */

	@Override
	public UserEntity toEntity(Document doc) {
		if (doc == null)
			return null;

		// id
		ObjectId id = reqObjectId(doc, "_id", null);

		// profile (required block)
		Document pDoc = reqSubDoc(doc, "profile", id);
		UserProfile profile = new UserProfile();
		profile.setEmail(reqString(pDoc, "email", id));
		profile.setFirstName(optString(pDoc, "firstName").orElse(null));
		profile.setLastName(optString(pDoc, "lastName").orElse(null));
		profile.setAvatarFileId(optObjectId(pDoc, "avatarFileId").orElse(null));

		// security (required block)
		Document sDoc = reqSubDoc(doc, "security", id);
		UserSecurity security = new UserSecurity();
		security.setPasswordHash(reqString(sDoc, "passwordHash", id));

		// finance (required block)
		Document fDoc = reqSubDoc(doc, "finance", id);
		UserFinance finance = new UserFinance();
		finance.setDefaultAccountId(optObjectId(fDoc, "defaultAccountId").orElse(null));

		// paymentMethodIds: допускаем отсутствие поля → пустой Set
		List<ObjectId> pmList = fDoc.containsKey("paymentMethodIds") ? reqObjectIdList(fDoc, "paymentMethodIds", id)
				: List.of();
		finance.setPaymentMethodIds(Set.copyOf(pmList));

		// system (required block)
		Document sysDoc = reqSubDoc(doc, "system", id);
		UserSystem system = new UserSystem();
		system.setLastLoginAt(optInstant(sysDoc, "lastLoginAt").orElse(null));

		// system.meta (required sub-block) — через LifecycleMetaMapper
		Document mDoc = reqSubDoc(sysDoc, "meta", id, "system.meta");
		LifecycleMeta meta = META.toEntity(mDoc);
		system.setMeta(meta);

		// assemble
		UserEntity e = new UserEntity();
		e.setId(id);
		e.setProfile(profile);
		e.setSecurity(security);
		e.setFinance(finance);
		e.setSystem(system);

		return e;
	}

	@Override
	public Document toDocument(UserEntity e) {
		Document doc = new Document();

		doc.put("_id", e.getId());

		// profile
		{
			UserProfile p = e.getProfile();
			Document d = new Document();
			if (p != null) {
				d.put("email", p.getEmail());
				d.put("firstName", p.getFirstName());
				d.put("lastName", p.getLastName());
				d.put("avatarFileId", p.getAvatarFileId());
			}
			doc.put("profile", d);
		}

		// security
		{
			UserSecurity s = e.getSecurity();
			Document d = new Document();
			if (s != null) {
				d.put("passwordHash", s.getPasswordHash());
			}
			doc.put("security", d);
		}

		// finance
		{
			UserFinance f = e.getFinance();
			Document d = new Document();
			if (f != null) {
				d.put("defaultAccountId", f.getDefaultAccountId());
				// в базе храним как список ObjectId
				List<ObjectId> pms = f.getPaymentMethodIds() == null ? List.of()
						: new ArrayList<>(f.getPaymentMethodIds());
				d.put("paymentMethodIds", pms);
			}
			doc.put("finance", d);
		}

		// system (meta + lastLoginAt)
		{
			UserSystem s = e.getSystem();
			Document d = new Document();
			if (s != null) {
				putIfNotNull(d, "lastLoginAt", s.getLastLoginAt());

				// meta через LifecycleMetaMapper
				LifecycleMeta meta = s.getMeta();
				Document m = META.toDocument(meta); // null → null (поле meta можно вовсе не класть, если нужно)
				if (m != null) {
					d.put("meta", m);
				}
			}
			doc.put("system", d);
		}

		return doc;
	}

	public UserCreateCmd toCmd(RegisterDto dto) {
		UserCreateCmd cmd = new UserCreateCmd();

		UserCreateCmd.ProfileBlock profile = new UserCreateCmd.ProfileBlock();

		profile.setEmail(dto.getEmail());
		profile.setFirstName(dto.getFirstName());
		profile.setLastName(dto.getLastName());

		UserCreateCmd.SecurityBlock security = new UserCreateCmd.SecurityBlock();

		security.setPassword(dto.getPassword());

		cmd.setProfile(profile);
		cmd.setSecurity(security);

		return cmd;
	}
}
