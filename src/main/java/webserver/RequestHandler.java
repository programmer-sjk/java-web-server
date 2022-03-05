package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import db.DataBase;
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
            byte[] resBody = "Hello World".getBytes();

            String[] httpHeaders = HttpRequestUtils.parseHeader(br);
            String method = httpHeaders[0];
            String url = httpHeaders[1];

//            HttpRequestUtils.printHttpHeader(br);
            DataOutputStream dos = new DataOutputStream(out);

            if (url.equals("/user/create")) {
                String body = null;
                if (method.equals("GET")) {
                    body = UrlUtils.getQueryString(url);
                }

                if (method.equals("POST")) {
                    int contentLength = HttpRequestUtils.contentLength(br);
                    body = IOUtils.readData(br, contentLength);
                }

                Map<String, String> qs = HttpRequestUtils.parseQueryString(body);
                User user = new User(qs.get("userId"), qs.get("password"), qs.get("name"), qs.get("email"));
                DataBase.addUser(user);

                response302Header(dos, "http://localhost:8080/index.html");
            }

            boolean isLogin = false;
            if (url.equals("/user/login")) {
                int contentLength = HttpRequestUtils.contentLength(br);
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> qs = HttpRequestUtils.parseQueryString(body);
                User user = DataBase.findUserById(qs.get("userId"));
                System.out.println(user);
                if (user != null) {
                    isLogin = true;
                }
            }

            if (url.equals("/user/list")) {
                String cookie = HttpRequestUtils.cookie(br);
                Map<String, String> cookieQs = HttpRequestUtils.parseCookies(cookie);
                if(Boolean.parseBoolean(cookieQs.get("logined"))) {
                    StringBuilder sb = new StringBuilder();
                    Collection<User> users = DataBase.findAll();
                    users.forEach((user) -> sb.append(user.toString()));
                    System.out.println(sb.toString());
                    resBody = sb.toString().getBytes();

                } else {
                    response302Header(dos, "http://localhost:8080/user/login.html");
                }
            }


            if (url.contains(".html")) {
                resBody = IOUtils.getFile(url);
            }

            response200Header(dos, resBody.length, isLogin);
            responseBody(dos, resBody);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, boolean isLogin) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + isLogin + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String redirectPath) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: " + redirectPath + "\r\n");
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
