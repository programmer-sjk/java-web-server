package webserver;

import java.io.IOException;

public abstract class AbstractController implements Controller {
    @Override
    public void service(HttpRequest req, HttpResponse res) throws IOException {
        if (req.isGetMethod()) {
            doGet(req, res);
        }

        if (req.isPostMethod()) {
            doPost(req, res);
        }

    }
    protected abstract void doPost(HttpRequest req, HttpResponse res) throws IOException;
    protected abstract void doGet(HttpRequest req, HttpResponse res) throws IOException;
}
