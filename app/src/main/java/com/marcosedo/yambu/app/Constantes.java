package com.marcosedo.yambu.app;

import java.util.ArrayList;

public final class Constantes {
    public static final String SENDER_ID = "734082968872";//este es el project ID de la consola google api

    public static final String REG_ID = "registration_id";
    public static final String APP_VERSION = "appVersion";
    public static final String NICK = "nickname";
    public static final Integer THUMB_SIZE = 64;

    public static final int NUMBER_MAX_COMMENTS_SHOW = 10;//numero maximo de comments que mostraremos

    //URL
    public static final String GCMSERVER_FILE = "gcmserver_main.php";
    public static final String LEER_EVENTOS_FILE = "leer_eventos.php";
    public static final String BORRAR_EVENTOS_FILE = "borrar_evento.php";
    public static final String LOAD_MESSAGES_FILE = "load_messages.php";
    public static final String GUARDAR_USERNAME = "guardar_username.php";
    public static final String GUARDAR_FOTOPERFIL = "guardar_foto_perfil.php";
    public static final String SAVE_PROFILE = "save_profile.php";
    public static final String PUBLISH_COMMENT = "publish_comment.php";
    public static final String EDITAR_EVENTOS_FILE = "editar_eventos.php";
    public static final String LEER_USUARIOS_FILE = "leer_usuarios.php";
    public static final String LOGIN_FILE = "login.php";
    public static final String ADMIN_GRUPOS_FILE = "admin_grupos.php";
    public static final String EJECUTAR_SQL_FILE = "execute_sql.php";
    public static final String GET_IMAGE_FILE = "get_image.php";
    public static final String FOLLOWGROUP = "follow_group.php";
    public static final String LAST_VERSIONCODE = "lastVersionCode.php";


    //TAG deL LOGIN
    public static final String LOGIN_TAG_LOGIN = "login";
    public static final String LOGIN_TAG_REGISTER = "register";
    public static final String LOGIN_TAG_FORPASS = "forpass";
    public static final String LOGIN_TAG_CHGPASS = "chgpass";


    // JSON Node names
    public static final String JSON_SUCCESS = "success";
    public static final String JSON_ERROR = "error";
    public static final String JSON_MESSAGE = "message";
    public static final String JSON_EVENTOS = "eventos";
    public static final String JSON_GRUPOS = "grupos";

    //CAMPOS EVENTOS
    public static final String EV_ID = "_id";
    public static final String EV_IDGRUPO = "idgrupo";
    public static final String EV_LUGAR = "lugar";
    public static final String EV_FECHA = "fecha";
    public static final String EV_HORA = "hora";
    public static final String EV_PRECIO = "precio";
    public static final String EV_URL = "url";
    public static final String EV_CAR = "cartel";
    public static final String EV_THUMB = "thumb";
    public static final String EV_FOLLOWED = "followed";


    //CAMPOS DISPOSITIVOS
    public static final String DEV_ID = "devid";
    public static final String DEV_REGID = "regid";
    public static final String DEV_USER = "user";

    //CAMPOS USUARIOS
    public static final String US_UID = "uid";
    public static final String US_FIRSTNAME = "firstname";
    public static final String US_LASTNAME = "lastname";
    public static final String US_USERNAME = "topisimo";
    public static final String US_EMAIL = "email";
    public static final String US_ENCRYPTED_PASSWORD = "encrypted_password";
    public static final String US_SALT = "salt";
    public static final String US_CREATED_AT = "created_at";
    public static final String US_ACTIVATED = "activated";
    public static final String US_ACTIVATE_KEY = "activate_key";
    public static final String US_GROUPSFOLLOWED = "groups_followed";

    //CAMPOS GRUPOS
    public static final String GP_ID = "_id";
    public static final String GP_NOMBRE = "nombre";
    public static final String GP_CREADOR = "creador";
    public static final String GP_IMG = "img";
    public static final String GP_THUMB = "thumb";

    //PERFIL DE USUARIO
    public static final int MAX_NICK_LENGHT = 15;
    public static final int JPEG_QUALITY_FOTO = 90;
    public static final int JPEG_QUALITY_STORED_IMAGES = 60;
    public static final int MAX_WIDTH_FOTO = 640;
    public static final int MAX_HEIGHT_FOTO = 480;
    public static final int RADIOPERCENT = 25;

    //NOTIFICACION
    public static final int MAX_WIDTH_NOTIFICACION = 75;
    public static final int MAX_HEIGHT_NOTIFICACION = 75;

    //CARTEL ACTUACION
    public static final int JPEG_QUALITY_CARTEL = 50;
    public static final int MAX_WIDTH_CARTEL = 640;
    public static final int MAX_HEIGHT_CARTEL = 480;

    //FECHA Y HORA
    public static final int DIA = 0;
    public static final int MES = 1;
    public static final int AÑO = 2;
    public static final int HOR = 0;
    public static final int MIN = 1;

    //MENSAJES
    public static final String MSG_ERROR_CONEXION = "Error de conexión. Revise su configuración de red. Active su red WIFI.";

    //Nombre de SharedPreferences
    public static final String SP_FILE = "yambu_shared_pref";
    public static final String SP_EMAIL_REG = "email_reg";

    public static final String SP_DISTANCE_UNITS = "distance_units";


    //CURRENCIES
    public static final ArrayList<Currency> currenciesList = new ArrayList<Currency>() {
        {
            add(new Currency("GBP", "£"));
            add(new Currency("USD", "$"));
            add(new Currency("EUR", "€"));
        }
    };

    //Root emails This users has all privileges
    public static final ArrayList<String> rootUsers = new ArrayList<String>() {
        {
            add("marcosedoatienza@gmail.com");
            add("topermaper@gmail.com");
        }
    };



    //Devuelve "" si no existe ninguna moneda o el simbolo de esa moneda como "$"
    public static String getCurrencySymbolByCode(String code) {
        Currency currency;
        for (int i = 0; i < currenciesList.size(); i++) {
            currency = currenciesList.get(i);
            if (currency.getCode().equals(code)) {
                return currency.getSymbol();
            }
        }
        return "";
    }

}
