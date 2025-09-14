/**
 * @file DocsRouter.java
 * @module core/router
 *
 * Простая статика для документации API:
 * - GET {prefix}/docs -> ReDoc UI, читает {prefix}/docs/openapi.json
 * - GET {prefix}/docs/openapi.json -> отдаёт файл из resources/docs/openapi.json
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.router;

/**
 * ! java imports
 */
import java.io.InputStream;

/**
 * ! my imports
 */
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.base.http.HttpContext;

public class DocsRouter extends ApiRouter {

	public DocsRouter(String apiPrefix) {
		super(DocsRouter.class.getSimpleName(), "/docs", apiPrefix);
	}

	@Override
	public void configure(HttpServerAdapter http) {
		this.get("/", this::serveReDoc);
		this.get("/openapi.json", this::serveOpenApiJson);
	}

	private void serveReDoc(HttpContext ctx) {
		String specUrl = this.basePath() + "/openapi.json";
		String html = "<!DOCTYPE html>" +
				"<html lang=\"en\"><head>" +
				"<meta charset=\"utf-8\"/>" +
				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>" +
				"<title>Spendi API ReDoc</title>" +
				"<style>html,body{margin:0;height:100%} redoc{height:100%}</style>" +
				"</head><body>" +
				"<redoc spec-url='" + specUrl + "'></redoc>" +
				"<script src=\"https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js\"></script>" +
				"</body></html>";
		ctx.res().header("Content-Type", "text/html; charset=utf-8").sendText(html);
	}

	private void serveOpenApiJson(HttpContext ctx) {
		try (InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("docs/openapi.json")) {
			if (is == null) {
				ctx.res().status(404).sendText("openapi.json not found");
				return;
			}
			byte[] bytes = is.readAllBytes();
			ctx.res().header("Content-Type", "application/json; charset=utf-8").sendBytes(bytes);
		} catch (Exception e) {
			ctx.res().status(500).sendText("Failed to read openapi.json");
		}
	}
}
