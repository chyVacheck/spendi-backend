
/**
 * @file ListQueryParams.java
 * @module shared/dto
 * @description
 * DTO для параметров запроса списка сущностей.
 * Расширяет {@link PaginationQueryDto}, добавляя фильтрацию по удалённым записям.
 * 
 * @param includeDeleted Включать ли удаленные записи
 * @param onlyDeleted Показывать только удаленные записи
 * 
 * @see PaginationQueryDto
 * 
 * @author Dmytro Shakh
 */

package com.spendi.shared.dto;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListQueryDto extends PaginationQueryDto {

	private boolean includeDeleted = false;
	private boolean onlyDeleted = false;

	@AssertTrue(message = "If onlyDeleted is true, includeDeleted must also be true")
	public boolean isValidCombination() {
		return !onlyDeleted || includeDeleted;
	}
}
