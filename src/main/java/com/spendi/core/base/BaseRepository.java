/**
* @file BaseRepository.java
* @module core/base
*
* Базовый репозиторий для работы с MongoDB (низкоуровневый слой).
* Предоставляет CRUD-операции как в "сыром" виде (Document),
* так и с маппингом в сущности (TEntity).
*
* <p>Принципы:
* <ul>
*   <li>Работа с коллекцией осуществляется через MongoCollection&lt;Document&gt;.</li>
*   <li>Маппинг Document ↔ Entity делегируется абстрактным методам {@link #toEntity(Document)} и {@link #toDocument(Object)}.</li>
*   <li>Пагинация: 1-базная (page &ge; 1), limit &ge; 1; параметры нормализуются.</li>
* </ul>
*
* @param <TEntity> тип сущности (POJO), с которой работает репозиторий
*
* @author Dmytro Shakh
*/

package com.spendi.core.base;

/**
 * ! lib imports
 */
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * ! java imports
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;

/**
 * Базовый репозиторий для работы с MongoDB.
 *
 * @param <TEntity> Тип сущности (POJO)
 */
public abstract class BaseRepository<TEntity> extends BaseClass {
	/** Класс сущности (может использоваться для рефлексии/маппинга/логирования). */
	protected final Class<TEntity> entityClass;
	/** Коллекция MongoDB, с которой работает репозиторий. */
	protected final MongoCollection<Document> collection;

	/**
	 * @param className      человекочитаемое имя класса (для логов)
	 * @param entity         класс сущности
	 * @param database       инстанс базы MongoDB (подключение/БД выбирается выше)
	 * @param collectionName имя коллекции (фиксируется на уровне конкретного
	 *                       репозитория)
	 */
	public BaseRepository(
			String className,
			Class<TEntity> entity,
			MongoDatabase database,
			String collectionName) {
		super(EClassType.REPOSITORY, className);
		this.entityClass = entity;
		this.collection = database.getCollection(collectionName);
	}

	/** @return класс сущности TEntity */
	public Class<TEntity> getEntityClass() {
		return entityClass;
	}

	/**
	 * ? ==============================
	 * ? ===== МАППИНГ (ABSTRACT) =====
	 * ? ==============================
	 */

	/**
	 * Преобразует BSON-документ в сущность доменной модели.
	 *
	 * @param doc BSON-документ из MongoDB (не null)
	 * @return сущность TEntity
	 */
	protected abstract TEntity toEntity(Document doc);

	/**
	 * Преобразует сущность доменной модели в BSON-документ для записи в MongoDB.
	 *
	 * @param entity сущность доменной модели
	 * @return BSON-документ
	 */
	protected abstract Document toDocument(TEntity entity);

	/**
	 * ? =================
	 * ? ===== COUNT =====
	 * ? =================
	 */

	/**
	 * Подсчитать количество документов по фильтру.
	 *
	 * @param filter карта условий
	 * @return количество документов
	 */
	public long count(Map<String, Object> filter) {
		return collection.countDocuments(new Document(filter));
	}

	/**
	 * Подсчитать количество документов по условию (key == value).
	 *
	 * @param key   поле
	 * @param value значение
	 * @return количество документов
	 */
	public long count(String key, Object value) {
		return collection.countDocuments(Filters.eq(key, value));
	}

	/**
	 * ? ==================
	 * ? ===== EXISTS =====
	 * ? ==================
	 */

	/**
	 * Проверить существование документов по фильтру.
	 *
	 * @param filter карта условий
	 * @return true, если существует хотя бы один документ
	 */
	public boolean exists(Map<String, Object> filter) {
		return collection.find(new Document(filter)).limit(1).first() != null;
	}

	/**
	 * Проверить существование документов по условию (key == value).
	 *
	 * @param key   поле
	 * @param value значение
	 * @return true, если существует хотя бы один документ
	 */
	public boolean exists(String key, Object value) {
		return collection.find(Filters.eq(key, value)).limit(1).first() != null;
	}

	/**
	 * ? ================
	 * ? ===== READ =====
	 * ? ================
	 */

