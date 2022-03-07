package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

import javax.xml.crypto.Data;

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
            String line = br.readLine();
            log.debug("request line: {}", line);

            if (line == null) {
                return;
            }

            String[] tokens = line.split(" ");
            boolean logined = false;
            int contentLength = 0;

            while(!line.equals("")) {
                line = br.readLine();
                log.debug("header: {}", line);
                if (line.contains("Content-Length")) {
                    contentLength = getContentLength(line);
                }

                if (line.contains("Cookie")) {
                    logined = isLogin(line);
                }
            }

            String url = tokens[1];
            DataOutputStream dos = new DataOutputStream(out);
            if ("/user/create".equals(url)) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);
                response302Header(dos, "/index.html");
            } else if ("/user/login".equals(url)) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = DataBase.findUserById(params.get("userId"));
                if (user == null) {
                    responseResource(out, "/user/login_failed.html");
                    return ;
                }

                response302LoginSuccessHeader(dos);
            } else if ("/user/list".equals(url)) {
                if (!logined) {
                    responseResource(out, "/user/login.html");
                    return ;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                for(User user: users) {
                    sb.append(user);
                }

                byte[] body = sb.toString().getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else if (url.endsWith(".css")) {
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

                response200CssHeader(dos, body.length);
                responseBody(dos, body);
            } else {
                responseResource(out, url);
            }



        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private int getContentLength(String line) {
       String[] headerTokens = line.split(":");
       return Integer.parseInt(headerTokens[1].trim());
    }

    private boolean isLogin(String line) {
        String[] tokens = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(tokens[1].trim());
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    public void myRun() {
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


            if (url.contains(".html") || url.contains(".css")) {
                resBody = IOUtils.getFile(url);
            }

            myResponse200Header(dos, resBody.length, isLogin, url.contains(".css"));
            responseBody(dos, resBody);
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

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void myResponse200Header(DataOutputStream dos, int lengthOfBodyContent, boolean isLogin, boolean isCss) {
        String type = isCss ? "text/css" : "text/html";
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + type + ";charset=utf-8\r\n");
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
