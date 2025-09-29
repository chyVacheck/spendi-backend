
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
import java.util.Optional;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.core.types.DocMapper;
import com.spendi.testutil.RealMongoTest;

/**
 * Интеграционные тесты для:
 * - deleteOne(Map<String,Object>)
 * - deleteOne(String, Object)
 */
class BaseRepositoryDeleteOneIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);

		// seed
		repo.insertOneDoc(new Document("_id", new ObjectId()).append("group", "A").append("name", "first"));
		repo.insertOneDoc(new Document("_id", new ObjectId()).append("group", "A").append("name", "second"));
		repo.insertOneDoc(new Document("_id", new ObjectId()).append("group", "B").append("name", "third"));
	}

	// ---------- deleteOne(Map) ----------

	@Test
	void deleteOne_map_existing_returns_id_and_removes_one() {
		long before = repo.count(Map.of());
		// when
		Optional<String> deleted = repo.deleteOne(Map.of("group", "A"));

		// then
		assertThat(deleted).isPresent();
		assertThat(repo.count(Map.of())).isEqualTo(before - 1);

		// убедимся, что остался ещё один из группы A и один из B
		var remainingA = repo.findManyDocs("group", "A", 1, 10);
		var remainingB = repo.findManyDocs("group", "B", 1, 10);
		assertThat(remainingA).hasSize(1);
		assertThat(remainingB).hasSize(1);
	}

	@Test
	void deleteOne_map_not_existing_returns_empty_and_no_changes() {
		long before = repo.count(Map.of());
		Optional<String> deleted = repo.deleteOne(Map.of("group", "Z"));

		assertThat(deleted).isEmpty();
		assertThat(repo.count(Map.of())).isEqualTo(before);
	}

	@Test
	void deleteOne_map_with_multiple_matches_deletes_first_only() {
		// подготовим ещё один документ в группе B, чтобы было 2 совпадения
		repo.insertOneDoc(new Document("group", "B").append("name", "fourth"));

		long before = repo.count(Map.of());
		var bBefore = repo.findManyDocs("group", "B", 1, 10);
		assertThat(bBefore).hasSize(2);

		Optional<String> deleted = repo.deleteOne(Map.of("group", "B"));

		assertThat(deleted).isPresent();
		assertThat(repo.count(Map.of())).isEqualTo(before - 1);

		var bAfter = repo.findManyDocs("group", "B", 1, 10);
		assertThat(bAfter).hasSize(1);
		// возвращённый id принадлежал одному из удаляемых B
		assertThat(bBefore.stream().map(d -> d.getObjectId("_id").toHexString())).contains(deleted.get());
	}

	// ---------- deleteOne(key, value) ----------

	@Test
	void deleteOne_kv_existing_returns_id_and_removes_one() {
		long before = repo.count(Map.of());
		Optional<String> deleted = repo.deleteOne("group", "A");

		assertThat(deleted).isPresent();
		assertThat(repo.count(Map.of())).isEqualTo(before - 1);

		var remainingA = repo.findManyDocs("group", "A", 1, 10);
		assertThat(remainingA).hasSize(1);
	}

	@Test
	void deleteOne_kv_not_existing_returns_empty_and_no_changes() {
		long before = repo.count(Map.of());
		Optional<String> deleted = repo.deleteOne("name", "nope");

		assertThat(deleted).isEmpty();
		assertThat(repo.count(Map.of())).isEqualTo(before);
	}

	@Test
	void deleteOne_kv_with_multiple_matches_deletes_first_only() {
		// добавим дубликат имени "third"
		repo.insertOneDoc(new Document("group", "B").append("name", "third"));

		var matchesBefore = repo.findManyDocs(Map.of("name", "third"), 1, 10);
		assertThat(matchesBefore).hasSize(2);

		Optional<String> deleted = repo.deleteOne("name", "third");
		assertThat(deleted).isPresent();

		var matchesAfter = repo.findManyDocs(Map.of("name", "third"), 1, 10);
		assertThat(matchesAfter).hasSize(1);

		// удалённый id был одним из совпадений до удаления
		assertThat(matchesBefore.stream().map(d -> d.getObjectId("_id").toHexString())).contains(deleted.get());
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
		private static final String COLL = "it_test_entities_delete_one";

		TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, new TestMapper());
		}
	}

}