/**
 * @file FileDownloadQuery.java
 * @module modules/files/dto
 */

package com.spendi.modules.files.dto;

/**
 * ! lib imports
 */
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class FileDownloadQuery {
	/**
	 * Boolean-like flag: true/false/1/0/yes/no (case-insensitive).
	 * Optional; defaults to false.
	 */
	@Pattern(regexp = "^(?i:true|false|1|0|yes|no)$", message = "download must be boolean-like")
	public String download;

	public boolean asAttachment() {
		if (download == null)
			return false;
		String s = download.trim().toLowerCase();
		return s.equals("1") || s.equals("true") || s.equals("yes");
	}
}
