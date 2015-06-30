package com.usman.gcm.crowdapps.Model;

/**
 * Created by usman on 5/28/15.
 */
public class Box {

    private int id;
    private int category_id;
    private String title;
    private String description;
    private String image_path;

    public Box() {
    }

    public Box(int id,int category_id, String title, String description, String image_path) {
        this.id = id;
        this.category_id = category_id;
        this.title = title;
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

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
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

