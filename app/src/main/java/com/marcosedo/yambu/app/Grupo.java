package com.marcosedo.yambu.app;

import android.os.Parcel;
import android.os.Parcelable;


public class Grupo{
    private String id;
    private byte[] img;
    private byte[] thumb; //imagen reducida para no usar tanta red
    private String nombre;
    private String creador;
    private int numberEvents=0;//cuantos eventos tienen por protagonista a este grupo



    public Grupo(String id, byte[] thumb,String nombre, String creador) {
        this.id = id;
        this.thumb = thumb;
        this.img = null;
        this.creador = creador;
        this.nombre = nombre;
    }


    public Grupo(String id,String nombre, String creador) {
        this.id = id;
        this.thumb = null;
        this.img = null;
        this.creador = creador;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    public byte[] getImg() {
        return this.img;
    }

    public void setThumb(byte[] thumb) {
        this.thumb = thumb;
    }

    public byte[] getThumb() {
        return this.thumb;
    }

    public String getCreador() {
        return this.creador;
    }

    public void setCreador(String creador) {
        this.nombre = creador;
    }

    public int getNumberOfEvents() {
        return this.numberEvents;
    }

    public void setNumberOfEvents(int numberEvents) {
        this.numberEvents = numberEvents;
    }

}