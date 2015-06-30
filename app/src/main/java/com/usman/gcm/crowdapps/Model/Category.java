package com.usman.gcm.crowdapps.Model;

/**
 * Created by usman on 5/27/15.
 */
public class Category {

    private int id;
    private String title;
    private String description;
    private String image_path;

    public Category(){ // default constructor


    }

    public Category(int id, String title, String description, String image_path){

        this.id = id;
        this. title = title;
        this.description = description;
        this.image_path = image_path;

    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }


}
