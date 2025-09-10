/**
 * @file TempFilesCleanupMiddleware.java
 * @module core/middleware
 *
 * Удаляет временные файлы после обработки запроса (в блоке finally).
 * Сделана без лишних вложенностей и реализована как Singleton,
 * так как не хранит состояние и потокобезопасна.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.middleware;

/**
 * ! java imports
 */
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMiddleware;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.MiddlewareChain;

public class TempFilesCleanupMiddleware extends BaseMiddleware {

	// -------- Singleton --------
	private static final TempFilesCleanupMiddleware INSTANCE = new TempFilesCleanupMiddleware();

	public static TempFilesCleanupMiddleware getInstance() {
		return INSTANCE;
	}

	private TempFilesCleanupMiddleware() {
		super(TempFilesCleanupMiddleware.class.getSimpleName());
	}

	// -------- Middleware --------
	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		try {
			chain.next();
		} finally {
			cleanupTemps(ctx);
		}
	}

	// -------- Internals --------
	private void cleanupTemps(HttpContext ctx) {
		List<Path> temps = (List<Path>) ctx.getTempFiles();
		if (temps == null || temps.isEmpty())
			return;

		String reqId = ctx.getRequestId();
		for (Path p : temps) {
			deleteQuietly(p, reqId);
		}
	}

	private void deleteQuietly(Path p, String requestId) {
		if (p == null)
			return;
		try {
			Files.deleteIfExists(p);
			this.info(
					"Delete temp file",
					requestId,
					detailsOf("path", p.toString()),
					true);
		} catch (Exception e) {
			this.warn(
					"Failed to delete temp file",
					requestId,
					detailsOf("path", p.toString(), "error", String.valueOf(e.getMessage())),
					true);
		}
	}
}
