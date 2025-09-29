
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
 * ! jva imports
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
 * Интеграционные тесты для BaseRepository.count(...)
 */
class BaseRepositoryCountIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);
	}

	// n из n: пустой фильтр должен посчитать все документы
	@Test
	void count_all_returns_n_of_n() {
		repo.insertManyEntities(List.of(e("A", 1), e("B", 2), e("C", 3)));

		long n = repo.count(Map.of()); // пустой фильтр -> все
		assertThat(n).isEqualTo(3L);
	}

	// 0 совпадений по eq-фильтру
	@Test
	void count_returns_zero_when_no_match() {
		repo.insertManyEntities(List.of(e("A", 1), e("B", 2)));

		long z1 = repo.count("name", "NOPE");
		long z2 = repo.count(Map.of("name", "NOPE"));
		assertThat(z1).isZero();
		assertThat(z2).isZero();
	}

	// m из m по фильтру (равенство) + пример с оператором через Map
	@Test
	void count_returns_m_of_m_with_filters() {
		repo.insertManyEntities(List.of(e("A", 1), e("A", 2), e("B", 3), e("A", 4)));

		// eq-фильтр
		long onlyA = repo.count("name", "A");
		assertThat(onlyA).isEqualTo(3L);

		// операторный фильтр через Map: number >= 3
		Map<String, Object> gte = new HashMap<>();
		gte.put("number", new Document("$gte", 3));
		long ge3 = repo.count(gte);
		assertThat(ge3).isEqualTo(2L); // (3 и 4)
	}

	// -------------------- тестовая обвязка --------------------

	/** Создание тестовой сущности. */
	private static TestEntity e(String name, int number) {
		TestEntity t = new TestEntity();
		t.setId(new ObjectId());
		t.setName(name);
		t.setNumber(number);
		return t;
	}

	/** Минимальная сущность для теста. */
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

	/** Простой маппер Document <-> TestEntity. */
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
		private static final String COLL = "it_test_entities";
		private static final TestMapper MAPPER = new TestMapper();

		public TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, MAPPER);
		}
	}
}