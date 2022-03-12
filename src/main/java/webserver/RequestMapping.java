package webserver;

import webserver.Controllers.CreateUserController;
import webserver.Controllers.ListUserController;
import webserver.Controllers.LoginController;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static Map<String, Controller> controllers = new HashMap<String, Controller>();

    static {
        controllers.put("/user/create", new CreateUserController());
        controllers.put("/user/list", new LoginController());
        controllers.put("/user/login", new ListUserController());
    }

    public static Controller getController(String url) {
        return controllers.get(url);
    }
}
