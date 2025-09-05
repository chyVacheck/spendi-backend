
/**
 * @file FilesRouter.java
 * @module core/router
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.router;

/**
 * ! java imports
 */
import java.util.List;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.http.RequestAttr;
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.base.server.javalin.middleware.JavalinMultipartParserMiddleware;
import com.spendi.core.files.UploadedFile;
import com.spendi.core.response.ApiSuccessResponse;

public class FilesRouter extends ApiRouter {

	public FilesRouter(String apiPrefix) {
		super(FilesRouter.class.getSimpleName(), "/files", apiPrefix);
	}

	@Override
	public void configure(HttpServerAdapter http) {
		this.post("/", ctx -> {
			@SuppressWarnings("unchecked")
			List<UploadedFile> files = (List<UploadedFile>) ctx.getAttr(RequestAttr.FILES, List.class);

			ctx.res().success(ApiSuccessResponse.created(
					ctx.getRequestId(),
					"uploaded",
					Map.of(
							"count", files == null ? 0 : files.size(),
							"files", files // Jackson сам сериализует с геттерами
			)));
		}, new JavalinMultipartParserMiddleware());
	}
}