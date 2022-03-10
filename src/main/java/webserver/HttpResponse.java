package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> parameters;

    public HttpResponse(OutputStream out) throws IOException {

    }

    public void forward(String url) {

    }

    public void sendRedirect(String url) {

    }

    public void addHeader(String key, String value) {

    }


}
