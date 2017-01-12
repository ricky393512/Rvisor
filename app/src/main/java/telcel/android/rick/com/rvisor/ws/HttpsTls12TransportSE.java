package telcel.android.rick.com.rvisor.ws;

import org.ksoap2.transport.HttpsServiceConnectionSE;
import org.ksoap2.transport.HttpsServiceConnectionSEIgnoringConnectionClose;
import org.ksoap2.transport.HttpsTransportSE;
import org.ksoap2.transport.ServiceConnection;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by PIN7025 on 10/01/2017.
 */
public class HttpsTls12TransportSE extends HttpsTransportSE
{
    private String host;
    private int port;
    private String file;
    private int timeout;


    public HttpsTls12TransportSE(String host, int port, String file, int timeout) {
        super(host, port, file, timeout);
        this.host=host;
        this.port=port;
        this.file=file;
        this.timeout=timeout;

    }

    @Override
    public ServiceConnection getServiceConnection() throws IOException
    {
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContext.getInstance("TLSv1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            sslcontext.init(null,
                    null,
                    null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
       SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());


        ServiceConnection serviceConnection =
                new HttpsServiceConnectionSEIgnoringConnectionClose(host, port, file, timeout);
        serviceConnection.setRequestProperty("Connection", "keep-alive");

      //   SSLSocketFactory factory = new Tls12SocketFactory();
        //NoSSLv3Factory.
        ((HttpsServiceConnectionSE)serviceConnection).setSSLSocketFactory(NoSSLv3Factory);

        return serviceConnection;
    }

}