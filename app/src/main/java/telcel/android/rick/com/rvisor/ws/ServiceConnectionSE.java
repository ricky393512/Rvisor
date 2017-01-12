package telcel.android.rick.com.rvisor.ws;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by PIN7025 on 06/01/2017.
 */
public class ServiceConnectionSE {

    private TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };


    public ServiceConnectionSE(String url) throws IOException {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.getMessage();
        }
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        ((HttpsURLConnection) connection).setHostnameVerifier(new AllowAllHostnameVerifier());
    }





    public ServiceConnectionSE(Proxy proxy, String url) throws IOException {
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.getMessage();
        }
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        ((HttpsURLConnection) connection).setHostnameVerifier(new AllowAllHostnameVerifier());

        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
    }

}
