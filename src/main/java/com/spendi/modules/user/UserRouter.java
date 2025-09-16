
/**
 * @file UserRouter.java
 * @module modules/user
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user;

/**
 * ! my imports
 */
import com.spendi.config.FileValidationConfig;
// core -> base
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.base.server.javalin.middleware.JavalinMultipartParserMiddleware;
import com.spendi.core.dto.PaginationQueryDto;
// core -> middleware
import com.spendi.core.middleware.AuthMiddleware;
import com.spendi.core.middleware.FileValidationMiddleware;
import com.spendi.core.middleware.ParamsValidationMiddleware;
import com.spendi.core.middleware.QueryValidationMiddleware;
import com.spendi.core.middleware.TempFilesCleanupMiddleware;
import com.spendi.core.middleware.BodyValidationMiddleware;
import com.spendi.core.middleware.JsonBodyParserMiddleware;
// core ->files
import com.spendi.core.files.validation.FileValidationRules;
// core -> router
import com.spendi.core.router.ApiRouter;
// user -> dto
import com.spendi.modules.user.dto.UserIdParams;
import com.spendi.modules.user.dto.UserPaymentMethodIdParams;
// payment -> dto
import com.spendi.modules.payment.dto.PaymentMethodCreateDto;
import com.spendi.modules.payment.dto.PaymentMethodOrderDto;

public class UserRouter extends ApiRouter {
	public UserController controller = UserController.getInstance();

	public UserRouter(String apiPrefix) {
		super(UserRouter.class.getSimpleName(), "/users", apiPrefix);
	}

	@Override
	public void configure(HttpServerAdapter http) {
		// Rules for avatar images: 1 file, common image extensions
		FileValidationRules avatarRules = FileValidationRules.builderFromConfig(FileValidationConfig.getConfig())
				.minFiles(1).maxFiles(1).allowedExtensions(".jpg", ".jpeg", ".png", ".gif").build();

		// Подключаем AuthMiddleware на весь роутер
		this.use(AuthMiddleware.getInstance());

		/**
		 * ? === === === Read === === ===
		 */

		// Получить свои данные
		this.get("/me", controller::getMe);

		// Получить данные о пользователе по id
		this.get("/{id}", controller::getOneById, ParamsValidationMiddleware.of(UserIdParams.class));

		/**
		 * * === === === Avatar === === ===
		 */

		/**
		 * ? === === === Create === === ===
		 */

		// Загрузить аватар для авторизованого пользователе
		this.post("/me/avatar", controller::uploadMeAvatar, new JavalinMultipartParserMiddleware(),
				new FileValidationMiddleware(avatarRules), TempFilesCleanupMiddleware.getInstance());

		/**
		 * ? === === === Read === === ===
		 */

		// Посмотреть свой аватар
		this.get("/me/avatar", controller::getMeAvatar);

		// Посмотреть аватар
		this.get("/{id}/avatar", controller::getAvatar, ParamsValidationMiddleware.of(UserIdParams.class));

		/**
		 * ? === === === Delete === === ===
		 */

		// Удалить аватар авторизованого пользователе
		this.delete("/me/avatar", controller::deleteMeAvatar);

		/**
		 * * === === === Payment methods === === ===
		 */

		/**
		 * ? === === === Create === === ===
		 */

		// Добавить метод оплаты авторизованого пользователя
		this.post("/me/payment-methods", controller::addPaymentMethod, new JsonBodyParserMiddleware(),
				BodyValidationMiddleware.of(PaymentMethodCreateDto.class));

		/**
		 * ? === === === Read === === ===
		 */

		// Получить все методы оплаты авторизованого пользователя
		this.get("/me/payment-methods", controller::getMePaymentMethods,
				QueryValidationMiddleware.of(PaginationQueryDto.class));

		/**
		 * ? === === === Update === === ===
		 */

		// Обновить порядок методов оплаты авторизованого пользователя
		this.put("/me/payment-methods/{pmId}/order", controller::updatePaymentMethodOrder,
				ParamsValidationMiddleware.of(UserPaymentMethodIdParams.class), new JsonBodyParserMiddleware(),
				BodyValidationMiddleware.of(PaymentMethodOrderDto.class));

		/**
		 * ? === === === Delete === === ===
		 */

		// Удалить метод оплаты авторизованого пользователя
		this.delete("/me/payment-methods/{pmId}", controller::deletePaymentMethod,
				ParamsValidationMiddleware.of(UserPaymentMethodIdParams.class));

	}
}
