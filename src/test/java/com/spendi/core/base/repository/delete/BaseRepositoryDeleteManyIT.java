
package com.spendi.core.base.repository.delete;

/**
 * ! lib imports
 */
import static org.assertj.core.api.Assertions.assertThat;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.core.types.DocMapper;
import com.spendi.testutil.RealMongoTest;

/**
 * Интеграционные тесты для:
 * - deleteMany(Map<String,Object>)
 * - deleteMany(String, Object)
 */
class BaseRepositoryDeleteManyIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);

		// seed: A(2), B(2), C(1)
		repo.insertOneDoc(new Document("_id", new ObjectId()).append("group", "A").append("name", "a1"));
		repo.insertOneDoc(new Document("_id", new ObjectId()).append("group", "A").append("name", "a2"));

		repo.insertOneDoc(new Document("_id", new ObjectId()).append("group", "B").append("name", "b1"));
		repo.insertOneDoc(new Document("_id", new ObjectId()).append("group", "B").append("name", "b2"));

		repo.insertOneDoc(new Document("_id", new ObjectId()).append("group", "C").append("name", "c1"));
	}

	// -------- deleteMany(Map) --------

	@Test
	void deleteMany_map_deletes_all_matching_and_returns_count() {
		long before = repo.count(Map.of());
		assertThat(before).isEqualTo(5);

		long deleted = repo.deleteMany(Map.of("group", "B"));
		assertThat(deleted).isEqualTo(2);

		// осталось 3
		assertThat(repo.count(Map.of())).isEqualTo(3);

		// и повторный вызов удаляет 0
		long deletedAgain = repo.deleteMany(Map.of("group", "B"));
		assertThat(deletedAgain).isEqualTo(0);
	}

	@Test
	void deleteMany_map_with_no_matches_returns_zero_and_changes_nothing() {
		long before = repo.count(Map.of());

		long deleted = repo.deleteMany(Map.of("group", "Z"));
		assertThat(deleted).isEqualTo(0);
		assertThat(repo.count(Map.of())).isEqualTo(before);
	}

	@Test
	void deleteMany_map_delete_all_documents_case() {
		// удалим всё по условию, которое совпадает со всеми (например, name exists)
		long deleted = repo.deleteMany(Map.of()); // пустой фильтр — удалит всё
		assertThat(deleted).isEqualTo(5);
		assertThat(repo.count(Map.of())).isZero();
	}

	// -------- deleteMany(key, value) --------

	@Test
	void deleteMany_kv_deletes_all_matching_and_returns_count() {
		long before = repo.count(Map.of());
		assertThat(before).isEqualTo(5);

		long deleted = repo.deleteMany("group", "A");
		assertThat(deleted).isEqualTo(2);

		assertThat(repo.count(Map.of())).isEqualTo(3);

		// повторная попытка — ничего не осталось из A
		long deletedAgain = repo.deleteMany("group", "A");
		assertThat(deletedAgain).isEqualTo(0);
	}

	@Test
	void deleteMany_kv_no_matches_returns_zero() {
		long before = repo.count(Map.of());
		long deleted = repo.deleteMany("group", "ZZ");

		assertThat(deleted).isEqualTo(0);
		assertThat(repo.count(Map.of())).isEqualTo(before);
	}

	// ------ тестовая обвязка (такая же, как в прошлых тестах) ------

	static class TestEntity {
		private ObjectId id;
		private String group;
		private String name;

		public ObjectId getId() {
			return id;
		}

		public void setId(ObjectId id) {
			this.id = id;
		}

		public String getGroup() {
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	static class TestMapper implements DocMapper<TestEntity> {
		@Override
		public TestEntity toEntity(Document d) {
			if (d == null)
				return null;
			TestEntity t = new TestEntity();
			t.setId(d.getObjectId("_id"));
			t.setGroup(d.getString("group"));
			t.setName(d.getString("name"));
			return t;
		}

		@Override
		public Document toDocument(TestEntity t) {
			Document d = new Document();
			if (t.getId() != null)
				d.put("_id", t.getId());
			d.put("group", t.getGroup());
			d.put("name", t.getName());
			return d;
		}
	}

	/** Тестовый репозиторий поверх BaseRepository. */
	static class TestRepository extends BaseRepository<TestEntity> {
		private static final String COLL = "it_test_entities_delete_many";

		TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, new TestMapper());
		}
	}
}