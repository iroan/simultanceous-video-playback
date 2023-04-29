package com.wangkaixuan.svpserver;

import com.alibaba.fastjson.JSON;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class LoginHandler implements HttpHandler {

    private int statusOk;
    private String response;
    private MySQL db;
    private Connection conn;

    public LoginHandler() {
        this.db = new MySQL();
        this.conn = db.getConn();
    }

    /**
     * 验证登录用户的账户和密码 1. 加盐计算密码的digest 2. 根据account查询user表记录 3.
     * 如果不存在，则注册用户，插入account、password 4. 如何存在，则比较digest是否相同
     *
     * @param u
     * @return true:验证成功，可以登录；
     * @throws IOException
     */
    public boolean verify(User u) throws IOException {
        boolean res = false;
        try {
            var stat = conn.createStatement();
            var template = "select account, hex(pass_digest) as digest from user where account='%s';";
            var sql = String.format(template, u.getAccount());
            var rs = stat.executeQuery(sql);

            var digest = u.passwordSHA3().toLowerCase();

            if (rs.next()) {
                var digestInDB = rs.getString("digest");
                res = digest.equals(digestInDB.toLowerCase());
            } else {
                template = "INSERT INTO `svp`.`user` (`account`, `pass_digest`, `utime`, `ctime`) VALUES ('%s', 0x%s, now(), now());";
                sql = String.format(template, u.getAccount(), digest);
                res = stat.executeUpdate(sql) > 0;
            }
        } catch (SQLException ex) {
            for (Throwable t : ex) {
                System.out.printf("dberror: %s\n", t.getMessage());
            }
        }
        return res;
    }

    public String makeOnline(String account) {
        var token = UUID.randomUUID().toString();
        var user = new HashMap<String, String>();
        var timestamp = Instant.now().getEpochSecond();
        user.put("token", token);
        user.put("loginTime", String.format("%d", timestamp));
        var sync = RedisConn.Sync();
        sync.hmset("user:" + account, user);
        return token;
    }

    class Person {
        public String account;
        public String token;
        public String errMsg;
        public boolean ok;

        public Person(String n, String a, String e, boolean o) {
            account = n;
            token = a;
            errMsg = e;
            ok = o;
        }
    }

    public void handle(HttpExchange t) throws IOException {
        String method = t.getRequestMethod();
        if (method.equals("POST")) {
            InputStream in = t.getRequestBody();
            var body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            System.out.printf("body:%s\n", body);

            User u = JSON.parseObject(body, User.class);
            if (this.verify(u)) {
                var token = makeOnline(u.getAccount());
                var person = new Person(u.getAccount(), token, "", true);
                response = JSON.toJSONString(person);
                statusOk = 200;
            } else {
                var person = new Person(u.getAccount(), "", "account or password is wrong", false);
                response = JSON.toJSONString(person);
                statusOk = 200;
            }
        } else {
            statusOk = 404;
            response = "This method is invaild.";
        }

        t.sendResponseHeaders(statusOk, response.getBytes().length);
        OutputStream os = t.getResponseBody();
        System.out.printf("response:%s\n", response);
        os.write(response.getBytes());
        os.close();
    }
}