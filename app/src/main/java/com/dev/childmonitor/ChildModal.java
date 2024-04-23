package com.dev.childmonitor;/*
 * *
 *  * Created by Husayn on 22/10/2021, 5:04 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 22/10/2021, 2:29 PM
 *
 */

import java.util.HashMap;

public class ChildModal
{
    private String username;
    private String email;
    private String password;
    private boolean isParent;
    private String battery;
    private String location;
    private String parent_uid;
    private HashMap<String, Long> AppsList;
    private String child_uid;
    private HashMap<String, String> ReminderList;

    // Mandatory empty constructor
    // for use of FirebaseUI
    public ChildModal() {}

    public ChildModal(String username, String email, String password, boolean isParent, String battery, String location, String parent_uid, HashMap<String, Long> AppsList,
                      String child_uid, HashMap<String, String> ReminderList) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.isParent = isParent;
        this.battery = battery;
        this.location = location;
        this.parent_uid = parent_uid;
        this.child_uid = child_uid;
        this.AppsList = AppsList;
        this.ReminderList = ReminderList;
    }

    public HashMap<String, String> getReminderList() {
        return ReminderList;
    }

    public void setReminderList(HashMap<String, String> reminderList) {
        ReminderList = reminderList;
    }

    public String getChild_uid() {
        return child_uid;
    }

    public void setChild_uid(String child_uid) {
        this.child_uid = child_uid;
    }

    public HashMap<String, Long> getAppsList() {
        return AppsList;
    }

    public void setAppsList(HashMap<String, Long> appsList) {
        AppsList = appsList;
    }

    public String getParent_uid() {
        return parent_uid;
    }

    public void setParent_uid(String parent_uid) {
        this.parent_uid = parent_uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean parent) {
        isParent = parent;
    }
}