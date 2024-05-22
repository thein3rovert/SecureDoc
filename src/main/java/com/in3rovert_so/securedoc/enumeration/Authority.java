package com.in3rovert_so.securedoc.enumeration;

import static com.in3rovert_so.securedoc.constant.Constants.*;

public enum Authority {
    USER(USER_AUTHORITY),
    ADMIN(ADMIN_AUTHORITY),
    SUPER_ADMIN(SUPER_ADMIN_AUTHORITY), //These values are like string we need to have a way to get these strings.
    MANAGER(MANAGER_AUTHORITY);

    private final String value;

    Authority(String value) {
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }
}
