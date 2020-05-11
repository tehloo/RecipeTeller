package com.example.recipe_teller;

public class UserInfo {

    private String userName;
    private String userAge;
    private String userGender;

    public UserInfo(String userName, String userAge, String userGender){
        this.userName = userName;
        this.userAge = userAge;
        this.userGender = userGender;
    }

    public String getUserName(){
        return this.userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getUserAge(){
        return this.userAge;
    }

    public void setUserAge(String userAge){
        this.userAge = userAge;
    }

    public String getUserGender(){
        return this.userGender;
    }

    public void setUserGender(String userGender){
        this.userGender = userGender;
    }
}
