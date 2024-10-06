package com.in3rovert_so.securedoc.enumeration;

import static com.in3rovert_so.securedoc.constant.Constants.*;

public enum Authority {
    /*
    =================
    Users Authorities and Values
    [USER, ADMIN, SUPER_ADMIN,MANAGER]
    =================
     */
    USER(USER_AUTHORITIES),
    ADMIN(ADMIN_AUTHORITIES),
    SUPER_ADMIN(SUPER_ADMIN_AUTHORITIES), //These values are like string we need to have a way to get these strings.
    MANAGER(MANAGER_AUTHORITIES);

    private final String value;
    // Allow us to create new instance of the Authority with the String values.
    Authority(String value) {
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }
}
