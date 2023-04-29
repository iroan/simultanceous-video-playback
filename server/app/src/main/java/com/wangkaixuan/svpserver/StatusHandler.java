package com.wangkaixuan.svpserver;

import com.alibaba.fastjson.JSON;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.lettuce.core.KeyValue;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatusHandler implements HttpHandler {
    private int statusOk;
    private final RedisCommands<String, String> sync;

    public StatusHandler() {
        sync = RedisConn.Sync();
    }

    private boolean verifyToken(String acc, String token) {
        var sync = RedisConn.Sync();
        var user = (List<KeyValue<String, String>>) sync.hmget("user:" + acc, "token");
        for (KeyValue<String, String> k : user) {
            var val = (String) k.getValueOrElse("");
            if (token.equals(val)) {
                return true;
            }
        }
        return false;
    }

    private Status process(Status s, String m) throws Exception {
        if (m.equals("POST")) {
            return processPOST(s);
        } else if (m.equals("GET")) {
            return processGET(s);
        } else {
            throw new HttpException(403, "invalid method");
        }
    }

    private Status processGET(Status s) throws HttpException {
        var result = new Status();
        if (s.action.equals("masterAccount")) {
            result = masterAccount();
        } else if (s.action.equals("sessionProgress")) {
            result = sessionProgress(s);
        } else {
            throw new HttpException(404, "not found");
        }

        return result;
    }

    private Status masterAccount() {
        var result = new Status();
        var sessions = (List<String>) sync.keys("session:*");
        result.extraArray = new ArrayList<String>();
        for (var session : sessions) {
            var item = session.split(":");
            result.extraArray.add(item[1]);
        }
        result.httpStatusCode = 200;
        return result;
    }

    private Status sessionProgress(Status s) {
        var result = new Status();
        var key = String.format("session:%s", s.master);
        var arr = sync.hmget(key, "progress");
        for (var kv : arr) {
            result.progress = kv.getValue();
        }
        result.httpStatusCode = 200;
        return result;
    }

    private Status processPOST(Status s) throws Exception {
        var res = new Status();
        if (s.action.equals("newSession")) {
            res = newSession(s);
        } else if (s.action.equals("updateProgress")) {
            res = updateProgress(s);
        } else if (s.action.equals("joinSession")) {
            res = joinSession(s);
        } else {
            throw new HttpException(404, "not found");
        }

        return res;
    }

    private Status updateProgress(Status s) {
        var progress = new HashMap<String, String>();
        progress.put("progress", s.progress);
        sync.hmset("session:" + s.account, progress);
        var tmp = new Status();
        tmp.success = true;
        tmp.httpStatusCode = 200;
        return tmp;
    }

    private Status joinSession(Status s) throws HttpException {
        var key = String.format("session:%s", s.master);
        var sha1 = "";
        var arr = sync.hmget(key, "videoSHA1");
        for (var kv : arr) {
            sha1 = kv.getValue();
        }

        var result = new Status();

        if (sha1.equals(s.videoSHA1)) {
            key = String.format("slave:%s", s.master);
            // TODO should be set
            sync.lpush(key, s.account);
            result.httpStatusCode = 200;
            result.success = true;
        } else {
            throw new HttpException(403, "there are different video.");
        }

        return result;
    }

    private Status newSession(Status s) {
        var session = new HashMap<String, String>();
        session.put("videoSHA1", s.videoSHA1);
        sync.hmset("session:" + s.account, session);
        var tmp = new Status();
        tmp.success = true;
        tmp.httpStatusCode = 200;
        return tmp;
    }

    private Status parseBody(HttpExchange exc) throws IOException {
        InputStream in = exc.getRequestBody();
        var body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        System.out.printf("body: %s\n", body);
        return JSON.parseObject(body, Status.class);
    }

    private Status parseQueryString(HttpExchange exc) throws IOException {
        var queryStr = exc.getRequestURI().getQuery();
        System.out.printf("query string: %s\n", queryStr);

        var jsonArr = new ArrayList<String>();
        var tmp = queryStr.split("&");
        for (var item : tmp) {
            var kv = item.replace("=", "\":\"");
            jsonArr.add(String.format("\"%s\"\n", kv));
        }
        var jsonStr = String.format("{%s}\n", String.join(",", jsonArr));
        return JSON.parseObject(jsonStr, Status.class);
    }

    public void handle(HttpExchange exc) throws IOException {
        String method = exc.getRequestMethod();
        var status = new Status();
        var result = new Status();

        try {
            if (method.equals("POST")) {
                status = parseBody(exc);
            } else if (method.equals("GET")) {
                status = parseQueryString(exc);
            } else {
                throw new HttpException(400, "unsupported http method");
            }

            if (this.verifyToken(status.account, status.token)) {
                result = this.process(status, method);
                result.httpStatusCode = 200;
                result.success = true;
            } else {
                throw new HttpException(403, "invalid token");
            }
        } catch (HttpException e) {
            result = e.getStatus();
        } catch (Exception e) {
            result.success = false;
            result.errMsg = e.getMessage();
            result.httpStatusCode = 500;
        } finally {
            var code = result.httpStatusCode;
            result.httpStatusCode = null;
            var response = JSON.toJSONString(result);
            exc.sendResponseHeaders(code, response.getBytes().length);
            OutputStream os = exc.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.printf("response:%s\n", response);
        }
    }
}
