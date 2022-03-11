package webserver.Controllers;

import db.DataBase;
import model.User;
import webserver.AbstractController;
import webserver.Controller;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class LoginController extends AbstractController implements Controller {
    @Override
    public void service(HttpRequest req, HttpResponse res) throws IOException {
        super.service(req, res);
    }

    public void doPost(HttpRequest req, HttpResponse res) throws IOException {
        User user = DataBase.findUserById(req.getParameter("userId"));
        if (user == null) {
            res.forward("/user/login_failed.html");
            return ;
        }

        res.setLogin(true);
        res.sendRedirect("/index.html");
    }

    public void doGet(HttpRequest req, HttpResponse res) {

    }
}

// 각 분기문을 controller 인터페이스를 구현하는 클래스를 만들어 분리
// 생성한 controller 구현체를 Map<String, String>에 저장한다. key는 url, value는 controller 구현체이다.
// 클라이언트 요청 url에 해당하는 controller를 찾아 service 메소드를 호출한다.