	/**
	 * Найти документ по ObjectId.
	 *
	 * @param id строковый ObjectId (24 hex-символа)
	 * @return Optional с документом или empty, если не найден
	 * @throws IllegalArgumentException если id невалидный для ObjectId
	 */
	public Optional<Document> findDocById(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", new ObjectId(id))).first());
	}

	/**
	 * Найти сущность по ObjectId.
	 *
	 * @param id строковый ObjectId (24 hex-символа)
	 * @return Optional с сущностью или empty, если не найдено
	 * @throws IllegalArgumentException если id невалидный для ObjectId
	 */
	public Optional<TEntity> findById(String id) {
		return this.findDocById(id).map(this::toEntity);
	}

	/**
	 * Найти первый документ по фильтру.
	 *
	 * @param filter карта условий (ключ-значение)
	 * @return Optional c документом или empty
	 */
	public Optional<Document> findOneDoc(Map<String, Object> filter) {
		return Optional.ofNullable(collection.find(new Document(filter)).first());
	}

	/**
	 * Найти первый документ по условию (key == value).
	 *
	 * @param key   поле
	 * @param value значение
	 * @return Optional c документом или empty
	 */
	public Optional<Document> findOneDoc(String key, Object value) {
		return Optional.ofNullable(collection.find(Filters.eq(key, value)).first());
	}

	/**
	 * Найти первую сущность по фильтру.
	 *
	 * @param filter карта условий (ключ-значение)
	 * @return Optional c сущностью или empty
	 */
	public Optional<TEntity> findOne(Map<String, Object> filter) {
		return this.findOneDoc(filter).map(this::toEntity);
	}

	/**
	 * Найти первую сущность по фильтру.
	 */
	public Optional<TEntity> findOne(String key, Object value) {
		return this.findOneDoc(key, value).map(this::toEntity);
	}

	/**
	 * Найти много документов по фильтру (с пагинацией).
	 *
	 * @param filter карта условий
	 * @param page   номер страницы (≥ 1)
	 * @param limit  размер страницы (≥ 1)
	 * @return список документов
	 */
	public List<Document> findManyDocs(Map<String, Object> filter, int page, int limit) {
		int safePage = Math.max(1, page);
		int safeLimit = Math.max(1, limit);
		int skip = (safePage - 1) * safeLimit;

		return collection.find(new Document(filter))
				.skip(skip)
				.limit(limit)
				.into(new ArrayList<>());
	}

	/**
	 * Найти много документов по условию (key == value) с пагинацией.
	 *
	 * @param key   поле
	 * @param value значение
	 * @param page  номер страницы (≥ 1)
	 * @param limit размер страницы (≥ 1)
	 * @return список документов
	 */
	public List<Document> findManyDocs(String key, Object value, int page, int limit) {
		int safePage = Math.max(1, page);
		int safeLimit = Math.max(1, limit);
		int skip = (safePage - 1) * safeLimit;

		return collection.find(Filters.eq(key, value))
				.skip(skip)
				.limit(limit)
				.into(new ArrayList<>());
	}

	/**
	 * Найти много сущностей по фильтру (с пагинацией).
	 *
	 * @param filter карта условий
	 * @param page   номер страницы (≥ 1)
	 * @param limit  размер страницы (≥ 1)
	 * @return список сущностей
	 */
	public List<TEntity> findMany(Map<String, Object> filter, int page, int limit) {
		return this.findManyDocs(filter, page, limit)
				.stream() // превращаем в Stream<Document>
				.map(this::toEntity) // применяем преобразование (map в TS)
				.toList();
	}

	/**
	 * Найти много сущностей по условию (key == value) с пагинацией.
	 *
	 * @param key   поле
	 * @param value значение
	 * @param page  номер страницы (≥ 1)
	 * @param limit размер страницы (≥ 1)
	 * @return список сущностей
	 */
	public List<TEntity> findMany(String key, Object value, int page, int limit) {
		return this.findManyDocs(key, value, page, limit)
				.stream() // превращаем в Stream<Document>
				.map(this::toEntity) // применяем преобразование (map в TS)
				.toList();
	}

