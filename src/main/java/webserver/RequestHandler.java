package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import util.UrlUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String[] httpHeaders = HttpRequestUtils.parseHeader(br);
            String method = httpHeaders[0];
            String url = httpHeaders[1];

//            HttpRequestUtils.printHttpHeader(br);
            if (method.equals("GET")) {
                String queryString = UrlUtils.getQueryString(url);
                Map<String, String> qs = HttpRequestUtils.parseQueryString(queryString);
                User user = new User(qs.get("userId"), qs.get("password"), qs.get("name"), qs.get("email"));
                System.out.println(user);
            }

            if (method.equals("POST")) {
                int contentLength = HttpRequestUtils.contentLength(br);
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> qs = HttpRequestUtils.parseQueryString(body);
                User user = new User(qs.get("userId"), qs.get("password"), qs.get("name"), qs.get("email"));
                System.out.println(user);
            }




            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = "Hello World".getBytes();

            if (url.length() > 1) {
                body = IOUtils.getFile(url);
            }

            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
