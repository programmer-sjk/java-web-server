package webserver;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import util.UrlUtils;
import webserver.Controllers.CreateUserController;

import javax.xml.crypto.Data;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private Map<String, String> controllerMap;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        this.controllerMap = new HashMap<String, String>();
    }
    public void initControllers() {
        controllerMap.put("/user/create", "webserver.Controllers.CreateUserController");
        controllerMap.put("/user/list", "webserver.Controllers.ListUserController");
        controllerMap.put("/user/login", "webserver.Controllers.LoginController");
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        this.initControllers();

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest req = new HttpRequest(in);
            HttpResponse res = new HttpResponse(out);

            String url = req.getPath();
            String controllerName = controllerMap.get(url);

            if (controllerName != null) {
                boolean logined = isLogin(req.getHeader("Cookie"));
                res.setLogin(logined);

                Class<?> controllerClass = Class.forName(controllerName);
                Constructor<?> constructor = controllerClass.getConstructor(null);
                Object object = constructor.newInstance();

                Method method = controllerClass.getDeclaredMethod("service", req.getClass(), res.getClass());
                method.invoke(object, req, res);
            } else if (url.endsWith(".css")) {
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                res.response200CssHeader(body.length);
                res.responseBody(body);
            } else {
                res.forward(url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
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
