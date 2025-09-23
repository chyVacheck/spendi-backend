/**
 * @file BaseRepositoryService.java
 * @module com.spendi.core.base.service
 *
 * @description
 * Базовый сервис «поверх репозитория». Инкапсулирует типовые сценарии:
 * - чтение по id / фильтру / пагинации;
 * - создание (одной/многих сущностей);
 * - обновления и удаления;
 * - count / exists;
 * - формирование единых ответов {@link com.spendi.core.response.ServiceResponse}.
 *
 * Сервис — место для бизнес‑логики и контекста. Репозиторий — низкий уровень (CRUD).
 *
 * @param <TRepo>   тип репозитория
 * @param <TEntity> тип сущности
 *
 * @author
 * Dmytro Shakh
 */

package com.spendi.core.base.service;

/**
 * ! lib imports
 */
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * ! java imports
 */
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.core.base.database.GenericUpdate;
import com.spendi.core.exceptions.EntityNotFoundException;
import com.spendi.core.response.ServiceResponse;
import com.spendi.core.types.Pagination;

public abstract class BaseRepositoryService<TRepo extends BaseRepository<TEntity>, TEntity> extends BaseService {
	protected final TRepo repository;

	/**
	 * @param className  имя сервиса
	 * @param repository инстанс репозитория (конкретного)
	 */
	public BaseRepositoryService(String className, TRepo repository) {
		super(className);

		this.repository = Objects.requireNonNull(repository, "repository must not be null ");
	}

	/**
	 * ! === === === PRIVATE === === ===
	 */

	/** Собрать фильтр из пары key/value. Допускает null в значении. */
	private static Map<String, Object> singletonFilter(String key, Object value) {
		HashMap<String, Object> map = new HashMap<>(1);
		map.put(key, value); // используем HashMap, т.к. Map.of(..) не допускает null
		return map;
	}

	/** DRY: единое построение пагинации по фильтру. */
	private Pagination buildPagination(Map<String, Object> filter, int page, int limit) {
		int safePage = Math.max(1, page);
		int safeLimit = Math.max(1, limit);
		long total = repository.count(filter);
		int totalPages = (int) Math.max(1, Math.ceil((double) total / safeLimit));
		return new Pagination(safePage, total, safeLimit, totalPages);
	}

	/**
	 * ? === === === COUNT === === ===
	 */

	/** Подсчитать количество по фильтру. */
	public ServiceResponse<Long> count(Map<String, Object> filter) {
		long total = this.repository.count(filter);
		return ServiceResponse.counted(total);
	}

	/** Подсчитать количество по условию key == value. */
	public ServiceResponse<Long> count(String key, Object value) {
		long total = this.repository.count(key, value);
		return ServiceResponse.counted(total);
	}

	/**
	 * ? === === === EXISTS === === ===
	 */

	/** Проверить существование по фильтру. */
	public ServiceResponse<Boolean> exists(Map<String, Object> filter) {
		boolean ok = this.repository.exists(filter);
		return ok ? ServiceResponse.founded(Boolean.TRUE) : ServiceResponse.founded(Boolean.FALSE);
	}

	/** Проверить существование по условию key == value. */
	public ServiceResponse<Boolean> exists(String key, Object value) {
		boolean ok = this.repository.exists(key, value);
		return ok ? ServiceResponse.founded(Boolean.TRUE) : ServiceResponse.founded(Boolean.FALSE);
	}

	/**
	 * ? === === === READ === === ===
	 */

	/**
	 * Получить документ по id.
	 */
	public ServiceResponse<Document> getDocById(ObjectId id) {
		return this.repository.findDocById(id).map(doc -> ServiceResponse.founded(doc))
				.orElseThrow(() -> new EntityNotFoundException(this.repository.getEntityClass().getSimpleName(),
						Map.of("id", id.toHexString())));
	}

