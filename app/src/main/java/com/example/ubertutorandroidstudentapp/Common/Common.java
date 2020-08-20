package com.example.ubertutorandroidstudentapp.Common;

import com.example.ubertutorandroidstudentapp.Models.StudentModel;

public class Common {

    public static final String STUDENT_INFO_REFERENCE = "StudentInfo";
    public static StudentModel currentUser;

    public static String buildWelcomeMessage() {
        if(Common.currentUser != null){
            return new StringBuilder( "Welcome " )
                    .append( Common.currentUser.getFirstName() )
                    .append( " " )
                    .append( Common.currentUser.getLastName() ).toString();
        }
        else
            return "";
    }
}
