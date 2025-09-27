/**
 * @file BaseMapper.java
 * @module core/base
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base;

/**
 * ! lib imports
 */
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

/**
 * ! my imports
 */
import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.util.Optional;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Collections;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;
import com.spendi.core.utils.InstantUtils;

public abstract class BaseMapper<TEntity> extends BaseClass {

	/** Класс сущности (может использоваться для рефлексии/маппинга/логирования). */
	protected final Class<TEntity> entityClass;
	protected final String collection;

	protected BaseMapper(String className, Class<TEntity> entityClass, String collection) {
		super(EClassType.MAPPER, className);
		this.entityClass = entityClass;
		this.collection = collection;
	}

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

	/** Безопасно конвертит raw в ObjectId, бросает IAE при некорректном значении. */
	protected ObjectId readObjectId(Object raw) {
		if (raw == null)
			return null;
		if (raw instanceof ObjectId oid)
			return oid;
		try {
			return new ObjectId(raw.toString());
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Invalid ObjectId value: " + raw, ex);
		}
	}

	/** Записывает ключ только если значение не null. Возвращает тот же Document для чейнинга. */
	protected <T> Document putIfNotNull(Document d, String key, T value) {
		if (value != null)
			d.put(key, value);
		return d;
	}

	protected IllegalStateException fatalAndThrow(String message, Map<String, Object> details) {
		this.fatal(message, null, details, true);
		throw new IllegalStateException(message + " :: " + details);
	}

	protected IllegalStateException missing(String kind, String field, ObjectId idForLog) {
		return fatalAndThrow("Missing required " + kind + " field", baseDetails(idForLog, field));
	}

	protected String safeHex(ObjectId id) {
		return id != null ? id.toHexString() : null;
	}

	private Map<String, Object> baseDetails(ObjectId idForLog, String path) {
		return detailsOf("entityClass", entityClass.getSimpleName(), "collection", collection, "entityId",
				safeHex(idForLog), "path", path);
	}

	/** Типобезопасный get с обязательным наличием поля. */
	protected <T> T req(Document d, String field, Class<T> type, ObjectId idForLog) {
		T v = d.get(field, type);
		if (v == null)
			missing("field", field, idForLog);
		return v;
	}

	/** === Strings & subDocs === */

	protected String reqString(Document d, String field, ObjectId idForLog) {
		String v = d.getString(field);
		if (v == null)
			missing("String", field, idForLog);
		return v;
	}

	protected String reqString(Document d, String field, ObjectId idForLog, String pathAlias) {
		String v = d.getString(field);
		if (v == null)
			fatalAndThrow("Missing required String field", baseDetails(idForLog, pathAlias));
		return v;
	}

	protected Optional<String> optString(Document d, String field) {
		return Optional.ofNullable(d.getString(field));
	}

	protected Document reqSubDoc(Document parent, String field, ObjectId idForLog) {
		Document v = parent.get(field, Document.class);
		if (v == null)
			missing("sub document", field, idForLog);
		return v;
	}

	// рядом с существующим reqSubDoc(parent, field, idForLog)
	protected Document reqSubDoc(Document parent, String field, ObjectId idForLog, String pathAlias) {
		Document v = parent.get(field, Document.class);
		if (v == null) {
			fatalAndThrow("Missing required sub document", baseDetails(idForLog, pathAlias));
		}
		return v;
	}

	protected Optional<Document> optSubDoc(Document d, String field) {
		return Optional.ofNullable(d.get(field, Document.class));
	}

	/** === Booleans === */
	protected Boolean reqBoolean(Document d, String field, ObjectId idForLog) {
		Object raw = d.get(field);
		if (raw instanceof Boolean b)
			return b;
		if (raw == null)
			missing("Boolean", field, idForLog);
		// допускаем "true"/"false", "1"/"0", 1/0
		if (raw instanceof String s) {
			String s1 = s.trim().toLowerCase();
			if (s1.equals("true") || s1.equals("1"))
				return Boolean.TRUE;
			if (s1.equals("false") || s1.equals("0"))
				return Boolean.FALSE;
		}
		if (raw instanceof Number n) {
			return n.intValue() != 0;
		}
		fatalAndThrow("Invalid Boolean value", baseDetails(idForLog, field));
		return null; // unreachable
	}

