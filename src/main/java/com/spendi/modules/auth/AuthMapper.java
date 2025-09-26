
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
import com.spendi.modules.user.cmd.UserCreateCmd;

public class AuthMapper extends BaseMapper {
	final private static AuthMapper INSTANCE = new AuthMapper();

	private AuthMapper() {
		super(AuthMapper.class.getSimpleName());
	}

	public static AuthMapper getInstance() {
		return INSTANCE;
	}

	public UserCreateCmd toCmd(RegisterDto dto) {
		UserCreateCmd cmd = new UserCreateCmd();

		UserCreateCmd.ProfileBlock profile = new UserCreateCmd.ProfileBlock();

		profile.setEmail(dto.getEmail());
		profile.setFirstName(dto.getFirstName());
		profile.setLastName(dto.getLastName());

		UserCreateCmd.SecurityBlock security = new UserCreateCmd.SecurityBlock();

		security.setPassword(dto.getPassword());

		cmd.setProfile(profile);
		cmd.setSecurity(security);

		return cmd;
	}
}
