package util;

import org.junit.Test;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class HttpResponseTest {
    private String testDirectory = "./src/test/resources/";

    @Test
    public void responseForward() throws Exception {
        HttpResponse res = new HttpResponse(createOutPutStream("Http_forward.txt"));
        res.forward("/index.html");
    }

    @Test
    public void responseRedirect() throws Exception {
        HttpResponse res = new HttpResponse(createOutPutStream("Http_Redirect.txt"));
        res.sendRedirect("/index.html");
    }

    @Test
    public void responseCookies() throws Exception {
        HttpResponse res = new HttpResponse(createOutPutStream("Http_Cookie.txt"));
        res.addHeader("Set-Cookie", "logined=true");
        res.sendRedirect("/index.html");
    }

    private OutputStream createOutPutStream(String filename) throws FileNotFoundException {
        return new FileOutputStream(new File(testDirectory + filename));
    }
}
