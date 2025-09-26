
/**
 * @file SessionCreateCmd.java
 * @module modules/session/cmd
 * 
 * DTO для создания новой пользовательской сессии.
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.session.cmd;

/**
 * ! lib imports
 */
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Содержит данные, необходимые для создания сессии: userId, ip, userAgent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionCreateCmd {

	/**
	 * Идентификатор пользователя (строка 24 hex-символа).
	 */
	private String userId;

	/**
	 * IP-адрес клиента (может быть null).
	 */
	private String ip;

	/**
	 * User-Agent клиента.
	 */
	private String userAgent;
}