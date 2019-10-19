package com.test.learn;

public class UserInfo {

    public static  boolean logined=false;
    public static  String fullname;
    public static  String username;
    public static  String usertype;
    public static  String email;
    public static  String department;

    static void login(String _username, String _fullname, String _usertype, String _email, String _department)
    {
        fullname = _fullname;
        username = _username;
        usertype = _usertype;
        email = _email;
        department = _department;

    }


    private static final UserInfo ourInstance = new UserInfo();

    public static UserInfo getInstance() {
        return ourInstance;
    }

    private UserInfo() {
    }
}
