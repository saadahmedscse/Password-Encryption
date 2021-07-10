package com.caffeine.encryptedify.Model;

public class UserDetails {
    String uid, name, email, password, uri, key, total;

    public UserDetails() {
    }

    public UserDetails(String uid, String name, String email, String password, String uri, String key, String total) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.uri = uri;
        this.key = key;
        this.total = total;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUri() {
        return uri;
    }

    public String getKey() {
        return key;
    }

    public String getTotal() {
        return total;
    }
}
