package com.aw.auth;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.aw.common.rest.Localizer;
import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.DefaultSecurityContext;
import com.aw.common.rest.security.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AccessTokenUtil {

	private static final Logger logger = LoggerFactory.getLogger(AccessTokenUtil.class);

    private static AccessTokenUtil util = null;

    //An authentication token storage which stores < auth_token, user name >.
    private final Map<String, String> authTokensUserNameStorage = new HashMap();

    public static final int SALT_BYTE_SIZE = 24;

	//create the secret key that will be used to sign and verify JWT's
    private static final String SECRET;
    static {
        //can't be dynamic anymore because need to have the same key on DGMC pass through API
        SECRET = getenvOrDefault("BEARER_SECRET_KEY", "61f1b78448adfb472861d2730d0e3cb41eb5859b61049635");
    }
    private static JWTSigner signer = new JWTSigner(SECRET);
    private static JWTVerifier verifier = new JWTVerifier(SECRET);
    private static Integer JWTExpireAfterSeconds = 15 * 60; //set the JWT to expire after 15 minutes

    //the secret key will be different if tomcat restarts so any valid JWT's would be invalidated
    private static String buildSecretKey() {

    	String rv = "";

        // Generate a random salt

        byte[] salt = createSalt();
    	rv = toHex(salt);

    	//logger.info("setting secret: " + rv);
		return rv;
    };


    private static String getenvOrDefault(String env, String def) {
    	String ret = System.getenv(env);
    	if (ret == null) {
    		ret = def;
    	}
    	return ret;
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param   array       the byte array to convert
     * @return              a length*2 character string encoding the byte array
     */
    private static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    private static byte[] createSalt()
    {
        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);
        return salt;

    }

    private AccessTokenUtil() {


    }

    public static AccessTokenUtil getInstance() {
        if ( util == null ) {
        	util = new AccessTokenUtil();
        }
        return util;
    }

    public String login( String userID, String userName, String tenantKey) throws LoginException {

                //Validate the password by hashing it and comparing the hashed value to the hash stored in the users record
                try {

                    //TODO: lookup roles for this validated user from the DB when creating the JWT


					/**
					 * The authToken will be needed for every REST API invocation and is only valid within
                     * the login session
                     */
                    HashMap<String, Object> claims = new HashMap<String, Object>();

                    AuthenticatedUser user = new AuthenticatedUser();

                    user.setId(userID);
                    user.setName(userName);
                    user.setTenant(tenantKey);
                    user.setLocale("en-us");
                    //TODO: will these roles come from the pass through login, or another service call? the former is preferred.
                    //user.setRoles(roles);
                    claims.put("user", user);

                    String authToken = signer.sign(claims,
                    		new JWTSigner.Options()
//								.setExpirySeconds(JWTExpireAfterSeconds)	// auth tokens don't expire, for now
								.setNotValidBeforeLeeway(1)
								.setIssuedAt(true));


                    authTokensUserNameStorage.put( authToken, userName );

                    return authToken;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new LoginException( "Don't Come Here Again!" );
				}


    }

    /**
     * The method that pre-validates if the client which invokes the REST API is
     * from a authorized and authenticated source.
     *
     * @param authToken The authorization token generated after login

     */
    public boolean isAuthTokenValid( String authToken ) throws Exception {

    	boolean rv = false;

		//Map<String, Object> decoded = null;
		//Note:  if not valid it will throw an error, we don't need to pull any claims out right now, that is done in: GetSecurityContextFromClaims

    	verifier.verify(authToken);
		rv = true;

    	return rv;
    }


    public PlatformSecurityContext getSecurityContextFromClaims(String authToken )  {

    	PlatformSecurityContext rv = null;
		AuthenticatedUser user = null;

		try {

			user = getWebUserFromToken(authToken);
			rv = new DefaultSecurityContext(user);

		} catch (InvalidKeyException
				| NoSuchAlgorithmException
				| IllegalStateException
				| SignatureException
				| IOException
				| JWTVerifyException e) {

			//If there are any exceptions validating the auth token,
			//swallow the exception and return null.  No user no access.

		}

		return rv;
    }


	public AuthenticatedUser getWebUserFromToken(String authToken) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, SignatureException, IOException, JWTVerifyException {

		AuthenticatedUser rv = null;

		Map<String, Object> decoded = null;
		//Note:  if not valid it will throw an error

		decoded = verifier.verify(authToken);

		LinkedHashMap<String, Object> user = (LinkedHashMap<String, Object>) decoded.get("user");

        //TODO: determine exact contents of token
		rv = new AuthenticatedUser(user.get("name").toString(),user.get("name").toString(), user.get("tenant").toString(),Localizer.LOCALE_EN_US , null , null, null);

		return rv;

		/*		if (decoded.get("user") instanceof AuthenticatedUser) {
		AuthenticatedUser user = (AuthenticatedUser)decoded.get("user");
		rv = user;
	}*/


	}
}