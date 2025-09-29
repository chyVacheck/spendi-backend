
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.core.types.DocMapper;
import com.spendi.testutil.RealMongoTest;

/**
 * IT для:
 * - findManyDocs(Map<String,Object>, page, limit)
 * - findManyDocs(String, Object, page, limit)
 * - findMany(Map<String,Object>, page, limit)
 * - findMany(String, Object, page, limit)
 */
class BaseRepositoryFindManyIT extends RealMongoTest {

	private TestRepository repo;

	@BeforeEach
	void setUp() {
		db.drop();
		repo = new TestRepository(db);
	}

	// ---------- findManyDocs(Map) ----------

	@Test
	void findManyDocs_byMap_all_with_pagination() {
		seed(e("A", 1, "G1"), e("B", 2, "G1"), e("C", 3, "G2"), e("D", 4, "G1"), e("E", 5, "G2"));

		// page1, limit2 -> A,B
		List<Document> p1 = repo.findManyDocs(Map.of(), 1, 2);
		assertThat(p1).hasSize(2);
		assertThat(names(p1)).containsExactly("A", "B");

		// page2, limit2 -> C,D
		List<Document> p2 = repo.findManyDocs(Map.of(), 2, 2);
		assertThat(p2).hasSize(2);
		assertThat(names(p2)).containsExactly("C", "D");

		// page3, limit2 -> E
		List<Document> p3 = repo.findManyDocs(Map.of(), 3, 2);
		assertThat(p3).hasSize(1);
		assertThat(names(p3)).containsExactly("E");

		// page4, limit2 -> empty
		List<Document> p4 = repo.findManyDocs(Map.of(), 4, 2);
		assertThat(p4).isEmpty();
	}

	@Test
	void findManyDocs_byMap_filtered_group() {
		seed(e("A", 1, "G1"), e("B", 2, "G1"), e("C", 3, "G2"), e("D", 4, "G1"), e("E", 5, "G2"));

		// filter group=G1 -> A,B,D (в порядке вставки)
		List<Document> g1 = repo.findManyDocs(Map.of("group", "G1"), 1, 10);
		assertThat(names(g1)).containsExactly("A", "B", "D");

		// filter group=G2 -> C,E
		List<Document> g2 = repo.findManyDocs(Map.of("group", "G2"), 1, 10);
		assertThat(names(g2)).containsExactly("C", "E");

		// filter group=MISS -> empty
		List<Document> miss = repo.findManyDocs(Map.of("group", "MISS"), 1, 10);
		assertThat(miss).isEmpty();
	}

	@Test
	void findManyDocs_byMap_normalizes_page_and_limit() {
		seed(e("A", 1, "G1"), e("B", 2, "G1"));

		// page<=0 -> page=1; limit<=0 -> limit=1
		List<Document> res1 = repo.findManyDocs(Map.of(), 0, 0);
		assertThat(names(res1)).containsExactly("A");

		List<Document> res2 = repo.findManyDocs(Map.of(), -5, -10);
		assertThat(names(res2)).containsExactly("A");
	}

	// ---------- findManyDocs(key,value) ----------

	@Test
	void findManyDocs_byKeyValue_pagination_slice() {
		seed(e("A", 1, "GX"), e("B", 2, "GX"), e("C", 3, "GX"), e("D", 4, "GY"), e("E", 5, "GX"));

		// group=GX: A,B,C,E
		List<Document> page1 = repo.findManyDocs("group", "GX", 1, 2); // A,B
		List<Document> page2 = repo.findManyDocs("group", "GX", 2, 2); // C,E
		List<Document> page3 = repo.findManyDocs("group", "GX", 3, 2); // empty

		assertThat(names(page1)).containsExactly("A", "B");
		assertThat(names(page2)).containsExactly("C", "E");
		assertThat(page3).isEmpty();
	}

	// ---------- findMany(Map) (entities) ----------

	@Test
	void findMany_byMap_maps_to_entities_in_order() {
		seed(e("A", 1, "G1"), e("B", 2, "G1"), e("C", 3, "G1"));

		var entities = repo.findMany(Map.of("group", "G1"), 1, 10);
		assertThat(entities).hasSize(3);
		assertThat(entities.stream().map(TestEntity::getName).toList()).containsExactly("A", "B", "C");
		assertThat(entities.stream().map(TestEntity::getNumber).toList()).containsExactly(1, 2, 3);
	}

	@Test
	void findMany_byMap_empty_when_no_matches() {
		seed(e("A", 1, "G1"));
		var entities = repo.findMany(Map.of("group", "MISS"), 1, 10);
		assertThat(entities).isEmpty();
	}

	@Test
	void findMany_byMap_pagination_boundary() {
		seed(e("A", 1, "G1"), e("B", 2, "G1"), e("C", 3, "G1"), e("D", 4, "G1"));

		var p1 = repo.findMany(Map.of("group", "G1"), 1, 3); // A,B,C
		var p2 = repo.findMany(Map.of("group", "G1"), 2, 3); // D
		var p3 = repo.findMany(Map.of("group", "G1"), 3, 3); // empty

		assertThat(p1.stream().map(TestEntity::getName).toList()).containsExactly("A", "B", "C");
		assertThat(p2.stream().map(TestEntity::getName).toList()).containsExactly("D");
		assertThat(p3).isEmpty();
	}

	// ---------- findMany(key,value) (entities) ----------

	@Test
	void findMany_byKeyValue_maps_to_entities_and_paginates() {
		seed(e("A", 1, "H"), e("B", 2, "H"), e("C", 3, "H"), e("D", 4, "X"));

		var h1 = repo.findMany("group", "H", 1, 2); // A,B
		var h2 = repo.findMany("group", "H", 2, 2); // C

		assertThat(h1.stream().map(TestEntity::getName).toList()).containsExactly("A", "B");
		assertThat(h2.stream().map(TestEntity::getName).toList()).containsExactly("C");
	}

	// ---------------- helpers ----------------

	private void seed(TestEntity... items) {
		repo.insertManyEntities(List.of(items));
	}

	private static List<String> names(List<Document> docs) {
		List<String> r = new ArrayList<>(docs.size());
		for (Document d : docs) {
			r.add(d.getString("name"));
		}
		return r;
	}

	private static TestEntity e(String name, int number, String group) {
		TestEntity t = new TestEntity();
		t.setId(new ObjectId());
		t.setName(name);
		t.setNumber(number);
		t.setGroup(group);
		return t;
	}

	// ------ тестовая обвязка (такая же, как в прошлых тестах) ------

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
		private static final String COLL = "it_test_entities_findMany";
		private static final TestMapper MAPPER = new TestMapper();

		public TestRepository(MongoDatabase db) {
			super(TestRepository.class.getSimpleName(), TestEntity.class, db, COLL, MAPPER);
		}
	}
}