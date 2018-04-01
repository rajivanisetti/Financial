package com.android.folio;

/**
 * Created by esuarez on 3/31/18.
 */

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;




public class User {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private String name;
    private String email;
    private Boolean isVirgin;

    //==============================================================================================
    // Constructors
    //==============================================================================================
    public User() {
        // Required empty constructor for Firebase
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.isVirgin = true;
    }

    //==============================================================================================
    // Accessor Methods
    //==============================================================================================
    public String getName() { return this.name; }

    public String getEmail() {return this.email; }

    public Boolean getIsVirgin() {return  this.isVirgin;}

    //==============================================================================================
    // Setter Methods
    //==============================================================================================
    public void setName(String name) { this.name = name; }

    public void setEmail(String email) { this.email = email; }

    public void setIsVirgin(Boolean bool) {this.isVirgin = bool;}

}
