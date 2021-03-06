package umm3601.admin;

import org.junit.Before;
import org.junit.Test;
import umm3601.digitalDisplayGarden.Authentication.Auth;
import umm3601.digitalDisplayGarden.Authentication.RedirectToken;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;
public class TestAuthorizedCookieBody {

    private Auth auth;

    @Before
    public void setup() throws NoSuchAlgorithmException, FileNotFoundException{
        auth = new Auth("fakeID", "fakeSecret", "callback");
    }

    @Test
    public void testThatCookieBodyIsAuthorizable(){
        String cookieBody = auth.generateCookieBody(120);
        assertNotNull(cookieBody);
        assertNotEquals("",cookieBody);

        boolean authorized = auth.authorized(cookieBody);
        assertTrue(authorized);
    }

    @Test
    public void testThatCookieBodyIsExpired(){
        String cookieBody = auth.generateCookieBody(0);
        boolean authorized = auth.authorized(cookieBody);
        assertFalse(authorized);
    }

    @Test
    public void testThatInvalidCookieBodyIsNotAuthorized(){
        boolean authorized = auth.authorized("SWvvnS4x1qPOQRUHbuX6s//TrXTk/ir74LDBp");
        assertFalse(authorized);
    }

    @Test
    public void testGenerateAndUnpackSecretSuccess(){
        String secret = auth.generateSharedGoogleSecret("someURL");
        RedirectToken redToken = auth.unpackSharedGoogleSecret(secret);
        assertEquals("someURL", redToken.originatingURL);
    }

    @Test
    public void testGenerateAndUnpackSecretFailure(){
        String secret = auth.generateSharedGoogleSecret("someURL");
        secret = secret + "fasd";
        RedirectToken redToken = auth.unpackSharedGoogleSecret(secret);
        assertNull(redToken);

    }
}

