package com.marcosedo.yambu.app;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;


public class Evento implements Parcelable {
    public static final Parcelable.Creator<Evento> CREATOR =
            new Parcelable.Creator<Evento>() {
                @Override
                public Evento createFromParcel(Parcel parcel) {
                    return new Evento(parcel);
                }

                @Override
                public Evento[] newArray(int size) {
                    return new Evento[size];
                }
            };
    private String id;
    private byte[] cartel;
    private byte[] thumb; //imagen reducida para no usar tanta red
    private String placeName;
    private String placeId;
    private Double latitude;
    private Double longitude;
    private String fecha;
    private String url;
    private int precio;
    private String currencyCode;
    private String hora;
    public static Comparator<Evento> DateComparator = new Comparator<Evento>() {

        @Override
        public int compare(Evento e1, Evento e2) {//Orden ascendentemente for fecha y luego por hora en caso de empate

            final int DIA = 0;
            final int MES = 1;
            final int AÑO = 2;
            final int HOR = 0;
            final int MIN = 1;

            //cogemos la fecha en string y la pasamos a entero para comparar mejor
            String[] fechaString1 = e1.getFecha().split("/");
            String[] fechaString2 = e2.getFecha().split("/");

            Integer[] fechaInt1 = new Integer[3];
            Integer[] fechaInt2 = new Integer[3];

            for (int i = 0; i < 3; i++) {
                fechaInt1[i] = Integer.parseInt(fechaString1[i]);
                fechaInt2[i] = Integer.parseInt(fechaString2[i]);
            }
            ///////////////////////////////////////////////////////////////////
            //cogemos la hora en string y la pasamos a entero para comparar mejor
            String[] horaString1 = e1.getHora().split(":");
            String[] horaString2 = e1.getHora().split(":");

            Integer[] horaInt1 = new Integer[2];
            Integer[] horaInt2 = new Integer[2];

            for (int i = 0; i < 2; i++) {
                horaInt1[i] = Integer.parseInt(horaString1[i]);
                horaInt2[i] = Integer.parseInt(horaString2[i]);
            }
            ///////////////////////////////////////////////////////////////////

            if (fechaInt1[AÑO].compareTo(fechaInt2[AÑO]) == 0) {
                if (fechaInt1[MES].compareTo(fechaInt2[MES]) == 0) {
                    if (fechaInt1[DIA].compareTo(fechaInt2[DIA]) == 0) {
                        if (horaInt1[HOR].compareTo(horaInt2[HOR]) == 0) {
                            return horaInt1[MIN].compareTo(horaInt2[MIN]);
                        } else {
                            return horaInt1[HOR].compareTo(horaInt2[HOR]);
                        }
                    } else {
                        return fechaInt1[DIA].compareTo(fechaInt2[DIA]);
                    }
                } else {
                    return fechaInt1[MES].compareTo(fechaInt2[MES]);
                }
            } else {
                return fechaInt1[AÑO].compareTo(fechaInt2[AÑO]);
            }
        }
    };
    private String idgrupo;
    private String nombregrupo;
    /*Comparator for sorting the list by Student Name*/
    public static Comparator<Evento> NameComparator = new Comparator<Evento>() {

        @Override
        public int compare(Evento e1, Evento e2) {
            String name1 = e1.getNombreGrupo().toUpperCase();
            String name2 = e2.getNombreGrupo().toUpperCase();

            //ascending order
            return name1.compareTo(name2);

        }
    };
    private Integer followed;
    private String separatorText;
    private boolean hided;//if TRUE this event won't be shown
    private boolean editable;//it would be editable if we are the creator

    public Evento(String id, String idgrupo, String nombregrupo, byte[] cartel, byte[] thumb, String placeName, String placeId,Double latitude,Double longitude, String fecha, String hora, int precio, String currencyCode, String url) {
        this.id = id;
        this.cartel = cartel;
        this.thumb = thumb;
        this.placeName = placeName;
        this.placeId = placeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fecha = fecha;
        this.hora = hora;
        this.precio = precio;
        this.url = url;
        this.idgrupo = idgrupo;
        this.nombregrupo = nombregrupo;
        this.followed = 0;
        this.separatorText = "";
        this.hided = false;
        this.editable = false;
        this.currencyCode = currencyCode;

    }

    //CONSTRUCTOR PARCELABLE
    public Evento(Parcel parcel) {
        //seguir el mismo orden que el usado en el método writeToParcel
        this.id = parcel.readString();
        this.cartel = (byte[]) parcel.readSerializable();
        this.thumb = (byte[]) parcel.readSerializable();
        this.placeName = parcel.readString();
        this.placeId = parcel.readString();
        this.latitude = parcel.readDouble();
        this.longitude = parcel.readDouble();
        this.fecha = parcel.readString();
        this.hora = parcel.readString();
        this.precio = parcel.readInt();
        this.currencyCode = parcel.readString();
        this.url = parcel.readString();
        this.idgrupo = parcel.readString();
        this.nombregrupo = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeByteArray(cartel);
        parcel.writeByteArray(thumb);
        parcel.writeString(placeName);
        parcel.writeString(placeId);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(fecha);
        parcel.writeInt(precio);
        parcel.writeString(currencyCode);
        parcel.writeString(url);
        parcel.writeString(idgrupo);
        parcel.writeString(nombregrupo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getCartel() {
        return cartel;
    }

    public void setCartel(byte[] cartel) {
        this.cartel = cartel;
    }

    public byte[] getThumb() {
        return thumb;
    }

    public void setThumb(byte[] thumb) {
        this.thumb = thumb;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setIdgrupo(String id) {
        this.idgrupo = id;
    }

    public String getIdGrupo() {
        return idgrupo;
    }

    public void setNombregrupo(String nombregrupo) {
        this.nombregrupo = nombregrupo;
    }

    public String getNombreGrupo() {
        return nombregrupo;
    }

    public int getFollowed() {
        return followed;
    }

    public void setFollowed(Integer followed) {
        this.followed = followed;
    }

    public String getSeparatorText() {
        return separatorText;
    }

    public void setSeparatorText(String text) {
        this.separatorText = text;
    }

    public boolean getHided() {
        return hided;
    }

    public void setHided(boolean hided) {
        this.hided = hided;
    }

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

}