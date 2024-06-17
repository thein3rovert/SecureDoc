package com.in3rovert_so.securedoc.enumeration;

public enum TokenType {
    //Define enums
    ACCESS("access-token"), REFRESH("refresh-token");
    private final String value; //What ever is defined inside of the enum "" is what i call value.

    //Define Constructor
    TokenType(String value) {
        this.value = value;
    }
    //Define getter for the value
    public String getValue() {
        return this.value;
    }

}
