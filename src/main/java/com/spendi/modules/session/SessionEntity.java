/**
 * @file SessionEntity.java
 * @module modules/session
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.session;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;

/**
 * ! my imports
 */
import java.time.Instant;

public class SessionEntity {
	public ObjectId id;
	public ObjectId userId;
	public Instant createdAt;
	public Instant lastSeenAt;
	public Instant expiresAt;
	public boolean revoked;
	public String ip;
	public String userAgent;
}
