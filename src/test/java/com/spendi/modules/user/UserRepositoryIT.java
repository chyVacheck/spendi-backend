// com/spendi/modules/user/UserRepositoryIT.java

package com.spendi.modules.user;

/**
 * ! lib imports
 */
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ! java imports
 */
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ! my imports
 */
import com.mongodb.MongoWriteException;
import com.mongodb.ErrorCategory;
import com.spendi.modules.user.model.UserEntity;
import com.spendi.modules.user.model.UserFinance;
import com.spendi.modules.user.model.UserProfile;
import com.spendi.modules.user.model.UserSecurity;
import com.spendi.modules.user.model.UserSystem;
import com.spendi.shared.model.meta.LifecycleMeta;
import com.spendi.testutil.RealMongoTest;

class UserRepositoryIT extends RealMongoTest {

	private UserRepository repo;

	@BeforeEach
	void cleanAndInit() {
		clean();
		// конструктор уже принимает маппер внутри
		this.repo = new UserRepository(db);
	}

	@AfterEach
	void clean() {
		db.drop();
	}

	@Test
	void insert_and_findByEmail_and_findById_roundtrip_ok() {
		// given
		UserEntity toInsert = sampleUser("alice@example.com");
		repo.insertOne(toInsert);

		// when
		var byEmail = repo.findByEmail("alice@example.com");
		var byId = repo.findById(toInsert.getId());

		// then
		assertThat(byEmail).isPresent();
		assertThat(byId).isPresent();

		UserEntity u1 = byEmail.get();
		UserEntity u2 = byId.get();

		assertThat(u1.getId()).isEqualTo(toInsert.getId());
		assertThat(u1.getProfile().getEmail()).isEqualTo("alice@example.com");
		assertThat(u1.getProfile().getFirstName()).isEqualTo("Alice");
		assertThat(u1.getSecurity().getPasswordHash()).isEqualTo("hashed:password");
		assertThat(u1.getFinance().getPaymentMethodIds())
				.containsExactlyInAnyOrderElementsOf(toInsert.getFinance().getPaymentMethodIds());
		assertThat(u1.getSystem().getMeta().getCreatedAt()).isNotNull();
		assertThat(u1.getSystem().getLastLoginAt()).isNull();

		// доп. проверка по findById
		assertThat(u2.getProfile().getEmail()).isEqualTo("alice@example.com");
	}

	@Test
	void existsByEmail_true_after_insert() {
		// given
		repo.insertOne(sampleUser("bob@example.com"));

		// when / then
		assertThat(repo.existsByEmail("bob@example.com")).isTrue();
		assertThat(repo.existsByEmail("none@example.com")).isFalse();
	}

