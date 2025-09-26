/**
 * @file PresentField.java
 * @module core/types
 *
 * Обёртка для PATCH-полей с 3 состояниями:
 * - ABSENT: поле не прислано (present = false)
 * - PRESENT+NULL: прислано "key": null
 * - PRESENT+VALUE: прислано "key": <value>
 *
 * ВАЖНО: если поле ОТСУТСТВУЕТ в JSON, Jackson НЕ вызывает десериализацию свойства.
 * Поэтому в DTO такие поля должны инициализироваться значением PresentField.absent()
 * (и при использовании Lombok @Builder — через @Builder.Default).
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.types;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.*;

/**
 * ! java imports
 */
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Обёртка для PATCH-полей: - ABSENT: поле не прислано (present=false) - PRESENT+NULL: прислано "key": null -
 * PRESENT+VALUE: прислано "key": <value>
 */
@JsonDeserialize(using = PresentField.Deser.class)
public final class PresentField<T> {

	private final boolean present;
	private final T value;

	private PresentField(boolean present, T value) {
		this.present = present;
		this.value = value;
	}

	/** Поле отсутствует в JSON. */
	public static <T> PresentField<T> absent() {
		return new PresentField<>(false, null);
	}

	/** Поле присутствует со значением (может быть null). */
	public static <T> PresentField<T> of(T value) {
		return new PresentField<>(true, value);
	}

	/** Явно прислано null. */
	public static <T> PresentField<T> ofNull() {
		return new PresentField<>(true, null);
	}

	/** Было ли поле прислано в запросе. */
	public boolean isPresent() {
		return present;
	}

	/** Поле отсутствовало в JSON. */
	public boolean isAbsent() {
		return !present;
	}

	/** Прислано явно значение null. */
	public boolean isNull() {
		return present && value == null;
	}

	/** Прислано непустое значение. */
	public boolean isValue() {
		return present && value != null;
	}

	/** Значение поля (может быть null, если прислано null). */
	public T get() {
		return value;
	}

	/** Optional: empty, если поле не прислано; иначе Optional.ofNullable(value). */
	public Optional<T> asOptional() {
		return present ? Optional.ofNullable(value) : Optional.empty();
	}

	/** Удобный маппинг: если present -> применить mapper, сохранив present-флаг. */
	public <R> PresentField<R> map(Function<? super T, ? extends R> mapper) {
		if (!present)
			return PresentField.absent();
		if (value == null)
			return PresentField.ofNull();
		return PresentField.of(mapper.apply(value));
	}

	/** Выполнить действие, если прислано непустое значение. */
	public void ifPresent(Consumer<? super T> consumer) {
		if (isValue())
			consumer.accept(value);
	}

	/** Вернуть значение или default, если ABSENT или NULL. */
	public T orElse(T other) {
		return isValue() ? value : other;
	}

	/** Вернуть значение или supplier.get(), если ABSENT или NULL. */
	public T orElseGet(Supplier<? extends T> supplier) {
		return isValue() ? value : supplier.get();
	}

	@Override
	public String toString() {
		return present ? "PresentField[present,value=" + value + "]" : "PresentField[absent]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(present, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PresentField<?> that))
			return false;
		return present == that.present && Objects.equals(value, that.value);
	}

	/**
	 * Десериализатор: missing -> absent(), null -> ofNull(), иначе -> of(converted).
	 */
	public static final class Deser extends JsonDeserializer<PresentField<?>> implements ContextualDeserializer {
		private JavaType valueType; // тип T внутри PatchField<T>

		public Deser() {}

		private Deser(JavaType valueType) {
			this.valueType = valueType;
		}

		@Override
		public PresentField<?> getNullValue(DeserializationContext ctxt) {
			// Явно обрабатываем JSON null как PRESENT+NULL
			return PresentField.ofNull();
		}

		@Override
		public PresentField<?> deserialize(JsonParser p, DeserializationContext ctxt) throws java.io.IOException {
			final ObjectCodec codec = p.getCodec();
			final JsonNode node = codec.readTree(p);

			if (node == null || node.isMissingNode()) {
				// Сюда практически не попадём для отсутствующего ключа, но оставим на всякий случай.
				return PresentField.absent();
			}

			if (node.isNull()) {
				return PresentField.ofNull();
			}

			// Конвертация с учётом целевого типа.
			Object value;
			if (codec instanceof ObjectMapper om) {
				// С ObjectMapper можно сохранять JavaType целиком.
				value = (valueType != null) ? om.convertValue(node, valueType) : om.treeToValue(node, Object.class);
			} else {
				// Fallback: без ObjectMapper теряем generics, но сохраняем raw-класс.
				final Class<?> raw = (valueType != null) ? valueType.getRawClass() : Object.class;
				value = ctxt.readTreeAsValue(node, raw);
			}

			return PresentField.of(value);
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty prop) {
			if (prop != null) {
				// PatchField<T> -> достаём T
				final JavaType wrapper = prop.getType();
				final JavaType inner = wrapper.containedTypeOrUnknown(0);
				return new Deser(inner);
			}
			return this;
		}
	}
}