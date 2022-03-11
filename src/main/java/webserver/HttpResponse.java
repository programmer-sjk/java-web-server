package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private OutputStream out;
    private DataOutputStream dos;
    private boolean logined;

    public HttpResponse(OutputStream out) throws IOException {
        this.out = out;
        this.dos = new DataOutputStream(out);
        this.logined = false;
    }

    public void forward(String url) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

        response200Header(body.length);
        responseBody(body);
    }

    public void sendRedirect(String url) {
        try {
            DataOutputStream dos = new DataOutputStream(this.out);
            this.dos.writeBytes("HTTP/1.1 302 OK \r\n");
            if (this.logined) {
                dos.writeBytes("Set-Cookie: logined=true \r\n");
            }
            this.dos.writeBytes("Location: " + url + "\r\n");
            this.dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void response200Header(int lengthOfBodyContent) {
        try {
            this.dos.writeBytes("HTTP/1.1 200 OK \r\n");
            this.dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            this.dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            this.dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void response200CssHeader(int lengthOfBodyContent) {
        try {
            this.dos.writeBytes("HTTP/1.1 200 OK \r\n");
            this.dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            this.dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            this.dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void responseBody(byte[] body) {
        try {
            this.dos.write(body, 0, body.length);
            this.dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addHeader(String key, String value) throws IOException {
        this.dos.writeBytes(key + ": " + value + "\r\n");
    }

    public void setLogin(boolean logined) {
        this.logined = logined;
    }

    public boolean isLogin() {
        return this.logined;
    }
}