	@Test
	void unique_email_violation_fails_on_second_insert() {
		// given
		repo.insertOne(sampleUser("dup@example.com"));

		// when / then
		MongoWriteException ex = assertThrows(MongoWriteException.class, () -> {
			repo.insertOne(sampleUser("dup@example.com"));
		});
		// Убедимся, что это действительно конфликт уникального ключа
		assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.DUPLICATE_KEY);
	}

	@Test
	void findMany_pagination_basic() {
		// given
		repo.insertManyEntities(
				List.of(sampleUser("p1@example.com"), sampleUser("p2@example.com"), sampleUser("p3@example.com")));

		// when
		var page1 = repo.findMany("profile.email", "p1@example.com", 1, 10);
		var pageAll = repo.findMany(java.util.Map.of(), 1, 2); // первые 2

		// then
		assertThat(page1).hasSize(1);
		assertThat(page1.get(0).getProfile().getEmail()).isEqualTo("p1@example.com");

		assertThat(pageAll).hasSize(2);
	}

	@Test
	void deleteById_returns_id_when_deleted() {
		// given
		UserEntity u = sampleUser("delme@example.com");
		repo.insertOne(u);

		// when
		var deleted = repo.deleteById(u.getId());

		// then
		assertThat(deleted).isPresent();
		assertThat(deleted.get()).isEqualTo(u.getId().toHexString());
		assertThat(repo.findById(u.getId())).isEmpty();
	}

	@Test
	void count_and_exists_with_map_filters() {
		// given
		repo.insertManyEntities(
				List.of(sampleUser("m1@example.com"), sampleUser("m2@example.com"), sampleUser("m3@example.com")));

		// when / then
		assertThat(repo.count(Map.of())).isEqualTo(3);
		assertThat(repo.count("profile.email", "m2@example.com")).isEqualTo(1);
		assertThat(repo.exists(Map.of("profile.email", "m3@example.com"))).isTrue();
		assertThat(repo.exists(Map.of("profile.email", "nope@example.com"))).isFalse();
	}

	@Test
	void deleteOne_by_filter_removes_single_and_returns_id() {
		// given
		var u1 = sampleUser("d1@example.com");
		var u2 = sampleUser("d2@example.com");
		repo.insertManyEntities(List.of(u1, u2));

		// when
		var removed = repo.deleteOne(Map.of("profile.email", "d2@example.com"));

		// then
		assertThat(removed).isPresent();
		assertThat(removed.get()).isEqualTo(u2.getId().toHexString());
		assertThat(repo.findByEmail("d2@example.com")).isEmpty();
		assertThat(repo.findByEmail("d1@example.com")).isPresent();
	}

	@Test
	void deleteMany_by_value_removes_all_matches_and_returns_count() {
		// создадим три разных email и удалим по одному точному значению
		repo.insertManyEntities(
				List.of(sampleUser("z1@example.com"), sampleUser("z2@example.com"), sampleUser("z3@example.com")));
		// when
		long removed = repo.deleteMany("profile.email", "z2@example.com");
		// then
		assertThat(removed).isEqualTo(1);
		assertThat(repo.findByEmail("z2@example.com")).isEmpty();
		assertThat(repo.count(Map.of())).isEqualTo(2);
	}

	@Test
	void findMany_pagination_edges_normalize_page_and_limit() {
		// given: 3 пользователя
		repo.insertManyEntities(
				List.of(sampleUser("pg1@example.com"), sampleUser("pg2@example.com"), sampleUser("pg3@example.com")));

		// when: page=0 и limit=0 должны нормализоваться до 1
		var page0limit0 = repo.findMany(Map.of(), 0, 0); // ожидаем 1 элемент (limit->1)
		var pageNeg = repo.findMany(Map.of(), -5, 2); // ожидаем 2 элемента (page->1)
		var limitNeg = repo.findMany(Map.of(), 1, -10); // ожидаем 1 элемент (limit->1)

		// then
		assertThat(page0limit0).hasSize(1);
		assertThat(pageNeg).hasSize(2);
		assertThat(limitNeg).hasSize(1);
	}

	@Test
	void findOneDoc_and_findDocById_raw_documents() {
		// given
		var u = sampleUser("raw@example.com");
		repo.insertOne(u);

		// when
		var od = repo.findOneDoc("profile.email", "raw@example.com");
		assertThat(od).isPresent();

		var id = od.get().getObjectId("_id");
		var byIdDoc = repo.findDocById(id);

		// then
		assertThat(byIdDoc).isPresent();
		assertThat(byIdDoc.get().get("profile", Document.class).getString("email")).isEqualTo("raw@example.com");
	}

	@Test
	void toEntity_handles_absent_optional_fields_in_document() {
		// given: вручную вставим сырой документ с пропущенными необязательными полями
		ObjectId id = new ObjectId();
		Document doc = new Document("_id", id).append("profile", new Document().append("email", "min@example.com") // обязательный
		// firstName/lastName отсутствуют
		// avatarFileId отсутствует
		).append("security", new Document().append("passwordHash", "hashed:pw") // обязательный
		).append("finance", new Document()
		// defaultAccountId отсутствует
		// paymentMethodIds отсутствует
		).append("system", new Document()
				// lastLoginAt отсутствует (optional)
				.append("meta", new Document().append("createdAt", Instant.now())
				// updatedAt/updatedBy/deletedAt/deletedBy отсутствуют
				));

		// прямой insert сырого документа
		repo.insertOneDoc(doc);

		// when
		var loaded = repo.findById(id);

		// then
		assertThat(loaded).isPresent();
		UserEntity u = loaded.get();

		assertThat(u.getId()).isEqualTo(id);
		assertThat(u.getProfile().getEmail()).isEqualTo("min@example.com");
		assertThat(u.getProfile().getFirstName()).isNull();
		assertThat(u.getProfile().getLastName()).isNull();
		assertThat(u.getProfile().getAvatarFileId()).isNull();

		assertThat(u.getSecurity().getPasswordHash()).isEqualTo("hashed:pw");

		// finance defaults
		assertThat(u.getFinance().getDefaultAccountId()).isNull();
		assertThat(u.getFinance().getPaymentMethodIds()).isEmpty();

		// system/meta defaults
		assertThat(u.getSystem().getLastLoginAt()).isNull();
		assertThat(u.getSystem().getMeta().getCreatedAt()).isNotNull();
		assertThat(u.getSystem().getMeta().getUpdatedAt()).isNull();
		assertThat(u.getSystem().getMeta().getUpdatedBy()).isNull();
		assertThat(u.getSystem().getMeta().getDeletedAt()).isNull();
		assertThat(u.getSystem().getMeta().getDeletedBy()).isNull();
	}

	@Test
	void insertManyEntities_and_findMany_all() {
		// given
		var users = List.of(sampleUser("b1@example.com"), sampleUser("b2@example.com"), sampleUser("b3@example.com"),
				sampleUser("b4@example.com"));
		repo.insertManyEntities(users);

		// when
		var page1 = repo.findMany(Map.of(), 1, 3);
		var page2 = repo.findMany(Map.of(), 2, 3);

		// then
		assertThat(page1).hasSize(3);
		assertThat(page2).hasSize(1);
	}

	// ---------------- helpers ----------------

	private static UserEntity sampleUser(String email) {
		ObjectId id = new ObjectId();

		UserProfile profile = UserProfile.builder().email(email).firstName("Alice").lastName("Wonder").build();

		UserSecurity sec = UserSecurity.builder().passwordHash("hashed:password").build();

		UserFinance fin = UserFinance.builder().defaultAccountId(null)
				.paymentMethodIds(Set.of(new ObjectId(), new ObjectId())).build();

		LifecycleMeta meta = LifecycleMeta.builder().createdAt(Instant.now()).createdBy(null).updatedAt(null)
				.updatedBy(null).deletedAt(null).deletedBy(null).build();

		UserSystem sys = UserSystem.builder().meta(meta).lastLoginAt(null).build();

		return UserEntity.builder().id(id) // важно: твой toDocument кладёт _id как есть; генерим тут
				.profile(profile).security(sec).finance(fin).system(sys).build();
	}
}