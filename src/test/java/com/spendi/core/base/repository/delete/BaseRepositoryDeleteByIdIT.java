
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
 * IT для:
 * - deleteById(ObjectId)
 */
class BaseRepositoryDeleteByIdIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);
	}

	@Test
	void delete_existing_id_returns_id_and_removes_doc() {
		// given
		ObjectId id = new ObjectId();
		Document doc = new Document("_id", id).append("name", "to-delete");
		repo.insertOneDoc(doc);

		assertThat(repo.count(Map.of())).isEqualTo(1);

		// when
		Optional<String> deleted = repo.deleteById(id);

		// then
		assertThat(deleted).isPresent();
		assertThat(deleted.get()).isEqualTo(id.toHexString());
		assertThat(repo.count(Map.of())).isZero();
	}

	@Test
	void delete_non_existing_id_returns_empty() {
		// when
		Optional<String> deleted = repo.deleteById(new ObjectId());

		// then
		assertThat(deleted).isEmpty();
	}

	@Test
	void delete_auto_generated_id_works() {
		// given
		Document doc = new Document("name", "auto-id");
		repo.insertOneDoc(doc);

		ObjectId generatedId = repo.findOneDoc("name", "auto-id").get().getObjectId("_id");

		// when
		Optional<String> deleted = repo.deleteById(generatedId);

		// then
		assertThat(deleted).isPresent();
		assertThat(deleted.get()).isEqualTo(generatedId.toHexString());
		assertThat(repo.findById(generatedId)).isEmpty();
	}

	// ------ тестовая обвязка (такая же, как в прошлых тестах) ------

	static class TestEntity {
		private ObjectId id;
		private String name;

		public ObjectId getId() {
			return id;
		}

		public void setId(ObjectId id) {
			this.id = id;
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
			t.setName(d.getString("name"));
			return t;
		}

		@Override
		public Document toDocument(TestEntity t) {
			Document d = new Document();
			if (t.getId() != null)
				d.put("_id", t.getId());
			d.put("name", t.getName());
			return d;
		}
	}

	/** Тестовый репозиторий поверх BaseRepository. */
	static class TestRepository extends BaseRepository<TestEntity> {
		private static final String COLL = "it_test_entities_delete";

		public TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, new TestMapper());
		}
	}
}