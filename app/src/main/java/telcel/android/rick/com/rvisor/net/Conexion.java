package telcel.android.rick.com.rvisor.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import telcel.android.rick.com.rvisor.exceptions.WebServiceConexionException;

/**
 * Created by PIN7025 on 03/01/2017.
 */
public class Conexion  {

    // Context
    Context context;
    // Constructor

    public Conexion(Context context){
        this.context = context;

    }

    public Boolean estaConectado(){
        if(conectadoWifi()){
            return true;
        }else{
            if(conectadoRedMovil()){
                return true;
            }else{

                return false;
            }
        }
    }


    public Boolean conectadoWifi(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean conectadoRedMovil(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }


    public int isAvailableWSDLCode(String url) throws IOException {
        HttpURLConnection c = null;
        Integer httpStatusCode=0;
        try {
            URL siteURL = new URL(url);
            c = (HttpURLConnection) siteURL
                    .openConnection();
            c.setRequestMethod("HEAD");
            c.setConnectTimeout(5000); //set timeout to 5 seconds
            c.setReadTimeout(5000);
            c.connect();
            httpStatusCode = c.getResponseCode(); //200, 404 etc.
            System.out.println("Codigo de respuesta!!!!!!!!!!"+httpStatusCode);
            return httpStatusCode;
        } catch (Exception e) {
            throw e;
        } finally {
            if (c != null) {
                c.disconnect();
                c = null;
            }
        }

    }

    public boolean isAvailableWSDL(String url) {
        Integer httpStatusCode=0;
        try {
            httpStatusCode= isAvailableWSDLCode(url);
            Log.d("RVISOR MOBILE", "El WS me responde un codigo "+httpStatusCode);
            if(httpStatusCode==200)
                return true;
            else
                return false;

        } catch (Exception e) {
            Log.e("RVISOR MOBILE", "No levanta el WS por "+e.getMessage());
            throw new WebServiceConexionException("El Web Service No Responde me manda el siguiente codigo: "+httpStatusCode.toString()+" con mensaje "+e.getMessage());
         //   mensajeFinal="WebService: "+e.getMessage();
           // codigoeFinal=httpStatusCode.toString();
          //  return false;
        }

    }

}
