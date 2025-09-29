
package com.spendi.core.base.repository.create;

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
 * - insertManyDocs(List<Document>)
 * - insertManyEntities(List<TEntity>)
 */
class BaseRepositoryInsertManyIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);
	}

	// ---------------- insertManyDocs ----------------

	@Test
	void insertManyDocs_null_is_noop() {
		// when
		repo.insertManyDocs(null);

		// then
		long cnt = repo.count(Map.of());
		assertThat(cnt).isZero();
	}

	@Test
	void insertManyDocs_empty_is_noop() {
		// when
		repo.insertManyDocs(List.of());

		// then
		long cnt = repo.count(Map.of());
		assertThat(cnt).isZero();
	}

	@Test
	void insertManyDocs_mixed_with_and_without_id_inserts_all_and_generates_ids() {
		// given
		ObjectId preset = new ObjectId();
		Document d1 = new Document("_id", preset).append("name", "raw-1").append("number", 11).append("group", "G-A");

		Document d2 = new Document().append("name", "raw-2").append("number", 22).append("group", "G-A");

		Document d3 = new Document().append("name", "raw-3").append("number", 33).append("group", "G-B");

		// when
		repo.insertManyDocs(List.of(d1, d2, d3));

		// then
		assertThat(repo.count(Map.of())).isEqualTo(3);

		// verify specific docs
		Optional<Document> f1 = repo.findOneDoc(Map.of("name", "raw-1"));
		Optional<Document> f2 = repo.findOneDoc(Map.of("name", "raw-2"));
		Optional<Document> f3 = repo.findOneDoc(Map.of("name", "raw-3"));

		assertThat(f1).isPresent();
		assertThat(f1.get().getObjectId("_id")).isEqualTo(preset);

		assertThat(f2).isPresent();
		assertThat(f2.get().getObjectId("_id")).isNotNull();

		assertThat(f3).isPresent();
		assertThat(f3.get().getObjectId("_id")).isNotNull();
	}

	// ---------------- insertManyEntities ----------------

	@Test
	void insertManyEntities_null_is_noop() {
		repo.insertManyEntities(null);
		assertThat(repo.count(Map.of())).isZero();
	}

	@Test
	void insertManyEntities_empty_is_noop() {
		repo.insertManyEntities(List.of());
		assertThat(repo.count(Map.of())).isZero();
	}

	@Test
	void insertManyEntities_roundtrip_mixed_ids_ok() {
		// given
		TestEntity e1 = entityWithId(new ObjectId(), "e1", 1, "G-1"); // с _id
		TestEntity e2 = entityNoId("e2", 2, "G-1"); // без _id -> сгенерится
		TestEntity e3 = entityNoId("e3", 3, "G-2"); // без _id -> сгенерится

		// when
		repo.insertManyEntities(List.of(e1, e2, e3));

		// then
		assertThat(repo.count(Map.of())).isEqualTo(3);

		var gotAll = repo.findMany(Map.of(), 1, 10);
		assertThat(gotAll).hasSize(3);

		// Проверим, что по именам всё есть и поля совпадают
		var names = gotAll.stream().map(TestEntity::getName).toList();
		assertThat(names).containsExactlyInAnyOrder("e1", "e2", "e3");

		TestEntity got1 = byName(gotAll, "e1");
		TestEntity got2 = byName(gotAll, "e2");
		TestEntity got3 = byName(gotAll, "e3");

		assertThat(got1.getId()).isEqualTo(e1.getId()); // сохранён заданный _id
		assertThat(got2.getId()).isNotNull(); // сгенерирован
		assertThat(got3.getId()).isNotNull(); // сгенерирован

		assertThat(got1.getNumber()).isEqualTo(1);
		assertThat(got2.getNumber()).isEqualTo(2);
		assertThat(got3.getNumber()).isEqualTo(3);

		assertThat(got1.getGroup()).isEqualTo("G-1");
		assertThat(got2.getGroup()).isEqualTo("G-1");
		assertThat(got3.getGroup()).isEqualTo("G-2");
	}

	// ---------------- helpers ----------------

	private static TestEntity entityWithId(ObjectId id, String name, int number, String group) {
		TestEntity t = new TestEntity();
		t.setId(id);
		t.setName(name);
		t.setNumber(number);
		t.setGroup(group);
		return t;
	}

	private static TestEntity entityNoId(String name, int number, String group) {
		TestEntity t = new TestEntity();
		t.setName(name);
		t.setNumber(number);
		t.setGroup(group);
		return t;
	}

	private static TestEntity byName(List<TestEntity> list, String name) {
		return list.stream().filter(t -> name.equals(t.getName())).findFirst().orElseThrow();
	}

	// ----- минимальная тестовая модель/маппер/репозиторий -----

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
		private static final String COLL = "it_test_entities_insertMany";
		private static final TestMapper MAPPER = new TestMapper();

		public TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, MAPPER);
		}
	}
}