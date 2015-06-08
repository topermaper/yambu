package com.marcosedo.yambu.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

public class Usuario implements Parcelable {
    private String uid;
    private byte[] image;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private ArrayList<Grupo> groups_followed;
    private boolean checked;


    public Usuario(String uid, byte[] image, String username, String firstname, String lastname, String email) {
        this.uid = uid;
        this.image = image;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.checked = false;
    }

    public static final Creator<Usuario> CREATOR =
            new Creator<Usuario>() {
                @Override
                public Usuario createFromParcel(Parcel parcel) {
                    return new Usuario(parcel);
                }

                @Override
                public Usuario[] newArray(int size) {
                    return new Usuario[size];
                }
            };

    //CONSTRUCTOR PARCELABLE
    public Usuario(Parcel parcel) {
        //seguir el mismo orden que el usado en el método writeToParcel
        this.uid = parcel.readString();
        this.image = (byte[]) parcel.readSerializable();
        this.username = parcel.readString();
        this.firstname = parcel.readString();
        this.lastname = parcel.readString();
        this.email = parcel.readString();
        this.checked = parcel.readInt() == 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(uid);
        parcel.writeByteArray(image);
        parcel.writeString(username);
        parcel.writeString(firstname);
        parcel.writeString(lastname);
        parcel.writeString(email);
        parcel.writeInt(checked ? 0 : 1 );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return uid;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getFirstname() {
        return firstname;
    }

    public void setRegId(String firstname) {
        this.firstname = firstname;
    }
    public String getLastName() {
        return lastname;
    }

    public void setLastName(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isChecked(){
        return checked;
    }

    public void setChecked(boolean checked){
        this.checked = checked;
    }

    public void print(String tag) {
        Log.i(tag, "ID = " + uid + "\nFirstname = " + firstname + "\nLastname = " + lastname+ "\nUsername = " + username + "\nEmail = " + email);
    }
}