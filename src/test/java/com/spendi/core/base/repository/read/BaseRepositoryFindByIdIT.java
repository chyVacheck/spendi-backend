
package com.spendi.core.base.repository.read;

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
import java.util.List;
import java.util.Optional;

/**
 * ! my imports
 */
import com.spendi.core.types.DocMapper;
import com.spendi.testutil.RealMongoTest;
import com.spendi.core.base.BaseRepository;

/**
 * IT для:
 * - findDocById(ObjectId id)
 * - findById(ObjectId id)
 */
class BaseRepositoryFindByIdIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);
	}

	@Test
	void findDocById_found_returns_document() {
		// given
		TestEntity a = e("A", 1);
		TestEntity b = e("B", 2);
		repo.insertManyEntities(List.of(a, b));

		// when
		Optional<Document> found = repo.findDocById(a.getId());

		// then
		assertThat(found).isPresent();
		Document d = found.get();
		assertThat(d.getObjectId("_id")).isEqualTo(a.getId());
		assertThat(d.getString("name")).isEqualTo("A");
		assertThat(((Number) d.get("number")).intValue()).isEqualTo(1);
	}

	@Test
	void findDocById_not_found_returns_empty() {
		// given
		repo.insertManyEntities(List.of(e("A", 1)));
		ObjectId missing = new ObjectId(); // другой id

		// when / then
		assertThat(repo.findDocById(missing)).isEmpty();
	}

	@Test
	void findById_found_maps_to_entity() {
		// given
		TestEntity a = e("A", 1);
		repo.insertManyEntities(List.of(a));

		// when
		Optional<TestEntity> found = repo.findById(a.getId());

		// then
		assertThat(found).isPresent();
		TestEntity t = found.get();
		assertThat(t.getId()).isEqualTo(a.getId());
		assertThat(t.getName()).isEqualTo("A");
		assertThat(t.getNumber()).isEqualTo(1);
	}

	@Test
	void findById_not_found_returns_empty() {
		// given
		repo.insertManyEntities(List.of(e("A", 1)));
		ObjectId missing = new ObjectId();

		// when / then
		assertThat(repo.findById(missing)).isEmpty();
	}

	// ---------------- тестовая обвязка ----------------

	private static TestEntity e(String name, int number) {
		TestEntity t = new TestEntity();
		t.setId(new ObjectId());
		t.setName(name);
		t.setNumber(number);
		return t;
	}

	static class TestEntity {
		private ObjectId id;
		private String name;
		private Integer number;

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

		public Integer getNumber() {
			return number;
		}

		public void setNumber(Integer number) {
			this.number = number;
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
			Object num = d.get("number");
			t.setNumber(num instanceof Number n ? n.intValue() : null);
			return t;
		}

		@Override
		public Document toDocument(TestEntity t) {
			Document d = new Document();
			if (t.getId() != null)
				d.put("_id", t.getId());
			d.put("name", t.getName());
			d.put("number", t.getNumber());
			return d;
		}
	}

	/** Тестовый репозиторий поверх BaseRepository. */
	static class TestRepository extends BaseRepository<TestEntity> {
		private static final String COLL = "it_test_entities_findById";
		private static final TestMapper MAPPER = new TestMapper();

		public TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, MAPPER);
		}
	}
}