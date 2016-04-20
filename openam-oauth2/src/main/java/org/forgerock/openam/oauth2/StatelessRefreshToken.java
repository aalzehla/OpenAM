/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

package org.forgerock.openam.oauth2;

import static org.forgerock.oauth2.core.OAuth2Constants.CoreTokenParams.AUTH_MODULES;
import static org.forgerock.oauth2.core.OAuth2Constants.CoreTokenParams.EXPIRE_TIME;
import static org.forgerock.oauth2.core.OAuth2Constants.CoreTokenParams.TOKEN_TYPE;
import static org.forgerock.oauth2.core.OAuth2Constants.JWTTokenParams.ACR;
import static org.forgerock.oauth2.core.OAuth2Constants.Params.REFRESH_TOKEN;
import static org.forgerock.oauth2.core.OAuth2Constants.Params.REDIRECT_URI;
import static org.forgerock.oauth2.core.OAuth2Constants.Bearer.BEARER;
import static org.forgerock.openam.utils.Time.currentTimeMillis;

import java.util.HashMap;
import java.util.Map;

import org.forgerock.json.jose.jwt.Jwt;
import org.forgerock.oauth2.core.RefreshToken;

/**
 * Models a stateless OpenAM OAuth2 refresh token.
 */
public class StatelessRefreshToken extends StatelessToken implements RefreshToken {

    private final String jwtString;

    /**
     * Constructs a new StatelessRefreshToken backed with the specified {@code Jwt}.
     *
     * @param jwt The stateless token.
     * @param jwtString The JWT string.
     */
    public StatelessRefreshToken(Jwt jwt, String jwtString) {
        super(jwt);
        this.jwtString = jwtString;
    }

    @Override
    public String getTokenId() {
        return jwtString;
    }

    @Override
    public String getTokenName() {
        return REFRESH_TOKEN;
    }

    @Override
    public String getAuthenticationContextClassReference() {
        return jwt.getClaimsSet().getClaim(ACR, String.class);
    }

    @Override
    public String getRedirectUri() {
        return jwt.getClaimsSet().getClaim(REDIRECT_URI, String.class);
    }

    @Override
    public String getAuthModules() {
        return jwt.getClaimsSet().getClaim(AUTH_MODULES, String.class);
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(getResourceString(REFRESH_TOKEN), jwt.build());
        tokenMap.put(getResourceString(TOKEN_TYPE), BEARER);
        tokenMap.put(getResourceString(EXPIRE_TIME), getExpireTime());
        return tokenMap;
    }

    private long getExpireTime() {
        return (getExpiryTime() == -1) ? null : (jwt.getClaimsSet().getExpirationTime().getTime() - currentTimeMillis()) / 1000;
    }

    @Override
    public String getAuditTrackingId() {
        return null;
    }

    @Override
    public String toString() {
        return jwtString;
    }

    /**
     * Get whether or not token expires.
     *
     * @return Whether or not token expires.
     */
    @Override
    public boolean isNeverExpires() {
        return getExpiryTime() == -1;
    }

    /**
     * Determines if the Refresh Token has expired.
     *
     * @return {@code true} if current time is greater than the expiry time.
     */
    @Override
    public boolean isExpired() {
        if (isNeverExpires()) {
            return false;
        }
        return currentTimeMillis() > getExpiryTime();
    }

    /**
     * Gets the display String for the given String.
     *
     * @param string The String.
     * @return The display String.
     */
    protected String getResourceString(final String string) {
        return string;
    }
}