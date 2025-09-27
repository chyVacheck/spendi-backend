// com/spendi/modules/user/UserMapperTest.java

package com.spendi.modules.user;

/**
 * ! lib imports
 */
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.bson.types.ObjectId;
import org.bson.Document;

/**
 * ! java imports
 */
import java.time.Instant;
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.modules.user.model.*;

class UserMapperTest {
	private final UserMapper mapper = UserMapper.getInstance();

	@Test
	void toEntity_minimal_ok() {
		ObjectId id = new ObjectId();
		Document doc = new Document().append("_id", id)
				.append("profile", new Document().append("email", "john@doe.dev"))
				.append("security", new Document().append("passwordHash", "phash"))
				.append("finance", new Document().append("paymentMethodIds", List.of())).append("system", new Document()
						.append("lastLoginAt", null).append("meta", new Document().append("createdAt", Instant.now())));

		UserEntity e = mapper.toEntity(doc);
		assertThat(e.getId()).isEqualTo(id);
		assertThat(e.getProfile().getEmail()).isEqualTo("john@doe.dev");
		assertThat(e.getSecurity().getPasswordHash()).isEqualTo("phash");
		assertThat(e.getFinance().getPaymentMethodIds()).isEmpty();
		assertThat(e.getSystem().getMeta().getCreatedAt()).isNotNull();
	}

	@Test
	void roundTrip_ok() {
		UserEntity src = new UserEntity();
		src.setId(new ObjectId());
		var profile = new UserProfile();
		profile.setEmail("a@b.c");
		src.setProfile(profile);
		var sec = new UserSecurity();
		sec.setPasswordHash("h");
		src.setSecurity(sec);
		var fin = new UserFinance();
		fin.setPaymentMethodIds(java.util.Set.of());
		src.setFinance(fin);
		var sys = new UserSystem();
		sys.setMeta(com.spendi.shared.model.meta.LifecycleMeta.builder().createdAt(Instant.now()).build());
		src.setSystem(sys);

		Document d = mapper.toDocument(src);
		UserEntity back = mapper.toEntity(d);

		// сравниваем ключевые поля
		assertThat(back.getId()).isEqualTo(src.getId());
		assertThat(back.getProfile().getEmail()).isEqualTo("a@b.c");
		assertThat(back.getSecurity().getPasswordHash()).isEqualTo("h");
		assertThat(back.getSystem().getMeta().getCreatedAt()).isNotNull();
	}
}