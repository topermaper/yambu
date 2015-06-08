
package com.marcosedo.yambu.app;
/**
 * Created by Marcos on 14/04/15.
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.ArrayList;



public class Comment implements Parcelable {
    //private String uid;
    private byte[] image;
    private String username;
    private String txt;
    private String timestamp;



    public Comment(byte[] image, String txt, String username,String timestamp) {

        this.image = image;
        this.username = username;
        this.txt = txt;
        this.timestamp = timestamp;
    }

    public static final Creator<Comment> CREATOR =
            new Creator<Comment>() {
                @Override
                public Comment createFromParcel(Parcel parcel) {
                    return new Comment(parcel);
                }

                @Override
                public Comment[] newArray(int size) {
                    return new Comment[size];
                }
            };

    //CONSTRUCTOR PARCELABLE
    public Comment(Parcel parcel) {
        Log.i("PARCEL",parcel.toString());
        //seguir el mismo orden que el usado en el metodo writeToParcel
        //this.uid = parcel.readString();
        this.image = new byte[parcel.readInt()];
        parcel.readByteArray(this.image);
        this.username = parcel.readString();
        this.txt = parcel.readString();
        this.timestamp = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        //parcel.writeString(uid);
        parcel.writeInt(image.length);
        parcel.writeByteArray(image);
        parcel.writeString(username);
        parcel.writeString(txt);
        parcel.writeString(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /*public String getId() {
        return uid;
    }*/

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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public String getTxt() {
        return this.txt;
    }

    public void print(String tag) {
        Log.i(tag, "username = " + username + "\ntexto = " + txt + "\ntimestamp = " + timestamp);
    }
}