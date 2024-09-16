package com.in3rovert_so.securedoc.domain;

public class RequestContext {
    // CREATE NEW USERID FOR EVERY THREAD-LOCAL
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private RequestContext() {

    }
    /*Remove values associated with the USER_ID variable from the thread object,
    this ensures that any previous user context is clear and new user context
    can be created
     */
     public static void start() {
        USER_ID.remove();
    } //Allows us to initialise everything.


    //SETTER AND GETTER FOR THE USERID
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

}
