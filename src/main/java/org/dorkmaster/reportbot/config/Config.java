package org.dorkmaster.reportbot.config;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Config {

    Map<String, Object> config;

    public Config() {
        config = parse(this.getClass().getResourceAsStream("/default.yaml"));
    }

    public Config(String filename) {
        if (null == filename) {
            config = parse(this.getClass().getResourceAsStream("/default.yaml"));
        }
        else {
            try {
                config = parse(new FileInputStream(filename));
            } catch (FileNotFoundException e) {
                throw new ConfigException(e);
            }
        }
    }

    protected Map<String, Object> parse(InputStream in) {
        Yaml y = new Yaml();
        try {
            return y.load(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    protected Object walk(List<String> parts, Object conf) {
        if (parts.size()> 0 && conf instanceof Map) {
            String p = parts.get(0);
            parts.remove(0);
            return walk(parts, ((Map) conf).get(p));
        }
        return conf;
    }

    public Value find(String path) {
        return get(path);
    }

    public Value get(String path) {
        List<String> parts = new LinkedList<>(Arrays.asList(path.split("\\.")));
        Object v = walk(parts, config);
        if (parts.size() == 0) {
            return new Value(path, v);
        }
        else {
            return new Value(path, null);
        }
    }

    public class Value {
        protected String path;
        protected Object value;

        public Value(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        public Integer asInt() {
            return asInt(null);
        }

        public Integer asInt(Integer def) {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.valueOf((String) value);
            } else {
                return def;
            }
        }

        public String getPath() {
            return path;
        }

        public String asString() {
            return asString(null);
        }

        public String asString(String def) {
            if (null == value) {
                return def;
            }
            return (String) value;
        }

        public Boolean asBoolean() {
            return asBoolean(null);
        }

        public Boolean asBoolean(Boolean def) {
            if (null == value) {
                return def;
            }
            return (Boolean) value;
        }

        public Map<String, Object> asMap() {
            return (Map<String, Object>) value;
        }

        public boolean isNull() {
            return value == null;
        }

        public List asList() {
            return (List) value;
        }
    }
}