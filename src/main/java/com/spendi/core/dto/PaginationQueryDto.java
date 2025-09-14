/**
 * @file PaginationQueryDto.java
 * @module core/dto
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.dto;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.Min;

/**
 * ! my imports
 */
import com.spendi.config.ApiConfig;

public class PaginationQueryDto {
    @Min(value = 1, message = "Page must be at least 1")
    public int page = ApiConfig.getConfig().getDefaultPage();

    @Min(value = 1, message = "Limit must be at least 1")
    public int limit = ApiConfig.getConfig().getDefaultLimit();

    /**
     * Получить значение страницы.
     * 
     * @return значение страницы
     */
    public int getPage() {
        return page;
    }

    /**
     * Получить значение лимита.
     * 
     * @return значение лимита
     */
    public int getLimit() {
        return Math.max(limit, ApiConfig.getConfig().getMaxLimit());
    }
}