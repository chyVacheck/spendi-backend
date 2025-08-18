/**
 * @module com.spendi.core.base.CoreClass
 * @description
 * Базовый абстрактный класс для всех классов приложения.
 * Служит фундаментом, от которого наследуются как системные, так и прикладные компоненты.
 *
 * <p>Основные задачи:</p>
 * <ul>
 *   <li>Хранение типа класса ({@link com.spendi.core.types.EClassType}) и человекочитаемого имени</li>
 *   <li>Единый контракт для логирования и отладки (имя и тип доступны во всех наследниках)</li>
 * </ul>
 *
 * <p>Наследуется классами: BaseController, BaseService, BaseRepository, BaseMapper, BaseRouter,
 * BaseMiddleware, BaseDatabaseManager и др.</p>
 *
 * <p>Пример:</p>
 * <pre>{@code
 * public final class UserService extends CoreClass {
 *     public UserService() {
 *         super(EClassType.SERVICE, "UserService");
 *     }
 * }
 * }</pre>
 *
 * <p><b>Требования:</b> поля неизменяемы, null не допускается. В случае некорректных данных
 * конструктор выбрасывает {@link IllegalArgumentException}.</p>
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.base;

/**
 * ! java imports
 */
import java.util.Objects;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;

public abstract class CoreClass {

	/** Тип класса (Controller, Service, Repository и т.д.) */
	private final EClassType classType;

	/** Имя класса (используется в логах и отладке) */
	private final String className;

	/**
	 * Базовый конструктор CoreClass.
	 *
	 * @param classType тип класса (нельзя null)
	 * @param className человекочитаемое имя класса (нельзя null/пустое/blank)
	 * @throws IllegalArgumentException при некорректных аргументах
	 */
	protected CoreClass(final EClassType classType, final String className) {
		this.classType = Objects.requireNonNull(classType, "classType must not be null");
		if (className == null || className.isBlank()) {
			throw new IllegalArgumentException("className must not be null/blank");
		}
		this.className = className;
	}

	/**
	 * Возвращает тип класса.
	 *
	 * @return тип класса из {@link EClassType}
	 */
	public EClassType getClassType() {
		return classType;
	}

	/**
	 * Возвращает имя класса.
	 *
	 * @return человекочитаемое имя класса
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Короткое читабельное представление для логов.
	 */
	@Override
	public String toString() {
		return classType + "(" + className + ")";
	}

	/**
	 * Равенство определяется по типу и имени.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CoreClass that))
			return false;
		return classType == that.classType && className.equals(that.className);
	}

	@Override
	public int hashCode() {
		return Objects.hash(classType, className);
	}
}
