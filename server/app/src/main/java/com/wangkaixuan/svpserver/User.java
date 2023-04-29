package com.wangkaixuan.svpserver;

import org.apache.commons.codec.binary.Hex;
import com.github.aelstad.keccakj.fips202.*;
import java.security.MessageDigest;
import java.util.List;

public class User {
    private String account;
    private String password;
    private static final String salt = "6BF05B3E-28D4-405D-ACB8-CC817DA0B571";

    public String getAccount() {
        return account;
    }

    public String passwordSHA3() {
        MessageDigest md = new SHA3_256();
        var strs = List.of(User.salt, this.password, this.account);
        String merge = String.join("@", strs);
        byte[] res = md.digest(merge.getBytes());
        return Hex.encodeHexString(res);
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
