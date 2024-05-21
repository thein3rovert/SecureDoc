package com.in3rovert_so.securedoc.domain;

public class RequestContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>(); // Allows us to create userId variables in every thread
    // and also set and get the userid (its like a typeof the variables)
    private RequestContext() {

    }
    //Setting the variable to nul
     public static void start() {
        USER_ID.remove();
    } //Allows us to initialise everything.


    //SETTER AND GETTER FOR THE VARIABLES
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

}
