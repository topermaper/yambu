package com.marcosedo.yambu.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Dispositivo implements Parcelable{
	private String devid;
	private byte[] fotoperfil;
    private String nick;
    private boolean checked;
    private String regid;
 
    public Dispositivo(String devid, byte[] fotoperfil, String nick,String regid, boolean checked) {
        this.devid = devid;
    	this.fotoperfil = fotoperfil;
        this.nick = nick;
        this.regid = regid;
        this.checked = checked;     
    }
    
    public static final Parcelable.Creator<Dispositivo> CREATOR =
            new Parcelable.Creator<Dispositivo>(){
                @Override
                public Dispositivo createFromParcel(Parcel parcel)
                {
                    return new Dispositivo(parcel);
                }
                @Override
                public Dispositivo[] newArray(int size)
                {
                    return new Dispositivo[size];
                }
        };
        
        //CONSTRUCTOR PARCELABLE
        public Dispositivo(Parcel parcel) {
        	//seguir el mismo orden que el usado en el método writeToParcel
            this.devid = parcel.readString();
            this.fotoperfil = (byte[]) parcel.readSerializable();
            this.nick = parcel.readString();
            this.regid = parcel.readString();
            this.checked = parcel.readInt() == 0;
        } 
            
        @Override
        public void writeToParcel(Parcel parcel, int flags){
        	parcel.writeString(devid);
        	parcel.writeByteArray(fotoperfil); 
        	parcel.writeString(nick);
        	parcel.writeString(regid);
        	parcel.writeInt(checked ? 0 : 1 );
        }
             
        @Override
        public int describeContents(){
                return 0;
        }


    public String getDevId() {
        return devid;
    }
 
    public void setDevId(String devid) {
        this.devid = devid;
    }
    
    public byte[] getFotoPerfil() {
        return fotoperfil;
    }
 
    public void setFotoPerfil(byte[] fotoperfil) {
        this.fotoperfil = fotoperfil;
    }
 
    public String getNick() {
        return nick;
    }
 
    public void setNick(String nick) {
        this.nick = nick;
    }
    
    public boolean isChecked()
    {
        return checked;
    }
 
    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }
    
    public String getRegId() {
        return regid;
    }
 
    public void setRegId(String regid) {
        this.regid = regid;
    }
    
    public void print(String tag){
    	Log.i(tag,"DEVID = "+devid+"\nREGID = "+regid+"\nNICK = "+nick);
    }
}