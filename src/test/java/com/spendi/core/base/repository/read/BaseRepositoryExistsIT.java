
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.types.DocMapper;
import com.spendi.testutil.RealMongoTest;
import com.spendi.core.base.BaseRepository;

/**
 * Интеграционные тесты для BaseRepository.exists(...)
 */
class BaseRepositoryExistsIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);
	}

	// ---- exists(String, Object) ----

	@Test
	void exists_byKeyValue_true_when_one_match() {
		repo.insertManyEntities(List.of(e("A", 1), e("B", 2)));

		assertThat(repo.exists("name", "A")).isTrue();
	}

	@Test
	void exists_byKeyValue_true_when_many_match() {
		repo.insertManyEntities(List.of(e("A", 1), e("A", 2), e("B", 3)));

		assertThat(repo.exists("name", "A")).isTrue(); // ≥1
	}

	@Test
	void exists_byKeyValue_false_when_no_match() {
		repo.insertManyEntities(List.of(e("A", 1), e("B", 2)));

		assertThat(repo.exists("name", "NOPE")).isFalse();
	}

	@Test
	void exists_byKeyValue_false_when_type_mismatch() {
		repo.insertManyEntities(List.of(e("A", 1), e("B", 2)));

		// Ищем строку по числовому полю -> false
		assertThat(repo.exists("number", "2")).isFalse();
	}

	// ---- exists(Map<String, Object>) ----

	@Test
	void exists_byMap_true_single_condition() {
		repo.insertManyEntities(List.of(e("X", 10), e("Y", 20)));

		assertThat(repo.exists(Map.of("name", "X"))).isTrue();
	}

	@Test
	void exists_byMap_true_when_all_conditions_match_AND() {
		repo.insertManyEntities(List.of(e("X", 10), e("X", 11), e("Y", 10)));

		Map<String, Object> filter = Map.of("name", "X", "number", 11);
		assertThat(repo.exists(filter)).isTrue();
	}

	@Test
	void exists_byMap_false_when_AND_conditions_not_all_match() {
		repo.insertManyEntities(List.of(e("X", 10), e("X", 11), e("Y", 10)));

		Map<String, Object> filter = Map.of("name", "X", "number", 999);
		assertThat(repo.exists(filter)).isFalse();
	}

	@Test
	void exists_byMap_with_operator_gte() {
		repo.insertManyEntities(List.of(e("A", 1), e("A", 2), e("A", 3)));

		Map<String, Object> filter = new HashMap<>();
		filter.put("number", new Document("$gte", 3));
		assertThat(repo.exists(filter)).isTrue();

		filter.put("number", new Document("$gte", 99));
		assertThat(repo.exists(filter)).isFalse();
	}

	@Test
	void exists_byMap_empty_filter_false_on_empty_collection_true_after_insert() {
		// пустая коллекция
		assertThat(repo.exists(Map.of())).isFalse();

		// после вставки хотя бы одного документа
		repo.insertManyEntities(List.of(e("Z", 5)));
		assertThat(repo.exists(Map.of())).isTrue();
	}

	@Test
	void exists_byMap_false_on_unknown_field() {
		repo.insertManyEntities(List.of(e("A", 1)));

		assertThat(repo.exists(Map.of("unknown", "value"))).isFalse();
	}

	// --------------- тестовая обвязка (та же, что в CountIT) ---------------

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

	static class TestRepository extends BaseRepository<TestEntity> {
		private static final String COLL = "it_test_entities_exists";
		private static final TestMapper MAPPER = new TestMapper();

		public TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, MAPPER);
		}
	}
}