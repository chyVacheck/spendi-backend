
package com.spendi.core.base.repository.read;

/**
 * ! lib imports
 */
import static org.assertj.core.api.Assertions.assertThat;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ! java imports
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ! my imports
 */
import com.mongodb.client.MongoDatabase;
import com.spendi.core.base.BaseRepository;
import com.spendi.core.types.DocMapper;
import com.spendi.testutil.RealMongoTest;

/**
 * IT для:
 * - findOneDoc(Map<String,Object>)
 * - findOneDoc(String, Object)
 * - findOne(Map<String,Object>)
 * - findOne(String, Object)
 */
class BaseRepositoryFindOneIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);
	}

	// ---------- findOneDoc(Map) ----------

	@Test
	void findOneDoc_byMap_found_returns_first_document() {
		// given: два документа с одинаковым "group" -> должны получить первый по естественному порядку вставки
		TestEntity t1 = e("first", 10, "G1");
		TestEntity t2 = e("second", 20, "G1");
		TestEntity t3 = e("third", 30, "G2");
		repo.insertManyEntities(List.of(t1, t2, t3));

		Map<String, Object> filter = new HashMap<>();
		filter.put("group", "G1");

		// when
		Optional<Document> docOpt = repo.findOneDoc(filter);

		// then
		assertThat(docOpt).isPresent();
		Document d = docOpt.get();
		assertThat(d.getObjectId("_id")).isEqualTo(t1.getId());
		assertThat(d.getString("name")).isEqualTo("first");
	}

	@Test
	void findOneDoc_byMap_not_found_returns_empty() {
		repo.insertManyEntities(List.of(e("one", 1, "A"), e("two", 2, "B")));
		Map<String, Object> filter = Map.of("group", "Z");
		assertThat(repo.findOneDoc(filter)).isEmpty();
	}

	// ---------- findOneDoc(key,value) ----------

	@Test
	void findOneDoc_byKeyValue_found_returns_document() {
		TestEntity t1 = e("alpha", 1, "K");
		TestEntity t2 = e("beta", 2, "K");
		repo.insertManyEntities(List.of(t1, t2));

		Optional<Document> docOpt = repo.findOneDoc("group", "K");
		assertThat(docOpt).isPresent();
		Document d = docOpt.get();
		assertThat(d.getObjectId("_id")).isEqualTo(t1.getId());
		assertThat(d.getString("name")).isEqualTo("alpha");
	}

	@Test
	void findOneDoc_byKeyValue_not_found_returns_empty() {
		repo.insertManyEntities(List.of(e("alpha", 1, "K")));
		assertThat(repo.findOneDoc("group", "MISS")).isEmpty();
	}

	// ---------- findOne(Map) ----------

	@Test
	void findOne_byMap_found_maps_to_entity() {
		TestEntity t1 = e("A", 100, "X");
		TestEntity t2 = e("B", 200, "X");
		repo.insertManyEntities(List.of(t1, t2));

		Optional<TestEntity> res = repo.findOne(Map.of("group", "X"));
		assertThat(res).isPresent();

		TestEntity got = res.get();
		assertThat(got.getId()).isEqualTo(t1.getId());
		assertThat(got.getName()).isEqualTo("A");
		assertThat(got.getNumber()).isEqualTo(100);
		assertThat(got.getGroup()).isEqualTo("X");
	}

	@Test
	void findOne_byMap_not_found_returns_empty() {
		repo.insertManyEntities(List.of(e("A", 1, "X")));
		assertThat(repo.findOne(Map.of("group", "Z"))).isEmpty();
	}

	// ---------- findOne(key,value) ----------

	@Test
	void findOne_byKeyValue_found_maps_to_entity() {
		TestEntity t1 = e("left", 7, "G7");
		TestEntity t2 = e("right", 8, "G7");
		repo.insertManyEntities(List.of(t1, t2));

		Optional<TestEntity> res = repo.findOne("group", "G7");
		assertThat(res).isPresent();

		TestEntity got = res.get();
		assertThat(got.getId()).isEqualTo(t1.getId());
		assertThat(got.getName()).isEqualTo("left");
		assertThat(got.getNumber()).isEqualTo(7);
		assertThat(got.getGroup()).isEqualTo("G7");
	}

	@Test
	void findOne_byKeyValue_not_found_returns_empty() {
		repo.insertManyEntities(List.of(e("only", 1, "ONE")));
		assertThat(repo.findOne("group", "MISS")).isEmpty();
	}

	// ---------------- тестовая обвязка ----------------

	private static TestEntity e(String name, int number, String group) {
		TestEntity t = new TestEntity();
		t.setId(new ObjectId());
		t.setName(name);
		t.setNumber(number);
		t.setGroup(group);
		return t;
	}

	static class TestEntity {
		private ObjectId id;
		private String name;
		private Integer number;
		private String group;

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

		public String getGroup() {
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
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
			t.setGroup(d.getString("group"));
			return t;
		}

		@Override
		public Document toDocument(TestEntity t) {
			Document d = new Document();
			if (t.getId() != null)
				d.put("_id", t.getId());
			d.put("name", t.getName());
			d.put("number", t.getNumber());
			d.put("group", t.getGroup());
			return d;
		}
	}

	/** Тестовый репозиторий поверх BaseRepository. */
	static class TestRepository extends BaseRepository<TestEntity> {
		private static final String COLL = "it_test_entities_findOne";
		private static final TestMapper MAPPER = new TestMapper();

		public TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, MAPPER);
		}
	}
}