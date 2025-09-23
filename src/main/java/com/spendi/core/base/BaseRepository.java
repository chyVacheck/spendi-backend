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
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

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
import com.spendi.core.base.database.GenericUpdate;
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
	 * @param collectionName имя коллекции (фиксируется на уровне конкретного репозитория)
	 */
	public BaseRepository(String className, Class<TEntity> entity, MongoDatabase database, String collectionName) {
		super(EClassType.REPOSITORY, className);
		this.entityClass = entity;
		this.collection = database.getCollection(collectionName);
	}

	/** @return класс сущности TEntity */
	public Class<TEntity> getEntityClass() {
		return entityClass;
	}

	/**
	 * ? === === === МАППИНГ (ABSTRACT) === === ===
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
	 * ? === === === COUNT === === ===
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
	 * ? === === === EXISTS === === ===
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
	 * ? === === === READ === === ===
	 */

	/**
	 * Найти документ по _id.
	 *
	 * @param id ObjectId
	 * @return Optional с документом или empty, если не найден
	 */
	public Optional<Document> findDocById(ObjectId id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first());
	}

	/**
	 * Найти сущность по ObjectId.
	 *
	 * @param id ObjectId
	 * @return Optional с сущностью или empty, если не найдено
	 */
	public Optional<TEntity> findById(ObjectId id) {
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

		return collection.find(new Document(filter)).skip(skip).limit(limit).into(new ArrayList<>());
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

		return collection.find(Filters.eq(key, value)).skip(skip).limit(limit).into(new ArrayList<>());
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
		return this.findManyDocs(filter, page, limit).stream() // превращаем в Stream<Document>
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
		return this.findManyDocs(key, value, page, limit).stream() // превращаем в Stream<Document>
				.map(this::toEntity) // применяем преобразование (map в TS)
				.toList();
	}

	/**
	 * ? === === === CREATE === === ===
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
	 * ? === === === UPDATE === === ===
	 */

	/**
	 * @description Обновить документ по id и вернуть обновлённый документ.
	 *
	 * @param id      ObjectId
	 * @param updates карта полей для обновления
	 * @return Optional с обновлённым документом или empty, если документ не найден
	 */
	public Optional<Document> updateDocById(ObjectId id, GenericUpdate updates) {
		var opts = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		Document updated = collection.findOneAndUpdate(Filters.eq("_id", id), updates.toMongoDocument(), opts);
		return Optional.ofNullable(updated);
	}

	/**
	 * @description Обновить документ по id и вернуть обновлённую сущность.
	 *
	 * @param id      ObjectId
	 * @param updates карта полей для обновления
	 * @return Optional с обновлённой сущностью или empty, если документ не найден
	 */
	public Optional<TEntity> updateById(ObjectId id, GenericUpdate updates) {
		return this.updateDocById(id, updates) // Optional<Document>
				.map(this::toEntity); // Optional<TEntity>
	}

	/**
	 * @description Обновить один документ по фильтру и вернуть обновлённый документ.
	 * 
	 * @param filter  карта условий
	 * @param updates карта полей для обновления
	 * @return Optional с обновлённым документом или empty, если документ не найден
	 */
	public Optional<Document> updateDocOne(Map<String, Object> filter, GenericUpdate updates) {
		var opts = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		Document updated = collection.findOneAndUpdate(new Document(filter), updates.toMongoDocument(), opts);
		return Optional.ofNullable(updated);
	}

	/**
	 * @description Обновить один документ по фильтру и вернуть обновлённую сущность.
	 *
	 * @param filter  карта условий
	 * @param updates карта полей для $set
	 * @return Optional с обновлённой сущностью или empty, если документ не найден
	 */
	public Optional<TEntity> updateOne(Map<String, Object> filter, GenericUpdate updates) {
		return this.updateDocOne(filter, updates).map(this::toEntity);
	}

	/**
	 * @description Обновить один документ по условию key==value и вернуть обновлённый документ.
	 *
	 * @param key     поле
	 * @param value   значение
	 * @param updates карта полей для обновления
	 * @return Optional с обновлённым документом или empty, если документ не найден
	 */
	public Optional<Document> updateDocOne(String key, Object value, GenericUpdate updates) {
		var opts = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
		Document updated = collection.findOneAndUpdate(Filters.eq(key, value), updates.toMongoDocument(), opts);
		return Optional.ofNullable(updated);
	}

	/**
	 * @description Обновить один документ по условию (key == value) и вернуть обновлённую сущность.
	 *
	 * @param key     поле
	 * @param value   значение
	 * @param updates карта полей для обновления
	 * @return Optional с обновлённой сущностью или empty, если документ не найден
	 */
	public Optional<TEntity> updateOne(String key, Object value, GenericUpdate updates) {
		return this.updateDocOne(key, value, updates).map(this::toEntity);
	}

	/**
	 * * Обновление нескольких
	 */

	/**
	 * @description Обновить много документов по фильтру. Возвращает статистику обновления.
	 *
	 * @param filter  карта условий
	 * @param updates карта полей для обновления
	 * @return UpdateResult с статистикой обновления
	 */
	public UpdateResult updateManyDocs(Map<String, Object> filter, GenericUpdate updates) {
		return collection.updateMany(new Document(filter), updates.toMongoDocument());
	}

	/**
	 * Обновить много документов по условию key==value. Возвращает статистику обновления.
	 * 
	 * @param key     поле
	 * @param value   значение
	 * @param updates обновления
	 * @return UpdateResult с статистикой обновления
	 */
	public UpdateResult updateManyDocs(String key, Object value, GenericUpdate updates) {
		return collection.updateMany(Filters.eq(key, value), updates.toMongoDocument());
	}

	/**
	 * ? === === === DELETE === === ===
	 */

	/**
	 * Удалить один документ по id и вернуть его id (если был удалён).
	 *
	 * @param id ObjectId в строковом виде
	 */
	public Optional<String> deleteById(ObjectId id) {
		Document deleted = collection.findOneAndDelete(Filters.eq("_id", id));
		if (deleted == null)
			return Optional.empty();
		ObjectId deletedId = deleted.getObjectId("_id");
		return Optional.ofNullable(deletedId).map(ObjectId::toHexString);
	}

	/**
	 * Удалить один документ по фильтру и вернуть удалённый id.
	 *
	 * 
	 * @param filter карта условий
	 */
	public Optional<String> deleteOne(Map<String, Object> filter) {
		Document deleted = collection.findOneAndDelete(new Document(filter));
		if (deleted == null)
			return Optional.empty();
		ObjectId deletedId = deleted.getObjectId("_id");
		return Optional.ofNullable(deletedId).map(ObjectId::toHexString);
	}

	/**
	 * Удалить один документ по условию key==value и вернуть удалённый id.
	 * 
	 * @param key   поле
	 * @param value значение
	 */
	public Optional<String> deleteOne(String key, Object value) {
		Document deleted = collection.findOneAndDelete(Filters.eq(key, value));
		if (deleted == null)
			return Optional.empty();
		ObjectId deletedId = deleted.getObjectId("_id");
		return Optional.ofNullable(deletedId).map(ObjectId::toHexString);
	}

	/**
	 * Удалить несколько документов по фильтру. Возвращает количество удалённых.
	 * 
	 * @param filter карта условий
	 */
	public long deleteMany(Map<String, Object> filter) {
		DeleteResult r = collection.deleteMany(new Document(filter));
		return r.getDeletedCount();
	}

	/**
	 * Удалить несколько документов по условию key==value. Возвращает количество удалённых.
	 * 
	 * @param key   поле
	 * @param value значение
	 */
	public long deleteMany(String key, Object value) {
		DeleteResult r = collection.deleteMany(Filters.eq(key, value));
		return r.getDeletedCount();
	}

}
