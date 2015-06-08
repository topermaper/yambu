package com.marcosedo.yambu.app;


import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class Utilidades {

    private Options generalOptions;

    public static File createTemporaryFile(String part, String ext,
                                           Context myContext) throws Exception {
        String path = myContext.getExternalCacheDir().getAbsolutePath()
                + "/temp/";
        File tempDir = new File(path);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    public static int nearest2pow(int value) {
        return value == 0 ? 0
                : (32 - Integer.numberOfLeadingZeros(value - 1)) / 2;
    }

    public static Bitmap redimensionarBitmap(Bitmap mBitmap, float newWidth, float newHeigth) {
        //Redimensionamos
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeigth) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);
    }

    // float radio = radio de las esquinas redondas
    public static Bitmap redondearBitmap(Bitmap bitmap, float radiopercent) {


        int width = 0;
        int height = 0;


        if (bitmap.getWidth() < bitmap.getHeight()) {
            width = bitmap.getWidth();
            height = bitmap.getWidth();
        } else {
            width = bitmap.getHeight();
            height = bitmap.getHeight();
        }

        float radio = radiopercent * width / 100;

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        final float roundPx = radio;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static String getRealPathFromURI(ContentResolver contentresolver, Uri contentURI) {
        String realpath;
        Cursor cursor = contentresolver.query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            realpath = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            realpath = cursor.getString(idx);
            cursor.close();
        }
        return realpath;
    }

    //Crea un fichero en formato jpg a partir de un bitmap
    public static void creaJPEG(Bitmap bitmap, String path, int width, int height, int calidad) {

        //si width o height es 0 no se escala la foto y se queda con la apariencia actual
        if ((width > 0) && (height > 0)) {
            bitmap = redimensionarBitmap(bitmap, width, height);
        }

        File file = new File(path);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, calidad, fos);
            fos.flush();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static int[] getScreenResolution(Context context) {
        int[] screenResolution = new int[2];
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenResolution[0] = metrics.widthPixels;
        screenResolution[1] = metrics.heightPixels;
        return screenResolution;
    }
    /////////////////////////////////////////////////////

    public static void storeBitmap(Bitmap bitmap, String storageDir, String fileName, int width, int height) {

        File storageDirFile = new File(storageDir);

        try {
            if (!storageDirFile.exists()) {
                storageDirFile.mkdir();
            }
            File newFile = new File(storageDir + fileName);
            if (!newFile.exists()) { // if file doesnt exists, then create it
                newFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(newFile);

            //si width o height es 0 no se escala la foto y se queda con la apariencia actual
            if ((width > 0) && (height > 0)) {
                bitmap = redimensionarBitmap(bitmap, width, height);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, Constantes.JPEG_QUALITY_STORED_IMAGES, fos);
            fos.flush();

        } catch (IOException exception) {
            System.err.println("Problems with files!");
            exception.printStackTrace();
        }
    }



    public static int[] calculaDimensionImagen(int oldWidth, int oldHeight, int maxWidth, int maxHeight) {
        //Si superamos alguno de los tamaños maximos ancho o alto, la foto se hara mas pequeña.
        //La foto debe mantener el ratio de aspecto pero respetando las nuevas dimensiones
        int[] dimensiones = new int[2];
        Log.i("AJUSTANDO FOTO", "Entra");

        float newWidth = 0;
        float newHeight = 0;
        float ratio = (float)oldWidth / (float)oldHeight;

        Log.i("AJUSTANDO FOTO", "maxwidth= " + maxWidth + "  maxHeight = " + maxHeight);

        if ((oldWidth <= maxWidth) && (oldHeight > maxHeight)) {
            newHeight = maxHeight;
            newWidth = maxHeight * ratio;
            Log.i("AJUSTANDO FOTO", "CASO 1");
        }
        if ((oldHeight <= maxHeight) && (oldWidth > maxWidth)) {
            newHeight = maxWidth * ratio;
            newWidth = maxWidth;
            Log.i("AJUSTANDO FOTO", "CASO 2");
        }
        if ((oldHeight > maxHeight) && (oldWidth > maxWidth)) {
            if ((maxHeight * ratio) > maxWidth) {
                newWidth = maxWidth;
                newHeight = newWidth / ratio;
                Log.i("AJUSTANDO FOTO", "CASO 3");
            } else {
                newHeight = maxHeight;
                newWidth = newHeight * ratio;
                Log.i("AJUSTANDO FOTO", "CASO 4");
            }
        }
        if ((oldHeight < maxWidth) && (oldWidth < maxWidth)) {
            Log.i("AJUSTANDO FOTO", "CASO 5:Hay que ampliar");
            if ((maxHeight * ratio) > maxWidth) {
                newWidth = maxWidth;
                newHeight = newWidth / ratio;
            } else {
                newHeight = maxHeight;
                newWidth = newHeight * ratio;
            }
        }

        Log.i("AJUSTANDO FOTO", "oldHeight= " + oldHeight + "  oldWidth = " + oldWidth
                + "  newHeight= " + newHeight + "  newWidth = " + newWidth + "  ratio = " + ratio);

        dimensiones[0] = (int) newWidth;
        dimensiones[1] = (int) newHeight;

        return dimensiones;
    }

    public static byte[] getByteArrayFromDrawableId(Context context, int id) {

        Resources res = context.getResources();
        Drawable drawable = res.getDrawable(id);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitMapData = stream.toByteArray();

        return bitMapData;
    }

    public static int convertDipTdoPixel(Context context, float dip) {
        return (int) (dip * context.getResources().getDisplayMetrics().density);
    }

    public static String getDevId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    /*public static void PrintFragmentList(FragmentActivity context) {
        List<Fragment> listfragment = context.getSupportFragmentManager().getFragments();
        Log.i("LISTA FRAGMENTS", "Esto son todos nuestros fragments");

        String tag;
        for (int i = 0; i < listfragment.size(); i++) {
            Fragment fragment = listfragment.get(i);
            if (fragment.getTag() == null) tag = "null"; else tag = fragment.getTag();
            Log.i("Fragment " + Integer.toString(i), " tag=" + tag);
        }
    }*/

    public Bitmap getImage(Context context,Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, options);
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.generalOptions = options;
        return scaleImage(context,options, uri, 300);
    }

    public Bitmap scaleImage(Context context, BitmapFactory.Options options, Uri uri,
                             int targetWidth) {
        if (options == null)
            options = generalOptions;
        Bitmap bitmap = null;
        double ratioWidth = ((float) targetWidth) / (float) options.outWidth;
        double ratioHeight = ((float) targetWidth) / (float) options.outHeight;
        double ratio = Math.min(ratioWidth, ratioHeight);
        int dstWidth = (int) Math.round(ratio * options.outWidth);
        int dstHeight = (int) Math.round(ratio * options.outHeight);
        ratio = Math.floor(1.0 / ratio);
        int sample = nearest2pow((int) ratio);

        options.inJustDecodeBounds = false;
        if (sample <= 0) {
            sample = 1;
        }
        options.inSampleSize = (int) sample;
        options.inPurgeable = true;
        try {
            InputStream is;
            is = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is, null, options);
            if (sample > 1)
                bitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight,
                        true);
            is.close();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmap;
    }


    public static String getTimeZoneOffset(Context context) {//devolveria esto "+1:00" , +0:00 ,+3:30
        String signo;
        TimeZone timezone = TimeZone.getDefault();


        int timeZoneOffset_ms = timezone.getRawOffset()+timezone.getDSTSavings();//en milisegundos sumamos desplazamiento de uso horario + la hora de verano si procede
        int timeZoneOffset_h = timeZoneOffset_ms/ (60 * 60 * 1000);//horas
        int timeZoneOffset_m = Math.abs( (timeZoneOffset_ms-timeZoneOffset_h*3600000) / 60000 );//minutos algunos sitios tienen minutos "30"

        if (timeZoneOffset_ms < 0) signo="-";
        else signo ="+";
        NumberFormat f = new DecimalFormat("00");

        return signo+String.valueOf(timeZoneOffset_h) + ":" +f.format(timeZoneOffset_m);
    }

    public static float kmToMiles(float km) {
        float constant = 0.621371192237334f;
        float miles = constant * km;
        return miles;
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}