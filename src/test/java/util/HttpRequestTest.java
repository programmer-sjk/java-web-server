package util;

import org.junit.Test;
import webserver.HttpRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HttpRequestTest {
    private String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
        HttpRequest request = new HttpRequest(in);
        int idx = "/user/create?userId=seo&password=password&name=seojeongkuk".indexOf("?");
        assertEquals("GET", request.getMethod());
        assertEquals(12, idx);
        assertEquals("userId=seo&password=password&name=seojeongkuk", "/user/create?userId=seo&password=password&name=seojeongkuk".substring(idx+1));
        System.out.println(1);

//        assertEquals("/user/create", request.getPath());
//        assertEquals("keep-alive", request.getHeader("Connection"));
//        assertEquals("seo", request.getParameter("userId"));
    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        HttpRequest request = new HttpRequest(in);
        assertEquals("POST", request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("seo", request.getParameter("userId"));
    }
}
