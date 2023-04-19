package org.openspp.keycloak.user.storage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQL10Dialect;

public enum JDBC {

    POSTGRESQL("PostgreSQL 10+", org.postgresql.Driver.class.getName(), "SELECT 1", new PostgreSQL10Dialect());

    private final String desc;
    private final String driver;
    private final String testString;
    private final Dialect dialect;

    JDBC(String desc, String driver, String testString, Dialect dialect) {
        this.desc = desc;
        this.driver = driver;
        this.testString = testString;
        this.dialect = dialect;
    }

    public static JDBC getByDescription(String desc) {
        for (JDBC value : values()) {
            if (value.desc.equals(desc)) {
                return value;
            }
        }
        return null;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public static List<String> getAllDescriptions() {
        return Arrays.stream(values()).map(JDBC::getDesc).collect(Collectors.toList());
    }

    public String getDesc() {
        return desc;
    }

    public String getDriver() {
        return driver;
    }

    public String getTestString() {
        return testString;
    }
}
