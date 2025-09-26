/**
 * @file SessionEntity.java
 * @module modules/session
 * 
 * Сущность пользовательской сессии.
 * Хранит данные о создании, активности и статусе отзыва.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.session;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * ! my imports
 */
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {
	/**
	 * Уникальный идентификатор сессии (_id).
	 */
	private ObjectId id;

	/**
	 * Идентификатор пользователя, которому принадлежит сессия.
	 */
	private ObjectId userId;

	/**
	 * Время создания сессии.
	 */
	private Instant createdAt;

	/**
	 * Время последней активности (last seen).
	 */
	private Instant lastSeenAt;

	/**
	 * Время истечения срока действия сессии. Используется в TTL-индексе MongoDB.
	 */
	private Instant expiresAt;

	/**
	 * Флаг, указывающий, что сессия была отозвана администратором или самим пользователем.
	 */
	private boolean revoked;

	/**
	 * IP-адрес, с которого была создана сессия.
	 */
	private String ip;

	/**
	 * User-Agent клиента, инициировавшего сессию.
	 */
	private String userAgent;

	/**
	 * Возвращает hex-строковое представление идентификатора сессии.
	 */
	public String getHexId() {
		return id.toHexString();
	}

	/**
	 * Возвращает hex-строковое представление идентификатора пользователя, которому принадлежит сессия.
	 */
	public String getUserHexId() {
		return userId.toHexString();
	}

	/**
	 * Является ли сессия активной на момент {@code now}. Активна, если не отозвана и {@code expiresAt} строго позже
	 * {@code now}.
	 */
	public boolean isActive(Instant now) {
		return !revoked && expiresAt != null && expiresAt.isAfter(now);
	}

	/**
	 * Является ли сессия активной «сейчас».
	 */
	public boolean isActive() {
		return isActive(Instant.now());
	}
}
