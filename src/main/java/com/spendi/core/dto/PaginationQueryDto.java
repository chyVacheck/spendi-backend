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

public class PaginationQueryDto {
    @Min(value = 1, message = "Page must be at least 1")
    public Integer page = 1;

    @Min(value = 1, message = "Limit must be at least 1")
    public Integer limit = 250;

    public Integer getPage() {
        return page != null ? page : 1;
    }

    public Integer getLimit() {
        return limit != null ? limit : 250;
    }
}