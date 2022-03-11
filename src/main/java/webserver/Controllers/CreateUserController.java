package webserver.Controllers;

import db.DataBase;
import model.User;
import webserver.AbstractController;
import webserver.Controller;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class CreateUserController extends AbstractController implements Controller {
    @Override
    public void service(HttpRequest req, HttpResponse res) throws IOException {
        super.service(req, res);
    }

    public void doPost(HttpRequest req, HttpResponse res) {
        User user = new User(
            req.getParameter("userId"),
            req.getParameter("password"),
            req.getParameter("name"),
            req.getParameter("email")
        );
        DataBase.addUser(user);
        res.sendRedirect("/index.html");
    }

    public void doGet(HttpRequest req, HttpResponse res) {

    }

}
