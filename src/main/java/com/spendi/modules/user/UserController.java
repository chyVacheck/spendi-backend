
/**
 * @file UserController.java
 * @module modules/user
 *
 * @description
 * Контроллер для управления пользователями и их данными.
 * Обрабатывает HTTP-запросы связанные с:
 * - Получением информации о пользователях
 * - Управлением аватарами пользователей
 * - Управлением методами оплаты пользователей
 * 
 * Использует паттерн Singleton для обеспечения единственного экземпляра.
 * Все операции требуют аутентификации через сессию.
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

/**
 * Контроллер для управления пользователями и связанными с ними данными.
 * 
 * <p>Предоставляет REST API endpoints для:
 * <ul>
 *   <li>Получения информации о пользователях (публичной и приватной)</li>
 *   <li>Управления аватарами (загрузка, получение, удаление)</li>
 *   <li>Управления методами оплаты пользователей</li>
 * </ul>
 * 
 * <p>Все операции с пользовательскими данными требуют валидной сессии.
 * Контроллер использует паттерн Singleton для управления экземпляром.
 * 
 * @see UserService
 * @see FileService
 * @see PaymentMethodService
 */
public class UserController extends BaseController {

	/** Единственный экземпляр контроллера (Singleton pattern) */
	protected static UserController INSTANCE = new UserController();

	/** Сервис для работы с пользователями */
	private final UserService userService = UserService.getInstance();
	/** Сервис для работы с файлами (аватары) */
	private final FileService fileService = FileService.getInstance();
	/** Сервис для работы с методами оплаты */
	private final PaymentMethodService paymentService = PaymentMethodService.getInstance();

	/**
	 * Приватный конструктор для реализации паттерна Singleton.
	 * Инициализирует контроллер с именем класса для логирования.
	 */
	protected UserController() {
		super(UserController.class.getSimpleName());
	}

	/**
	 * Получить единственный экземпляр контроллера.
	 * 
	 * @return экземпляр UserController
	 */
	public static UserController getInstance() {
		return INSTANCE;
	}

	/**
	 * * Get
	 */

