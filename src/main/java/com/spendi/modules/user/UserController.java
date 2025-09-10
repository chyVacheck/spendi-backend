
/**
 * @file UserController.java
 * @module modules/user
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user;

/**
 * ! java imports
 */
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseController;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.dto.IdDto;
import com.spendi.core.response.ApiSuccessResponse;
import com.spendi.core.types.ServiceProcessType;
import com.spendi.modules.files.FileService;
import com.spendi.modules.session.SessionEntity;
import com.spendi.modules.session.SessionService;
import com.spendi.core.files.UploadedFile;
import com.spendi.core.http.HttpStatusCode;
import com.spendi.core.files.DownloadedFile;

public class UserController extends BaseController {

	protected static UserController INSTANCE = new UserController();

	protected final UserService userService = UserService.getInstance();
	protected final SessionService sessionService = SessionService.getInstance();
	protected final FileService fileService = FileService.getInstance();

	protected UserController() {
		super(UserController.class.getSimpleName());
	}

	public static UserController getInstance() {
		return INSTANCE;
	}

	/**
	 * * Get
	 */

	/**
	 * GET /users/me: получение данных авторизованого пользователя
	 */
	public void getMe(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();

		UserEntity user = this.userService.getById(s.userId.toHexString()).getData();

		// Лог запроса сущности пользователя (несохраненный)
		this.info(
				"User get me",
				ctx.getRequestId(),
				detailsOf("userId", user.id.toHexString()));

		ctx.res().success(ApiSuccessResponse.ok(
				ctx.getRequestId(),
				"User " + user.getEmail(),
				user.getPrivateData()));
	}

	/**
	 * GET /users/{id}: получение данных пользователя по id
	 */
	public void getOneById(HttpContext ctx) {
		IdDto params = ctx.getValidParams(IdDto.class);

		// Лог запроса сущности пользователя (несохраненный)
		this.info(
				"User get by id",
				ctx.getRequestId(),
				detailsOf("userId", params.id));

		var resp = this.userService.getById(params.id);
		UserEntity user = resp.getData();

		ctx.res().success(ApiSuccessResponse.ok(
				ctx.getRequestId(),
				"User " + user.getEmail(),
				user.getPublicData()));
	}

	/**
	 * * Update
	 */

	/**
	 * ? === === === Avatar === === ===
	 */

	/**
	 * * Get
	 */

	/**
	 * GET /users/me/avatar: stream the avatar content inline
	 */
	public void getMeAvatar(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();

		UserEntity u = this.userService.getById(s.userId.toHexString()).getData();

		// Лог запроса получения аватара (несохраненный)
		this.info("user avatar get requested", ctx.getRequestId(),
				detailsOf("userId", u.id.toHexString()));

		if (!u.hasAvatar()) {
			this.debug("No avatar");
			ctx.res().status(HttpStatusCode.NO_CONTENT.getCode());
			return;
		}

		var fileResp = fileService.downloadOne(ctx.getRequestId(), u.profile.avatarFileId);
		DownloadedFile file = fileResp.getData();

		String disposition = "inline; filename=\""
				+ (file.getFilename() == null ? u.id.toHexString() : file.getFilename())
				+ "\"";
		ctx.res()
				.header("Content-Type", file.getContentType())
				.header("Content-Disposition", disposition)
				.sendBytes(file.getContent());
	}

	/**
	 * GET /users/{id}/avatar: stream the avatar content inline
	 */
	public void getAvatar(HttpContext ctx) {
		IdDto params = ctx.getValidParams(IdDto.class);

		// Лог запроса получения аватара (несохраненный)
		this.info("user avatar get requested", ctx.getRequestId(), detailsOf("userId", params.id));

		// read user to get file id
		var u = this.userService.getById(params.id).getData();
		if (!u.hasAvatar()) {
			this.debug("No avatar");
			ctx.res().status(HttpStatusCode.NO_CONTENT.getCode());
			return;
		}
		var fileResp = fileService.downloadOne(ctx.getRequestId(), u.profile.avatarFileId);
		DownloadedFile file = fileResp.getData();

		String disposition = "inline; filename=\""
				+ (file.getFilename() == null ? (params.id + "") : file.getFilename())
				+ "\"";
		ctx.res()
				.header("Content-Type", file.getContentType())
				.header("Content-Disposition", disposition)
				.sendBytes(file.getContent());
	}

	/**
	 * * Create
	 */

	/**
	 * POST /users/me/avatar: загружает аватар для авторизованого пользователя,
	 * возвращает url адрес аватарки
	 */
	public void uploadMeAvatar(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();

		UserEntity user = this.userService.getById(s.userId.toHexString()).getData();

		List<UploadedFile> files = ctx.getFiles();

		// Лог запроса загрузки аватара (несохраненный)
		this.info("user avatar upload requested", ctx.getRequestId(),
				detailsOf("userId", user.id.toHexString()));

		var resp = this.userService.uploadAvatar(ctx.getRequestId(), user.id.toHexString(), files.get(0));
		String url = resp.getData();

		if (resp.getProcess() == ServiceProcessType.CREATED) {
			ctx.res().success(ApiSuccessResponse.created(
					ctx.getRequestId(),
					"avatar created",
					detailsOf("url", url)));
		} else {
			ctx.res().success(ApiSuccessResponse.ok(
					ctx.getRequestId(),
					"avatar updated",
					detailsOf("url", url)));
		}
	}

	/**
	 * * Delete
	 */

	/**
	 * DELETE /users/me/avatar: полностью удаляет аватар авторизованого пользователя
	 */
	public void deleteMeAvatar(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();

		UserEntity user = this.userService.getById(s.userId.toHexString()).getData();

		// Лог запроса удаления аватара (несохраненный)
		this.info("user avatar delete requested", ctx.getRequestId(),
				detailsOf("userId", user.id.toHexString()));

		var resp = this.userService.deleteAvatar(ctx.getRequestId(), user.id.toHexString());
		String url = resp.getData();

		ctx.res().success(ApiSuccessResponse.ok(
				ctx.getRequestId(),
				"avatar deleted",
				detailsOf("url", url)));
	}

	/**
	 * ? === === === Avatar === === ===
	 */
}