	/**
	 * Получить документ по id.
	 */
	public ServiceResponse<Document> getDocById(String id) {
		return this.getDocById(new ObjectId(id));
	}

	/**
	 * Получить сущность по id.
	 */
	public ServiceResponse<TEntity> getById(ObjectId id) {
		return this.repository.findById(id).map(ServiceResponse::founded)
				.orElseThrow(() -> new EntityNotFoundException(this.repository.getEntityClass().getSimpleName(),
						Map.of("id", id.toHexString())));
	}

	/**
	 * Получить сущность по id.
	 */
	public ServiceResponse<TEntity> getById(String id) {
		return this.getById(new ObjectId(id));
	}

	/**
	 * Единичный документ по фильтру (ядро).
	 */
	protected ServiceResponse<Document> getOneDocByFilter(Map<String, Object> filter) {
		return repository.findOneDoc(filter).map(ServiceResponse::founded)
				.orElseThrow(() -> new EntityNotFoundException(repository.getEntityClass().getSimpleName(), filter));
	}

	/**
	 * Единичная сущность по фильтру (ядро).
	 */
	protected ServiceResponse<TEntity> getOneByFilter(Map<String, Object> filter) {
		return repository.findOne(filter).map(ServiceResponse::founded)
				.orElseThrow(() -> new EntityNotFoundException(repository.getEntityClass().getSimpleName(), filter));
	}

	/**
	 * Много документов по фильтру с пагинацией.
	 */
	protected ServiceResponse<List<Document>> getManyDocsByFilter(Map<String, Object> filter, int page, int limit) {
		List<Document> docs = repository.findManyDocs(filter, page, limit);
		Pagination pagination = buildPagination(filter, page, limit);
		return ServiceResponse.founded(docs, pagination);
	}

	/**
	 * Много сущностей по фильтру с пагинацией.
	 */
	protected ServiceResponse<List<TEntity>> getManyByFilter(Map<String, Object> filter, int page, int limit) {
		List<TEntity> list = repository.findMany(filter, page, limit);
		Pagination pagination = buildPagination(filter, page, limit);
		return ServiceResponse.founded(list, pagination);
	}

	/**
	 * Получить документ по фильтру.
	 */
	public ServiceResponse<Document> getOneDoc(Map<String, Object> filter) {
		return this.getOneDocByFilter(filter);
	}

	/**
	 * Получить первый документ по условию (key == value).
	 */
	public ServiceResponse<Document> getOneDoc(String key, Object value) {
		return this.getOneDocByFilter(singletonFilter(key, value));
	}

	/**
	 * Получить сущность по фильтру.
	 */
	public ServiceResponse<TEntity> getOne(Map<String, Object> filter) {
		return this.getOneByFilter(filter);
	}

	/**
	 * Получить сущность по фильтру.
	 */
	public ServiceResponse<TEntity> getOne(String key, Object value) {
		return this.getOneByFilter(singletonFilter(key, value));
	}

	/**
	 * * Получение списка
	 */

	/**
	 * Получить много документов по фильтру с пагинацией.
	 */
	public ServiceResponse<List<Document>> getManyDocs(Map<String, Object> filter, int page, int limit) {
		return this.getManyDocsByFilter(filter, page, limit);
	}

	/**
	 * Получить много документов по условию (key == value) с пагинацией.
	 */
	public ServiceResponse<List<Document>> getManyDocs(String key, Object value, int page, int limit) {
		return this.getManyDocsByFilter(singletonFilter(key, value), page, limit);
	}

	/**
	 * Получить много сущностей по фильтру с пагинацией.
	 */
	public ServiceResponse<List<TEntity>> getMany(Map<String, Object> filter, int page, int limit) {
		return this.getManyByFilter(filter, page, limit);
	}

	/**
	 * Получить много сущностей по условию (key == value) с пагинацией.
	 */
	public ServiceResponse<List<TEntity>> getMany(String key, Object value, int page, int limit) {
		return this.getManyByFilter(singletonFilter(key, value), page, limit);
	}

