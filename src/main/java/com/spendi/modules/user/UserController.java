
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
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseController;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.response.ApiSuccessResponse;
import com.spendi.core.response.ServiceResponse;
import com.spendi.core.types.ServiceProcessType;
import com.spendi.modules.files.FileService;
import com.spendi.modules.session.SessionEntity;
import com.spendi.modules.user.model.UserEntity;
import com.spendi.shared.dto.IdParams;
import com.spendi.shared.dto.PaginationQueryDto;
import com.spendi.core.files.UploadedFile;
import com.spendi.core.http.HttpStatusCode;
import com.spendi.core.files.DownloadedFile;
import com.spendi.modules.payment.PaymentMethodMapper;
import com.spendi.modules.payment.PaymentMethodService;
import com.spendi.modules.payment.cmd.PaymentMethodCreateCmd;
import com.spendi.modules.payment.dto.PaymentMethodIdParams;
import com.spendi.modules.payment.dto.PaymentMethodOrderDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateDto;
import com.spendi.modules.payment.model.PaymentMethodEntity;

/**
 * Контроллер для управления пользователями и связанными с ними данными.
 * 
 * <p>
 * Предоставляет REST API endpoints для:
 * <ul>
 * <li>Получения информации о пользователях (публичной и приватной)</li>
 * <li>Управления аватарами (загрузка, получение, удаление)</li>
 * <li>Управления методами оплаты пользователей</li>
 * </ul>
 * 
 * <p>
 * Все операции с пользовательскими данными требуют валидной сессии. Контроллер использует паттерн Singleton для
 * управления экземпляром.
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
	/** Mapper для преобразования DTO в команды создания методов оплаты */
	private final PaymentMethodMapper paymentMapper = PaymentMethodMapper.getInstance();

	/**
	 * Приватный конструктор для реализации паттерна Singleton. Инициализирует контроллер с именем класса для
	 * логирования.
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
	 * <p>
	 * Endpoint: {@code GET /users/me}
	 * 
	 * <p>
	 * Возвращает приватные данные пользователя (включая email и другую конфиденциальную информацию), доступные только
	 * самому пользователю.
	 * 
	 * <p>
	 * Требования:
	 * <ul>
	 * <li>Валидная сессия аутентификации</li>
	 * <li>Активный пользователь в системе</li>
	 * </ul>
	 * 
	 * @param ctx HTTP контекст запроса с данными сессии
	 * 
	 * @throws UnauthorizedException   если сессия невалидна
	 * @throws EntityNotFoundException если пользователь не найден
	 * 
	 * @apiNote Возвращает приватные данные пользователя
	 * @see UserEntity#getPrivateData()
	 */
	public void getMe(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();

		UserEntity user = this.userService.getById(s.getUserHexId()).getData();

		// Лог запроса сущности пользователя (несохраненный)
		this.info("User get me", ctx.getRequestId(), detailsOf("userId", user.getHexId()));

		ctx.res().success(ApiSuccessResponse.ok(ctx.getRequestId(), "User " + user.getEmail(), user.getPrivateData()));
	}

	/**
	 * Получить публичные данные пользователя по его ID.
	 * 
	 * <p>
	 * Endpoint: {@code GET /users/{id}}
	 * 
	 * <p>
	 * Возвращает только публичные данные пользователя (без конфиденциальной информации типа email). Доступно для всех
	 * аутентифицированных пользователей.
	 * 
	 * <p>
	 * Параметры URL:
	 * <ul>
	 * <li>{@code id} - ObjectId пользователя в формате строки</li>
	 * </ul>
	 * 
	 * @param ctx HTTP контекст с параметрами запроса
	 * 
	 * @throws BadRequestException     если ID имеет неверный формат
	 * @throws EntityNotFoundException если пользователь не найден
	 * 
	 * @apiNote Возвращает только публичные данные пользователя
	 * @see UserEntity#getPublicData()
	 * @see IdParams
	 */
	public void getOneById(HttpContext ctx) {
		IdParams p = ctx.getValidParams(IdParams.class);

		// Лог запроса сущности пользователя (несохраненный)
		this.info("User get by id", ctx.getRequestId(), detailsOf("userId", p.getId()));

		var resp = this.userService.getById(p.getId());
		UserEntity user = resp.getData();

		ctx.res().success(ApiSuccessResponse.ok(ctx.getRequestId(), "User " + user.getEmail(), user.getPublicData()));
	}

	/**
	 * Получить аватар текущего авторизованного пользователя.
	 * 
	 * <p>
	 * Endpoint: {@code GET /users/me/avatar}
	 * 
	 * <p>
	 * Возвращает бинарное содержимое файла аватара с соответствующими HTTP заголовками для отображения в браузере
	 * (Content-Type, Content-Disposition).
	 * 
	 * <p>
	 * Поведение:
	 * <ul>
	 * <li>Если аватар существует - возвращает файл с кодом 200</li>
	 * <li>Если аватар отсутствует - возвращает код 204 (No Content)</li>
	 * </ul>
	 * 
	 * <p>
	 * HTTP заголовки ответа:
	 * <ul>
	 * <li>{@code Content-Type} - MIME тип файла</li>
	 * <li>{@code Content-Disposition: inline} - для отображения в браузере</li>
	 * </ul>
	 * 
	 * @param ctx HTTP контекст запроса
	 * 
	 * @throws UnauthorizedException   если сессия невалидна
	 * @throws EntityNotFoundException если пользователь не найден
	 * 
	 * @apiNote Возвращает бинарные данные файла или 204 если аватар отсутствует
	 */
	public void getMeAvatar(HttpContext ctx) {
		// Извлекаем информацию о текущей сессии для получения ID пользователя
		SessionEntity s = ctx.getAuthSession();

		// Получаем полные данные пользователя из базы данных по ID из сессии
		UserEntity u = this.userService.getById(s.getUserHexId()).getData();

		// Логируем запрос для отслеживания активности пользователей
		this.info("user avatar get requested", ctx.getRequestId(), detailsOf("userId", u.getId().toHexString()));

		// Проверяем, есть ли у пользователя аватар
		// Если аватар отсутствует, возвращаем HTTP 204 (No Content)
		if (!u.hasAvatar()) {
			this.debug("No avatar found for user");
			ctx.res().status(HttpStatusCode.NO_CONTENT.getCode());
			return;
		}

		// Загружаем файл аватара из файлового хранилища по ID файла
		var fileResp = this.fileService.downloadOne(ctx.getRequestId(), u.getProfile().getAvatarFileId());
		DownloadedFile file = fileResp.getData();

		// Формируем заголовок Content-Disposition для inline отображения
		// Используем оригинальное имя файла или ID пользователя как fallback
		String disposition = "inline; filename=\""
				+ (file.getFilename() == null ? u.getProfile().getAvatarFileId().toHexString() : file.getFilename())
				+ "\"";

		// Отправляем файл клиенту с правильными заголовками
		// Content-Type определяет MIME тип для корректного отображения
		// Content-Disposition: inline позволяет отображать изображение в браузере
		ctx.res().header("Content-Type", file.getContentType()).header("Content-Disposition", disposition)
				.sendBytes(file.getContent());
	}

	/**
	 * GET /users/{id}/avatar: stream the avatar content inline
	 */
	public void getAvatar(HttpContext ctx) {
		IdParams p = ctx.getValidParams(IdParams.class);

		// Лог запроса получения аватара (несохраненный)
		this.info("user avatar get requested", ctx.getRequestId(), detailsOf("userId", p.getId()));

		// read user to get file id
		var u = this.userService.getById(p.getId()).getData();
		if (!u.hasAvatar()) {
			this.debug("No avatar");
			ctx.res().status(HttpStatusCode.NO_CONTENT.getCode());
			return;
		}
		var fileResp = this.fileService.downloadOne(ctx.getRequestId(), u.getProfile().getAvatarFileId());
		DownloadedFile file = fileResp.getData();

		String disposition = "inline; filename=\""
				+ (file.getFilename() == null ? (p.getId() + "") : file.getFilename()) + "\"";
		ctx.res().header("Content-Type", file.getContentType()).header("Content-Disposition", disposition)
				.sendBytes(file.getContent());
	}

	/**
	 * GET /users/me/payment-methods
	 */
	public void getMePaymentMethods(HttpContext ctx) {
		// Получаем сессию для идентификации пользователя
		SessionEntity s = ctx.getAuthSession();

		// Получаем и валидируем параметры пагинации из запроса
		PaginationQueryDto query = ctx.getValidQuery(PaginationQueryDto.class);

		// Загружаем данные пользователя для проверки существования
		ServiceResponse<List<Map<String, Object>>> paymentMethods = this.userService
				.getPaymentMethods(ctx.getRequestId(), s.getUserHexId(), query);

		ctx.res().success(ApiSuccessResponse.ok(ctx.getRequestId(), "payment methods loaded", paymentMethods.getData(),
				paymentMethods.getPaginationOrThrow().toMap()));
	}

	/**
	 * * Create
	 */

	/**
	 * Загрузить или обновить аватар текущего пользователя.
	 * 
	 * <p>
	 * Endpoint: {@code POST /users/me/avatar}
	 * 
	 * <p>
	 * Принимает multipart/form-data с файлом изображения и сохраняет его как аватар пользователя. Если аватар уже
	 * существует - заменяет его.
	 * 
	 * <p>
	 * Требования к файлу:
	 * <ul>
	 * <li>Должен быть изображением (JPEG, PNG, GIF и т.д.)</li>
	 * <li>Размер файла ограничен конфигурацией системы</li>
	 * <li>Файл проходит валидацию через FileService</li>
	 * </ul>
	 * 
	 * <p>
	 * Ответ:
	 * <ul>
	 * <li>201 Created - если аватар создан впервые</li>
	 * <li>200 OK - если аватар обновлен</li>
	 * </ul>
	 * 
	 * @param ctx HTTP контекст с multipart данными
	 * 
	 * @throws UnauthorizedException    если сессия невалидна
	 * @throws BadRequestException      если файл отсутствует или невалиден
	 * @throws FileValidationException  если файл не прошел валидацию
	 * @throws PayloadTooLargeException если файл слишком большой
	 * 
	 * @apiNote Возвращает URL для доступа к загруженному аватару
	 * @see FileService#uploadOne(String, UploadedFile)
	 */
	public void uploadMeAvatar(HttpContext ctx) {
		// Получаем сессию для идентификации пользователя
		SessionEntity s = ctx.getAuthSession();

		// Загружаем данные пользователя для проверки существования
		UserEntity user = this.userService.getById(s.getUserHexId()).getData();

		// Извлекаем загруженные файлы из multipart запроса
		// Ожидается только один файл (аватар)
		List<UploadedFile> files = ctx.getFiles();

		// Логируем попытку загрузки аватара для аудита
		this.info("user avatar upload requested", ctx.getRequestId(), detailsOf("userId", user.getId().toHexString()));

		// Делегируем обработку загрузки UserService
		// Сервис выполнит валидацию файла, сохранение и обновление профиля
		var resp = this.userService.uploadAvatar(ctx.getRequestId(), user.getId().toHexString(), files.get(0));
		String url = resp.getData();

		// Определяем тип ответа на основе того, был ли аватар создан впервые или
		// обновлен
		// ServiceProcessType.CREATED означает, что аватар создан впервые
		if (resp.getProcess() == ServiceProcessType.CREATED) {
			// Возвращаем HTTP 201 Created для нового аватара
			ctx.res().success(ApiSuccessResponse.created(ctx.getRequestId(), "avatar created", detailsOf("url", url)));
		} else {
			// Возвращаем HTTP 200 OK для обновления существующего аватара
			ctx.res().success(ApiSuccessResponse.ok(ctx.getRequestId(), "avatar updated", detailsOf("url", url)));
		}
	}

	/**
	 * POST /users/me/payment-methods
	 */
	public void addPaymentMethod(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();
		PaymentMethodCreateDto dto = ctx.getValidBody(PaymentMethodCreateDto.class);

		PaymentMethodCreateCmd cmd = paymentMapper.toCmd(dto);

		PaymentMethodEntity created = this.paymentService.createOne(ctx.getRequestId(), s.getUserHexId(), cmd)
				.getData();

		UserEntity updated = this.userService.addPaymentMethod(ctx.getRequestId(), s.getUserHexId(), created).getData();

		ctx.res().success(
				ApiSuccessResponse.created(ctx.getRequestId(), "payment method created", updated.getPrivateData()));
	}

	/**
	 * PUT /users/me/payment-methods/{id}/order
	 */
	public void updatePaymentMethodOrder(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();
		var p = ctx.getValidParams(PaymentMethodIdParams.class);
		PaymentMethodOrderDto dto = ctx.getValidBody(PaymentMethodOrderDto.class);

		PaymentMethodEntity updated = this.userService
				.updatePaymentMethodOrder(ctx.getRequestId(), s.getUserHexId(), p.getPmId(), dto.order).getData();

		ctx.res().success(
				ApiSuccessResponse.ok(ctx.getRequestId(), "payment method order updated", updated.getPublicData()));
	}

	/**
	 * * Delete
	 */

	/**
	 * DELETE /users/me/payment-methods/{id}
	 */
	public void deletePaymentMethod(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();
		var p = ctx.getValidParams(PaymentMethodIdParams.class);

		this.userService.deletePaymentMethod(ctx.getRequestId(), s.getUserHexId(), p.getPmId());

		ctx.res().success(
				ApiSuccessResponse.ok(ctx.getRequestId(), "payment method deleted", detailsOf("id", p.getPmId())));
	}

	/**
	 * DELETE /users/me/avatar: полностью удаляет аватар авторизованого пользователя
	 */
	public void deleteMeAvatar(HttpContext ctx) {
		SessionEntity s = ctx.getAuthSession();

		// Лог запроса удаления аватара (несохраненный)
		this.info("user avatar delete requested", ctx.getRequestId(), detailsOf("userId", s.getUserHexId()));

		var resp = this.userService.deleteAvatar(ctx.getRequestId(), s.getUserHexId());
		String url = resp.getData();

		ctx.res().success(ApiSuccessResponse.ok(ctx.getRequestId(), "avatar deleted", detailsOf("url", url)));
	}

}
