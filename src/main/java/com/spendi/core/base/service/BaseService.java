/**
 * @file BaseService.java
 * @module com.spendi.core.base.service
 *
 * @description
 * Базовый сервисный класс без привязки к репозиторию.
 * Держит контекст (имя/тип класса), предоставляет удобную точку
 * для общих сервисных хелперов.
 *
 * Наследуется: специфичные сервисы ИЛИ {@link BaseRepositoryService}.
 *
 * @author
 * Dmytro Shakh
 */

package com.spendi.core.base.service;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseClass;
import com.spendi.core.types.EClassType;

public abstract class BaseService extends BaseClass {

	public BaseService(String className) {
		super(EClassType.SERVICE, className);
	}

}
