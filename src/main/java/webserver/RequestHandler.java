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
            HttpRequest req = new HttpRequest(in);
            HttpResponse res = new HttpResponse(out);

            String url = req.getPath();
            boolean logined = isLogin(req.getHeader("Cookie"));
            res.setLogin(logined);

            if ("/user/create".equals(url)) {
                User user = new User(
                    req.getParameter("userId"),
                    req.getParameter("password"),
                    req.getParameter("name"),
                    req.getParameter("email")
                );
                DataBase.addUser(user);
                res.sendRedirect("/index.html");
            } else if ("/user/login".equals(url)) {
                User user = DataBase.findUserById(req.getParameter("userId"));
                if (user == null) {
                    res.forward("/user/login_failed.html");
                    return ;
                }

                res.setLogin(true);
                res.sendRedirect("/index.html");
            } else if ("/user/list".equals(url)) {
                if (!logined) {
                    res.forward("/user/login.html");
                    return ;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                for(User user: users) {
                    sb.append(user);
                }

                byte[] body = sb.toString().getBytes();
                res.response200Header(body.length);
                res.responseBody(body);
            } else if (url.endsWith(".css")) {
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                res.response200CssHeader(body.length);
                res.responseBody(body);
            } else {
                res.forward(url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String cookie) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookie);
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
