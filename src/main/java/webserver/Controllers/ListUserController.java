package webserver.Controllers;

import db.DataBase;
import model.User;
import webserver.AbstractController;
import webserver.Controller;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Collection;

public class ListUserController extends AbstractController implements Controller {
    @Override
    public void service(HttpRequest req, HttpResponse res) throws IOException {
        super.service(req, res);
    }

    public void doPost(HttpRequest req, HttpResponse res) {

    }

    public void doGet(HttpRequest req, HttpResponse res) throws IOException {
        if (!res.isLogin()) {
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
    }
}