	/**
	 * ? === === === CREATE === === ===
	 */

	/**
	 * Создать один документ (сырой).
	 */
	public ServiceResponse<Document> createOne(Document doc) {
		this.repository.insertOne(doc);
		return ServiceResponse.created(doc);
	}

	/**
	 * Создать одну сущность.
	 */
	public ServiceResponse<TEntity> createOne(TEntity entity) {
		this.repository.insertOne(entity);
		return ServiceResponse.created(entity);
	}

	/**
	 * * Создания списка
	 */

	/**
	 * Создать несколько документов.
	 */
	public ServiceResponse<List<Document>> createManyDocs(List<Document> docs) {
		this.repository.insertMany(docs);
		return ServiceResponse.created(docs);
	}

	/**
	 * Создать несколько сущностей.
	 */
	public ServiceResponse<List<TEntity>> createMany(List<TEntity> entities) {
		this.repository.insertManyEntities(entities);
		return ServiceResponse.created(entities);
	}

	/**
	 * ? === === === UPDATE === === ===
	 */

	/**
	 * Быстрый апдейт по id (без детекции no-op). Возвращает UPDATED.
	 * 
	 * @param id      ObjectId сущности
	 * @param updates обновления
	 * 
	 * @throws EntityNotFoundException если сущность не найдена
	 * 
	 * @return обновлённая сущность
	 */
	public ServiceResponse<TEntity> updateById(ObjectId id, GenericUpdate updates) {
		return repository.updateById(id, updates).map(ServiceResponse::updated)
				.orElseThrow(() -> new EntityNotFoundException(repository.getEntityClass().getSimpleName(),
						Map.of("id", id.toHexString())));
	}

	/**
	 * Быстрый апдейт по id (без детекции no-op). Возвращает UPDATED.
	 * 
	 * @param id      строковый ObjectId сущности
	 * @param updates обновления
	 * 
	 * @throws EntityNotFoundException если сущность не найдена
	 * 
	 * @return обновлённая сущность
	 */
	public ServiceResponse<TEntity> updateById(String id, GenericUpdate updates) {
		return this.updateById(new ObjectId(id), updates);
	}

	/**
	 * Быстрый апдейт по фильтру (первого подходящего). Возвращает UPDATED.
	 * 
	 * @param id      строковый ObjectId сущности
	 * @param updates обновления
	 * 
	 * @throws EntityNotFoundException если сущность не найдена
	 * 
	 * @return обновлённая сущность
	 */
	public ServiceResponse<TEntity> updateOne(Map<String, Object> filter, GenericUpdate updates) {
		return repository.updateOne(filter, updates).map(ServiceResponse::updated)
				.orElseThrow(() -> new EntityNotFoundException(repository.getEntityClass().getSimpleName(), filter));
	}

	/**
	 * Быстрый апдейт по key==value (первого подходящего). Возвращает UPDATED.
	 * 
	 * @param key     строковый ключ
	 * @param value   значение
	 * @param updates обновления
	 * 
	 * @throws EntityNotFoundException если сущность не найдена
	 * 
	 * @return обновлённая сущность
	 */
	public ServiceResponse<TEntity> updateOne(String key, Object value, GenericUpdate updates) {
		return repository.updateOne(key, value, updates).map(ServiceResponse::updated).orElseThrow(
				() -> new EntityNotFoundException(repository.getEntityClass().getSimpleName(), Map.of(key, value)));
	}

	/**
	 * * Обновить множество
	 */

	/**
	 * Обновить много по фильтру. Возвращает UPDATED(modifiedCount) или NOTHING(0).
	 * 
	 * @param filter  фильтр
	 * @param updates обновления
	 * 
	 * @return обновлённое количество документов
	 */
	public ServiceResponse<Long> updateMany(Map<String, Object> filter, GenericUpdate updates) {
		var res = repository.updateManyDocs(filter, updates);
		long modified = res.getModifiedCount();
		return (modified > 0) ? ServiceResponse.updated(modified) : ServiceResponse.nothingWrite(0L);
	}