	protected Boolean reqBoolean(Document d, String field, ObjectId idForLog, String pathAlias) {
		Object raw = d.get(field);
		if (raw instanceof Boolean b)
			return b;
		if (raw == null)
			fatalAndThrow("Missing required Boolean field", baseDetails(idForLog, pathAlias));
		if (raw instanceof String s) {
			String s1 = s.trim().toLowerCase();
			if (s1.equals("true") || s1.equals("1"))
				return Boolean.TRUE;
			if (s1.equals("false") || s1.equals("0"))
				return Boolean.FALSE;
		}
		if (raw instanceof Number n)
			return n.intValue() != 0;
		fatalAndThrow("Invalid Boolean value", baseDetails(idForLog, pathAlias));
		return null; // unreachable
	}

	protected Optional<Boolean> optBoolean(Document d, String field) {
		Object raw = d.get(field);
		if (raw == null)
			return Optional.empty();
		if (raw instanceof Boolean b)
			return Optional.of(b);
		if (raw instanceof String s) {
			String s1 = s.trim().toLowerCase();
			if (s1.equals("true") || s1.equals("1"))
				return Optional.of(Boolean.TRUE);
			if (s1.equals("false") || s1.equals("0"))
				return Optional.of(Boolean.FALSE);
		}
		if (raw instanceof Number n)
			return Optional.of(n.intValue() != 0);
		return Optional.empty();
	}

	/** === Enums === */
	protected <E extends Enum<E>> E reqEnum(Document d, String field, Class<E> enumType, ObjectId idForLog) {
		String raw = d.getString(field);
		if (raw == null)
			missing("Enum", field, idForLog);
		try {
			return Enum.valueOf(enumType, raw);
		} catch (IllegalArgumentException ex) {
			fatalAndThrow("Invalid Enum value", detailsOf("value", raw, "enum", enumType.getSimpleName()));
			return null; // unreachable
		}
	}

	protected <E extends Enum<E>> E reqEnum(Document d, String field, Class<E> enumType, ObjectId idForLog,
			String pathAlias) {
		String raw = d.getString(field);
		if (raw == null)
			fatalAndThrow("Missing required Enum field", baseDetails(idForLog, pathAlias));
		try {
			return Enum.valueOf(enumType, raw);
		} catch (IllegalArgumentException ex) {
			fatalAndThrow("Invalid Enum value",
					detailsOf("value", raw, "enum", enumType.getSimpleName(), "path", pathAlias));
			return null; // unreachable
		}
	}

	/** === ObjectId === */
	protected ObjectId reqObjectId(Document d, String field, ObjectId idForLog) {
		Object raw = d.get(field);
		if (raw instanceof ObjectId oid)
			return oid;
		if (raw == null)
			missing("ObjectId", field, idForLog);
		return new ObjectId(raw.toString());
	}

	protected Optional<ObjectId> optObjectId(Document d, String field) {
		Object raw = d.get(field);
		if (raw == null)
			return Optional.empty();
		return Optional.of(raw instanceof ObjectId oid ? oid : new ObjectId(raw.toString()));
	}

	protected ObjectId reqObjectId(Document d, String field, ObjectId idForLog, String pathAlias) {
		Object raw = d.get(field);
		if (raw instanceof ObjectId oid)
			return oid;
		if (raw == null)
			fatalAndThrow("Missing required ObjectId field", baseDetails(idForLog, pathAlias));
		return new ObjectId(raw.toString());
	}

	/** Список ObjectId; допускаем строки/ObjectId. */
	protected List<ObjectId> reqObjectIdList(Document d, String field, ObjectId idForLog) {
		Object raw = d.get(field);
		if (raw == null)
			missing("Array", field, idForLog);
		if (!(raw instanceof List<?>)) {
			fatalAndThrow("Field is not an Array", baseDetails(idForLog, field));
		}
		List<?> list = (List<?>) raw; // явная инициализация, чтобы не смущало
		List<ObjectId> out = new ArrayList<>(list.size());
		for (Object o : list) {
			if (o == null)
				fatalAndThrow("Array contains null", baseDetails(idForLog, field));
			if (o instanceof ObjectId oid)
				out.add(oid);
			else
				out.add(new ObjectId(o.toString()));
		}
		return Collections.unmodifiableList(out);
	}

