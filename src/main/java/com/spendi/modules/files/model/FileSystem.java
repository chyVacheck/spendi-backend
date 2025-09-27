
/**
 * @file FileSystem.java
 * @module modules/files/model 
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.files.model;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spendi.shared.model.meta.BaseMeta;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileSystem {
	BaseMeta meta;
}