	/**
	 * ? ==================
	 * ? ===== CREATE =====
	 * ? ==================
	 */

	/**
	 * Вставить один документ (сырой).
	 *
	 * @param doc BSON-документ
	 */
	public void insertOne(Document entity) {
		collection.insertOne(entity);
	}

	/**
	 * Вставить одну сущность (с маппингом в Document).
	 *
	 * @param entity сущность
	 */
	public void insertOne(TEntity entity) {
		insertOne(toDocument(entity));
	}

	/**
	 * Вставить несколько документов.
	 *
	 * @param docs список BSON-документов
	 */
	public void insertMany(List<Document> docs) {
		if (docs == null || docs.isEmpty())
			return;
		collection.insertMany(docs);
	}

	/**
	 * Вставить несколько сущностей (с маппингом).
	 *
	 * @param entities список сущностей
	 */
	public void insertManyEntities(List<TEntity> entities) {
		if (entities == null || entities.isEmpty())
			return;
		List<Document> docs = entities.stream().map(this::toDocument).toList();
		insertMany(docs);
	}

	/**
	 * ? ==================
	 * ? ===== UPDATE =====
	 * ? ==================
	 */

	/**
	 * Обновить документ по id (partial update: $set).
	 *
	 * @param id      ObjectId в строковом виде
	 * @param updates карта полей для $set
	 */
	public void updateById(String id, Map<String, Object> updates) {
		collection.updateOne(
				Filters.eq("_id", new ObjectId(id)),
				new Document("$set", new Document(updates)));
	}

	/**
	 * Обновить один документ по фильтру (partial update: $set).
	 *
	 * @param filter  карта условий
	 * @param updates карта полей для $set
	 */
	public void updateOne(Map<String, Object> filter, Map<String, Object> updates) {
		collection.updateOne(
				new Document(filter),
				new Document("$set", new Document(updates)));
	}

	/**
	 * Обновить один документ по условию (key == value) (partial update: $set).
	 *
	 * @param key     поле
	 * @param value   значение
	 * @param updates карта полей для $set
	 */
	public void updateOne(String key, Object value, Map<String, Object> updates) {
		collection.updateOne(
				Filters.eq(key, value),
				new Document("$set", new Document(updates)));
	}

	/**
	 * Обновить много документов по фильтру (partial update: $set).
	 *
	 * @param filter  карта условий
	 * @param updates карта полей для $set
	 */
	public void updateMany(Map<String, Object> filter, Map<String, Object> updates) {
		collection.updateMany(
				new Document(filter),
				new Document("$set", new Document(updates)));
	}

	/**
	 * Обновить много документов по условию (key == value) (partial update: $set).
	 *
	 * @param key     поле
	 * @param value   значение
	 * @param updates карта полей для $set
	 */
	public void updateMany(String key, Object value, Map<String, Object> updates) {
		collection.updateMany(
				Filters.eq(key, value),
				new Document("$set", new Document(updates)));
	}

	/**
	 * ? ==================
	 * ? ===== DELETE =====
	 * ? ==================
	 */

	/**
	 * Удалить один документ по id.
	 *
	 * @param id ObjectId в строковом виде
	 */
	public void deleteById(String id) {
		collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
	}

	/**
	 * Удалить один документ по фильтру.
	 *
	 * @param filter карта условий
	 */
	public void deleteOne(Map<String, Object> filter) {
		collection.deleteOne(new Document(filter));
	}

	/**
	 * Удалить один документ по условию (key == value).
	 *
	 * @param key   поле
	 * @param value значение
	 */
	public void deleteOne(String key, Object value) {
		collection.deleteOne(Filters.eq(key, value));
	}

	/**
	 * Удалить несколько документов по фильтру.
	 *
	 * @param filter карта условий
	 */
	public void deleteMany(Map<String, Object> filter) {
		collection.deleteMany(new Document(filter));
	}

	/**
	 * Удалить несколько документов по условию (key == value).
	 *
	 * @param key   поле
	 * @param value значение
	 */
	public void deleteMany(String key, Object value) {
		collection.deleteMany(Filters.eq(key, value));
	}
}
