
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
import com.spendi.core.files.UploadedFile;
import com.spendi.core.http.HttpStatusCode;
import com.spendi.core.files.DownloadedFile;
import com.spendi.modules.payment.PaymentMethodEntity;
import com.spendi.modules.payment.PaymentMethodService;
import com.spendi.modules.payment.dto.PaymentMethodCreateDto;
import com.spendi.modules.payment.dto.PaymentMethodIdParams;
import com.spendi.modules.payment.dto.PaymentMethodOrderDto;

public class UserController extends BaseController {

	protected static UserController INSTANCE = new UserController();

	private final UserService userService = UserService.getInstance();
	private final FileService fileService = FileService.getInstance();
	private final PaymentMethodService paymentService = PaymentMethodService.getInstance();

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

		var fileResp = this.fileService.downloadOne(ctx.getRequestId(), u.profile.avatarFileId);
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
		var fileResp = this.fileService.downloadOne(ctx.getRequestId(), u.profile.avatarFileId);
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
	 * ? === === Payment Methods === ===
	 */

	/**
	 * POST /users/me/payment-methods
	 */
	public void addPaymentMethod(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();
		PaymentMethodCreateDto dto = ctx.getValidBody(PaymentMethodCreateDto.class);

		// Assemble entity
		PaymentMethodEntity e = new PaymentMethodEntity();
		var info = new PaymentMethodEntity.Info();
		info.type = dto.info.type;
		info.name = dto.info.name;
		info.currency = dto.info.currency;
		info.order = dto.info.order;
		info.tags = dto.info.tags;
		e.info = info;

		var details = new PaymentMethodEntity.Details();
		if (dto.details != null) {
			if (dto.details.card != null) {
				var c = new PaymentMethodEntity.Card();
				c.brand = dto.details.card.brand;
				c.last4 = dto.details.card.last4;
				c.expMonth = dto.details.card.expMonth;
				c.expYear = dto.details.card.expYear;
				details.card = c;
			}
			if (dto.details.bank != null) {
				var b = new PaymentMethodEntity.Bank();
				b.bankName = dto.details.bank.bankName;
				b.accountMasked = dto.details.bank.accountMasked;
				details.bank = b;
			}
			if (dto.details.wallet != null) {
				var w = new PaymentMethodEntity.Wallet();
				w.provider = dto.details.wallet.provider;
				w.handle = dto.details.wallet.handle;
				details.wallet = w;
			}
		}
		e.details = details;

		var sys = new PaymentMethodEntity.System();
		sys.status = PaymentMethodEntity.EPaymentMethodStatus.Active;
		sys.meta = new PaymentMethodEntity.Meta();
		e.system = sys;

		var created = this.paymentService.createForUser(ctx.getRequestId(), s.userId.toHexString(), e).getData();

		ctx.res().success(ApiSuccessResponse.created(
				ctx.getRequestId(),
				"payment method created",
				created.getPublicData()));
	}

	/**
	 * DELETE /users/me/payment-methods/{id}
	 */
	public void deletePaymentMethod(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();
		var params = ctx.getValidParams(PaymentMethodIdParams.class);

		this.paymentService.deleteForUser(ctx.getRequestId(), s.userId.toHexString(), params.pmId);

		ctx.res().success(ApiSuccessResponse.ok(
				ctx.getRequestId(),
				"payment method deleted",
				detailsOf("id", params.pmId)));
	}

	/**
	 * PUT /users/me/payment-methods/{id}/order
	 */
	public void updatePaymentMethodOrder(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();
		var params = ctx.getValidParams(PaymentMethodIdParams.class);
		PaymentMethodOrderDto dto = ctx.getValidBody(PaymentMethodOrderDto.class);

		var updated = this.paymentService.updateOrder(ctx.getRequestId(), s.userId.toHexString(), params.pmId,
				dto.order);

		ctx.res().success(ApiSuccessResponse.ok(
				ctx.getRequestId(),
				"payment method order updated",
				updated.getData().getPublicData()));
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
