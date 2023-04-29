package com.wangkaixuan.svpserver;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class Config {
    private static Map content;

    public static Map ins() {
        if (content != null) {
            return content;
        }
        var fileName = System.getenv("SVP_CONF_FILE");
        System.out.printf("config file:%s\n", fileName);
        try {
            YamlReader reader = new YamlReader(new FileReader(fileName));
            content = (Map) reader.read();
        } catch (IOException ex) {
            System.out.printf("error:%s", ex);
            System.exit(-1);
        }

        return content;
    }
}
