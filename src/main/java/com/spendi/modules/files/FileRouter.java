
/**
 * @file FileRouter.java
 * @module core/router
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.files;

/**
 * ! my imports
 */
// config
import com.spendi.config.FileValidationConfig;
// core -> base
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.base.server.javalin.middleware.JavalinMultipartParserMiddleware;
import com.spendi.core.middleware.AuthMiddleware;
// core -> middleware
import com.spendi.core.middleware.FileValidationMiddleware;
import com.spendi.core.middleware.ParamsValidationMiddleware;
import com.spendi.core.middleware.QueryValidationMiddleware;
import com.spendi.core.middleware.TempFilesCleanupMiddleware;
// core -> router
import com.spendi.core.router.ApiRouter;
// core -> files
import com.spendi.core.files.validation.FileValidationRules;
// files -> dto
import com.spendi.modules.files.dto.FileDownloadParams;
import com.spendi.modules.files.dto.FileDownloadQuery;

public class FileRouter extends ApiRouter {
	public FileController controller = FileController.getInstance();

	public FileRouter(String apiPrefix) {
		super(FileRouter.class.getSimpleName(), "/files", apiPrefix);
	}

	@Override
	public void configure(HttpServerAdapter http) {

		// Подключаем AuthMiddleware на весь роутер
		this.use(AuthMiddleware.getInstance());

		// Базовые правила на один файл для тестов
        FileValidationRules rules = FileValidationRules
                .builderFromConfig(FileValidationConfig.getConfig())
                .minFiles(1)
                .maxFiles(1)
                .allowedExtensions(".jpg", ".gif")
                .build();

		this.post("/",
				controller::createOne,
				new JavalinMultipartParserMiddleware(),
				new FileValidationMiddleware(rules),
				TempFilesCleanupMiddleware.getInstance());

		// Просмотр/скачивание файла по id
		this.get(
				"/{id}",
				controller::downloadOne,
				ParamsValidationMiddleware.of(FileDownloadParams.class),
				QueryValidationMiddleware.of(FileDownloadQuery.class));

		// Удаление файла по id
		this.delete(
				"/{id}",
				controller::deleteOne,
				ParamsValidationMiddleware.of(FileDownloadParams.class),
				QueryValidationMiddleware.of(FileDownloadQuery.class));
	}
}
