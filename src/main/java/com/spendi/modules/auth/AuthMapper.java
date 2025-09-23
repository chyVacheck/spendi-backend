
/**
 * @file AuthMapper.java
 * @module modules/auth
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.auth;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMapper;
import com.spendi.modules.auth.dto.RegisterDto;
import com.spendi.modules.user.command.UserCreateCommand;

public class AuthMapper extends BaseMapper {
	final private static AuthMapper INSTANCE = new AuthMapper();

	private AuthMapper() {
		super(AuthMapper.class.getSimpleName());
	}

	public static AuthMapper getInstance() {
		return INSTANCE;
	}

	public UserCreateCommand toCmd(RegisterDto dto) {
		UserCreateCommand command = new UserCreateCommand();

		UserCreateCommand.ProfileBlock profile = new UserCreateCommand.ProfileBlock();

		profile.setEmail(dto.getEmail());
		profile.setFirstName(dto.getFirstName());
		profile.setLastName(dto.getLastName());

		UserCreateCommand.SecurityBlock security = new UserCreateCommand.SecurityBlock();

		security.setPassword(dto.getPassword());

		command.setProfile(profile);
		command.setSecurity(security);

		return command;
	}
}
