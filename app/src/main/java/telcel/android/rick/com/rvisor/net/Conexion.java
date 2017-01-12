package telcel.android.rick.com.rvisor.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by PIN7025 on 03/01/2017.
 */
public class Conexion  extends AppCompatActivity {

    public Boolean conectadoWifi(){
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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



}
