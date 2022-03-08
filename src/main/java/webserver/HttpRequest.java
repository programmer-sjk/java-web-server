package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> parameters;

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String[] requests = br.readLine().split(" ");
        this.method = requests[0];
        this.path = this.parsePath(requests[1]);
        this.headers = parseHeader(br);
        this.parameters = parseParameter(br, requests[1]);
    }

    private String parsePath(String url) {
        if (this.isGetMethod()) {
            int idx = url.indexOf("?");
            System.out.println(url.substring(0, idx -1));
            return url.substring(0, idx -1);
        }
        return url;
    }

    private Map<String, String> parseHeader(BufferedReader br) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        String line = br.readLine();
        while(!line.equals("")) {
            String[] headers = line.split(":");
            map.put(headers[0], headers[1].trim());
            line = br.readLine();
        }

        return map;
    }

    private Map<String, String> parseParameter(BufferedReader br, String url) throws IOException {
        String body = null;
        if (this.isGetMethod()) {
            int idx = url.indexOf("?");
            body = url.substring(idx + 1);
            System.out.println(idx);
            System.out.println(url.substring(idx + 1, url.length()));
            System.out.println(body);
        }

        if (this.isPostMethod()) {
            int length = Integer.parseInt(this.headers.get("Content-Length").trim());
            body = IOUtils.readData(br, length);
        }

        return HttpRequestUtils.parseQueryString(body);
    }

    private boolean isGetMethod() {
        return this.method == "GET";
    }

    private boolean isPostMethod() {
        return this.method == "POST";
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getHeader(String headerName) {
        return this.headers.get(headerName);
    }

    public String getParameter(String paramName) {
        return this.parameters.get(paramName);
    }
}
