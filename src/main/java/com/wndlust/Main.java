package com.wndlust;

import com.fizzed.rocker.runtime.RockerRuntime;
import com.fizzed.rocker.runtime.StringBuilderOutput;
import one.nio.http.Header;
import one.nio.http.HttpServer;
import one.nio.http.HttpSession;
import one.nio.http.Param;
import one.nio.http.Path;
import one.nio.http.Request;
import one.nio.http.Response;
import one.nio.server.ServerConfig;
import one.nio.util.Utf8;

import java.io.IOException;

public class Main extends HttpServer {

    public Main(ServerConfig config) throws IOException {
        super(config);
    }

    @Path({"/simple"})
    public Response handleSimple() {
        return Response.ok("Simple");
    }

    @Path({"/http"})
    public Response http() {

        StringBuilderOutput output = views.index.template("World")
                .render(StringBuilderOutput.FACTORY);

        String text = output.toString();
        Response response = Response.ok(output.toString());
        response.addHeader("Content-Type: text/html");
        return response;
    }

    @Path({"/multi1", "/multi2"})
    public void handleMultiple(Request request, HttpSession session) throws IOException {
        Response response = Response.ok("Multiple: " + request.getPath());
        session.sendResponse(response);
    }

    @Override
    public void handleRequest(Request request, HttpSession session) throws IOException {
        try {
            super.handleRequest(request, session);
        } catch (RuntimeException e) {
            session.sendError(Response.BAD_REQUEST, e.toString());
        }
    }

    @Path("/param")
    public Response handleParam(@Param("i") int i,
                                @Param("l=123") Long l,
                                @Param(value = "s", required = true) String s,
                                @Header(value = "Host", required = true) String host,
                                @Header("User-Agent") String agent) throws IOException {
        String params = "i = " + i + "\r\nl = " + l + "\r\ns = " + s + "\r\n";
        String headers = "host = " + host + "\r\nagent = " + agent + "\r\n";
        Response response = Response.ok(Utf8.toBytes("<html><body><pre>" + params + headers + "</pre></body></html>"));
        response.addHeader("Content-Type: text/html");
        return response;
    }

    @Override
    public void handleDefault(Request request, HttpSession session) throws IOException {
        Response response = Response.ok(Utf8.toBytes("<html><body><pre>Default</pre></body></html>"));
        response.addHeader("Content-Type: text/html");
        session.sendResponse(response);
    }

    public static void main(String[] args) throws Exception {
//        RockerRuntime.getInstance().setReloading(true);
        String url = args.length > 0 ? args[0] : "socket://0.0.0.0:8080";
        Main server = new Main(ServerConfig.from(url));
        server.start();
    }
}