	/**
	 * Обновить много по key==value. Возвращает UPDATED(modifiedCount) или NOTHING(0).
	 * 
	 * @param key     строковый ключ
	 * @param value   значение
	 * @param updates обновления
	 * 
	 * @return обновлённое количество документов
	 */
	public ServiceResponse<Long> updateMany(String key, Object value, GenericUpdate updates) {
		var res = repository.updateManyDocs(key, value, updates);
		long modified = res.getModifiedCount();
		return (modified > 0) ? ServiceResponse.updated(modified) : ServiceResponse.nothingWrite(0L);
	}

	/**
	 * ? === === === DELETE === === ===
	 */

	/**
	 * Удалить документ по id. Возвращает удалённый id или NotFound.
	 * 
	 * @param id ObjectId сущности
	 * 
	 * @throws EntityNotFoundException если сущность не найдена
	 * 
	 * @return удалённый id
	 */
	public ServiceResponse<String> deleteById(ObjectId id) {
		return repository.deleteById(id).map(ServiceResponse::deleted) // успех: DELETED + id
				.orElseThrow(() -> new EntityNotFoundException(repository.getEntityClass().getSimpleName(),
						Map.of("id", id.toHexString())));
	}

	/**
	 * Удалить документ по id. Возвращает удалённый id или NotFound.
	 * 
	 * @param id строковый ObjectId сущности
	 * 
	 * @throws EntityNotFoundException если сущность не найдена
	 * 
	 * @return удалённый id
	 */
	public ServiceResponse<String> deleteById(String id) {
		return this.deleteById(new ObjectId(id));
	}

	/**
	 * Удалить один документ по фильтру. Возвращает удалённый id или NotFound.
	 * 
	 * @param filter фильтр
	 * 
	 * @throws EntityNotFoundException если сущность не найдена
	 * 
	 * @return удалённый id
	 */
	public ServiceResponse<String> deleteOne(Map<String, Object> filter) {
		return repository.deleteOne(filter).map(ServiceResponse::deleted)
				.orElseThrow(() -> new EntityNotFoundException(repository.getEntityClass().getSimpleName(), filter));
	}

	/**
	 * Удалить один документ по условию (key == value). Возвращает удалённый id или NotFound.
	 * 
	 * @param filter фильтр
	 * 
	 * @throws EntityNotFoundException если сущность не найдена
	 * 
	 * @return удалённый id
	 */
	public ServiceResponse<String> deleteOne(String key, Object value) {
		return repository.deleteOne(key, value).map(ServiceResponse::deleted).orElseThrow(
				() -> new EntityNotFoundException(repository.getEntityClass().getSimpleName(), Map.of(key, value)));
	}

	/**
	 * * Удалить множество
	 */

	/**
	 * Удалить несколько документов по фильтру. Если что-то удалили — DELETED(totalDeleted), если нет — NOTHING(0) как
	 * бизнес-неуспех delete-операции.
	 */
	public ServiceResponse<Long> deleteMany(Map<String, Object> filter) {
		long deleted = repository.deleteMany(filter);
		return (deleted > 0) ? ServiceResponse.deleted(deleted) : ServiceResponse.nothingDeleted(0L);
	}

	/**
	 * Удалить несколько документов по условию (key == value). Если что-то удалили — DELETED(totalDeleted), если нет —
	 * NOTHING(0).
	 */
	public ServiceResponse<Long> deleteMany(String key, Object value) {
		long deleted = repository.deleteMany(key, value);
		return (deleted > 0) ? ServiceResponse.deleted(deleted) : ServiceResponse.nothingDeleted(0L);
	}
}
