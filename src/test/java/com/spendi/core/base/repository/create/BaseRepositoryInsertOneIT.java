
package com.spendi.core.base.repository.create;

/**
 * ! lib imports
 */
import static org.assertj.core.api.Assertions.assertThat;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
 * - insertOneDoc(Document)
 * - insertOne(TEntity)
 */
class BaseRepositoryInsertOneIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);
	}

	// ---------- insertOneDoc(Document) ----------

	@Test
	void insertOneDoc_without_id_mongo_generates_id() {
		// given
		Document raw = new Document().append("name", "raw-no-id").append("number", 100).append("group", "G-A");

		// when
		repo.insertOneDoc(raw);

		// then: читаем по уникальному полю name
		Optional<Document> found = repo.findOneDoc(Map.of("name", "raw-no-id"));
		assertThat(found).isPresent();

		Document d = found.get();
		assertThat(d.getObjectId("_id")).isNotNull();
		assertThat(d.getString("name")).isEqualTo("raw-no-id");
		assertThat(d.getInteger("number")).isEqualTo(100);
		assertThat(d.getString("group")).isEqualTo("G-A");
	}

	@Test
	void insertOneDoc_with_id_is_persisted_as_is() {
		// given
		ObjectId id = new ObjectId();
		Document raw = new Document().append("_id", id).append("name", "raw-with-id").append("number", 200)
				.append("group", "G-B");

		// when
		repo.insertOneDoc(raw);

		// then: читаем по _id
		Optional<Document> byId = repo.findDocById(id);
		assertThat(byId).isPresent();

		Document d = byId.get();
		assertThat(d.getObjectId("_id")).isEqualTo(id);
		assertThat(d.getString("name")).isEqualTo("raw-with-id");
		assertThat(d.getInteger("number")).isEqualTo(200);
		assertThat(d.getString("group")).isEqualTo("G-B");
	}

	// ---------- insertOne(entity) ----------

	@Test
	void insertOne_entity_with_id_roundtrip_via_findById() {
		// given
		TestEntity e = new TestEntity();
		ObjectId id = new ObjectId();
		e.setId(id);
		e.setName("entity-with-id");
		e.setNumber(10);
		e.setGroup("G-1");

		// when
		repo.insertOne(e);

		// then
		var found = repo.findById(id);
		assertThat(found).isPresent();

		TestEntity got = found.get();
		assertThat(got.getId()).isEqualTo(id);
		assertThat(got.getName()).isEqualTo("entity-with-id");
		assertThat(got.getNumber()).isEqualTo(10);
		assertThat(got.getGroup()).isEqualTo("G-1");
	}

	@Test
	void insertOne_entity_without_id_mongo_generates_id_and_mapper_reads_back() {
		// given: сущность без _id
		TestEntity e = new TestEntity();
		e.setName("entity-no-id");
		e.setNumber(11);
		e.setGroup("G-2");

		// when
		repo.insertOne(e);

		// then: ищем по фильтру и проверяем маппинг (в entity _id из БД мы не проталкивали)
		var found = repo.findOne(Map.of("name", "entity-no-id"));
		assertThat(found).isPresent();

		TestEntity got = found.get();
		assertThat(got.getId()).isNotNull(); // _id сгенерирован Mongo
		assertThat(got.getName()).isEqualTo("entity-no-id");
		assertThat(got.getNumber()).isEqualTo(11);
		assertThat(got.getGroup()).isEqualTo("G-2");
	}

	// ---------------- helpers ----------------

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
		private static final String COLL = "it_test_entities_insertOne";
		private static final TestMapper MAPPER = new TestMapper();

		public TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, MAPPER);
		}
	}
}