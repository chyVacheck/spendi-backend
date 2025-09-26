/**
 * @file PaginationQueryDto.java
 * @module core/dto
 *
 * @author Dmytro Shakh
 */

package com.spendi.shared.dto;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * ! my imports
 */
import com.spendi.config.ApiConfig;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationQueryDto {
	@Min(value = 1, message = "Page must be at least 1")
	private int page = ApiConfig.getConfig().getDefaultPage();

	@Min(value = 1, message = "Limit must be at least 1")
	private int limit = ApiConfig.getConfig().getDefaultLimit();

	/**
	 * Получить значение лимита.
	 * 
	 * @return значение лимита, не выше максимального
	 */
	public int getLimit() {
		return Math.min(limit, ApiConfig.getConfig().getMaxLimit());
	}
}