	/** === Instants === */
	protected Instant reqInstant(Document d, String field, ObjectId idForLog) {
		Object raw = d.get(field);
		Instant v = InstantUtils.getInstantOrNull(raw);
		if (v == null)
			missing("Instant", field, idForLog);
		return v;
	}

	protected Instant reqInstant(Document d, String field, ObjectId idForLog, String pathAlias) {
		Object raw = d.get(field);
		Instant v = InstantUtils.getInstantOrNull(raw);
		if (v == null)
			fatalAndThrow("Missing required Instant field", baseDetails(idForLog, pathAlias));
		return v;
	}

	protected Optional<Instant> optInstant(Document d, String field) {
		Object raw = d.get(field);
		return Optional.ofNullable(InstantUtils.getInstantOrNull(raw));
	}

	/** === Numbers: Integer, Long, Double, BigDecimal (Decimal128) === */
	protected Integer reqInt(Document d, String field, ObjectId idForLog) {
		Number n = coerceNumber(d.get(field), field, idForLog);
		return n.intValue();
	}

	protected Integer reqInt(Document d, String field, ObjectId idForLog, String pathAlias) {
		Number n = coerceNumber(d.get(field), pathAlias, idForLog);
		return n.intValue();
	}

	protected Optional<Integer> optInt(Document d, String field) {
		return optNumber(d.get(field)).map(Number::intValue);
	}

	protected Long reqLong(Document d, String field, ObjectId idForLog) {
		Number n = coerceNumber(d.get(field), field, idForLog);
		return n.longValue();
	}

	protected Optional<Long> optLong(Document d, String field) {
		return optNumber(d.get(field)).map(Number::longValue);
	}

	protected Double reqDouble(Document d, String field, ObjectId idForLog) {
		Number n = coerceNumber(d.get(field), field, idForLog);
		return n.doubleValue();
	}

	protected Optional<Double> optDouble(Document d, String field) {
		return optNumber(d.get(field)).map(Number::doubleValue);
	}

	protected BigDecimal reqBigDecimal(Document d, String field, ObjectId idForLog) {
		Object raw = d.get(field);
		BigDecimal bd = coerceBigDecimal(raw);
		if (bd == null)
			missing("BigDecimal", field, idForLog);
		return bd;
	}

	protected Optional<BigDecimal> optBigDecimal(Document d, String field) {
		return Optional.ofNullable(coerceBigDecimal(d.get(field)));
	}

	/** Вспомогательные приведения чисел. Поддержка Integer/Long/Double/Decimal128/String. */
	private Optional<Number> optNumber(Object raw) {
		if (raw == null)
			return Optional.empty();
		if (raw instanceof Number n)
			return Optional.of(n);
		if (raw instanceof Decimal128 dec)
			return Optional.of(dec.bigDecimalValue());
		if (raw instanceof String s) {
			String t = s.trim();
			if (t.isEmpty())
				return Optional.empty();
			try {
				// пробуем long, потом double (без BigDecimal здесь, для простоты)
				if (t.matches("^[+-]?\\d+$"))
					return Optional.of(Long.parseLong(t));
				return Optional.of(Double.parseDouble(t));
			} catch (NumberFormatException ignore) {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	private Number coerceNumber(Object raw, String field, ObjectId idForLog) {
		Optional<Number> on = optNumber(raw);
		if (on.isEmpty())
			missing("Number", field, idForLog);
		return on.get();
	}

	private BigDecimal coerceBigDecimal(Object raw) {
		if (raw == null)
			return null;
		if (raw instanceof BigDecimal bd)
			return bd;
		if (raw instanceof Decimal128 dec)
			return dec.bigDecimalValue();
		if (raw instanceof Number n)
			return new BigDecimal(n.toString());
		if (raw instanceof String s) {
			try {
				return new BigDecimal(s.trim());
			} catch (NumberFormatException ignore) {
				return null;
			}
		}
		return null;
	}
}