	/**
	 * Получить данные текущего авторизованного пользователя.
	 * 
	 * <p>Endpoint: {@code GET /users/me}
	 * 
	 * <p>Возвращает приватные данные пользователя (включая email и другую
	 * конфиденциальную информацию), доступные только самому пользователю.
	 * 
	 * <p>Требования:
	 * <ul>
	 *   <li>Валидная сессия аутентификации</li>
	 *   <li>Активный пользователь в системе</li>
	 * </ul>
	 * 
	 * @param ctx HTTP контекст запроса с данными сессии
	 * 
	 * @throws UnauthorizedException если сессия невалидна
	 * @throws EntityNotFoundException если пользователь не найден
	 * 
	 * @apiNote Возвращает приватные данные пользователя
	 * @see UserEntity#getPrivateData()
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
	 * Получить публичные данные пользователя по его ID.
	 * 
	 * <p>Endpoint: {@code GET /users/{id}}
	 * 
	 * <p>Возвращает только публичные данные пользователя (без конфиденциальной
	 * информации типа email). Доступно для всех аутентифицированных пользователей.
	 * 
	 * <p>Параметры URL:
	 * <ul>
	 *   <li>{@code id} - ObjectId пользователя в формате строки</li>
	 * </ul>
	 * 
	 * @param ctx HTTP контекст с параметрами запроса
	 * 
	 * @throws BadRequestException если ID имеет неверный формат
	 * @throws EntityNotFoundException если пользователь не найден
	 * 
	 * @apiNote Возвращает только публичные данные пользователя
	 * @see UserEntity#getPublicData()
	 * @see IdDto
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
	 * Получить аватар текущего авторизованного пользователя.
	 * 
	 * <p>Endpoint: {@code GET /users/me/avatar}
	 * 
	 * <p>Возвращает бинарное содержимое файла аватара с соответствующими
	 * HTTP заголовками для отображения в браузере (Content-Type, Content-Disposition).
	 * 
	 * <p>Поведение:
	 * <ul>
	 *   <li>Если аватар существует - возвращает файл с кодом 200</li>
	 *   <li>Если аватар отсутствует - возвращает код 204 (No Content)</li>
	 * </ul>
	 * 
	 * <p>HTTP заголовки ответа:
	 * <ul>
	 *   <li>{@code Content-Type} - MIME тип файла</li>
	 *   <li>{@code Content-Disposition: inline} - для отображения в браузере</li>
	 * </ul>
	 * 
	 * @param ctx HTTP контекст запроса
	 * 
	 * @throws UnauthorizedException если сессия невалидна
	 * @throws EntityNotFoundException если пользователь не найден
	 * 
	 * @apiNote Возвращает бинарные данные файла или 204 если аватар отсутствует
	 */
	public void getMeAvatar(HttpContext ctx) {
		// Извлекаем информацию о текущей сессии для получения ID пользователя
		SessionEntity s = ctx.getAuthSession();

		// Получаем полные данные пользователя из базы данных по ID из сессии
		UserEntity u = this.userService.getById(s.userId.toHexString()).getData();

		// Логируем запрос для отслеживания активности пользователей
		this.info("user avatar get requested", ctx.getRequestId(),
				detailsOf("userId", u.id.toHexString()));

		// Проверяем, есть ли у пользователя аватар
		// Если аватар отсутствует, возвращаем HTTP 204 (No Content)
		if (!u.hasAvatar()) {
			this.debug("No avatar found for user");
			ctx.res().status(HttpStatusCode.NO_CONTENT.getCode());
			return;
		}

		// Загружаем файл аватара из файлового хранилища по ID файла
		var fileResp = this.fileService.downloadOne(ctx.getRequestId(), u.profile.avatarFileId);
		DownloadedFile file = fileResp.getData();

		// Формируем заголовок Content-Disposition для inline отображения
		// Используем оригинальное имя файла или ID пользователя как fallback
		String disposition = "inline; filename=\""
				+ (file.getFilename() == null ? u.id.toHexString() : file.getFilename())
				+ "\"";
		
		// Отправляем файл клиенту с правильными заголовками
		// Content-Type определяет MIME тип для корректного отображения
		// Content-Disposition: inline позволяет отображать изображение в браузере
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
	 * Загрузить или обновить аватар текущего пользователя.
	 * 
	 * <p>Endpoint: {@code POST /users/me/avatar}
	 * 
	 * <p>Принимает multipart/form-data с файлом изображения и сохраняет его
	 * как аватар пользователя. Если аватар уже существует - заменяет его.
	 * 
	 * <p>Требования к файлу:
	 * <ul>
	 *   <li>Должен быть изображением (JPEG, PNG, GIF и т.д.)</li>
	 *   <li>Размер файла ограничен конфигурацией системы</li>
	 *   <li>Файл проходит валидацию через FileService</li>
	 * </ul>
	 * 
	 * <p>Ответ:
	 * <ul>
	 *   <li>201 Created - если аватар создан впервые</li>
	 *   <li>200 OK - если аватар обновлен</li>
	 * </ul>
	 * 
	 * @param ctx HTTP контекст с multipart данными
	 * 
	 * @throws UnauthorizedException если сессия невалидна
	 * @throws BadRequestException если файл отсутствует или невалиден
	 * @throws FileValidationException если файл не прошел валидацию
	 * @throws PayloadTooLargeException если файл слишком большой
	 * 
	 * @apiNote Возвращает URL для доступа к загруженному аватару
	 * @see FileService#uploadOne(String, UploadedFile)
	 */
	public void uploadMeAvatar(HttpContext ctx) {
		// Получаем сессию для идентификации пользователя
		SessionEntity s = ctx.getAuthSession();

		// Загружаем данные пользователя для проверки существования
		UserEntity user = this.userService.getById(s.userId.toHexString()).getData();

		// Извлекаем загруженные файлы из multipart запроса
		// Ожидается только один файл (аватар)
		List<UploadedFile> files = ctx.getFiles();

		// Логируем попытку загрузки аватара для аудита
		this.info("user avatar upload requested", ctx.getRequestId(),
				detailsOf("userId", user.id.toHexString()));

		// Делегируем обработку загрузки UserService
		// Сервис выполнит валидацию файла, сохранение и обновление профиля
		var resp = this.userService.uploadAvatar(ctx.getRequestId(), user.id.toHexString(), files.get(0));
		String url = resp.getData();

		// Определяем тип ответа на основе того, был ли аватар создан впервые или обновлен
		// ServiceProcessType.CREATED означает, что аватар создан впервые
		if (resp.getProcess() == ServiceProcessType.CREATED) {
			// Возвращаем HTTP 201 Created для нового аватара
			ctx.res().success(ApiSuccessResponse.created(
					ctx.getRequestId(),
					"avatar created",
					detailsOf("url", url)));
		} else {
			// Возвращаем HTTP 200 OK для обновления существующего аватара
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
