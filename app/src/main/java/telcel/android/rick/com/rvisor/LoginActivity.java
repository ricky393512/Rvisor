package telcel.android.rick.com.rvisor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import telcel.android.rick.com.rvisor.net.Conexion;
import telcel.android.rick.com.rvisor.telcel.android.rick.com.rvisor.session.SessionManager;
import telcel.android.rick.com.rvisor.ws.HttpsTls12TransportSE;
import telcel.android.rick.com.rvisor.ws.NoSSLv3SocketFactory;

/**
 * A login screen that offers login via claveDistribuidor/claveVendedor
 */
public class LoginActivity extends AppCompatActivity{

    // Session Manager Class
    private SessionManager session;
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mClaveDistribuidorView;
    private EditText mClaveVendedorView;
    private View mLoginFormView;
    private String mensajeFinal;
    private String codigoeFinal;
    String distribuidor;
    String vendedor;
    private Conexion conexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Session class instance
        session = new SessionManager(getApplicationContext());
        conexion = new Conexion();

        boolean firstRun = session.isFirstRun();

        if (!firstRun) {
            Intent intent = new Intent(this, ConsultaActivity.class);
            startActivity(intent);
            Log.i("RVISOR MOBILE", "Primera vez que se ejecuta---firstRun(false): " + Boolean.valueOf(firstRun).toString());
        } else {
            Log.i("RVISOR MOBILE", "Segunda vez que se ejecuta---firstRun(true): " + Boolean.valueOf(firstRun).toString());

            mClaveDistribuidorView = (EditText) findViewById(R.id.distribuidor);
            mClaveVendedorView = (EditText) findViewById(R.id.vendedor);
            Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
            mSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
            mLoginFormView = findViewById(R.id.login_form);

        }
    }




    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
       if(!estaConectado())
           return;






        if (mAuthTask != null) {
            return;
        }

        mClaveDistribuidorView.setError(null);
        mClaveVendedorView.setError(null);

        distribuidor = mClaveDistribuidorView.getText().toString();
        vendedor = mClaveVendedorView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(distribuidor)) {

            mClaveDistribuidorView.setError(getString(R.string.error_distribuidor_vacio));
            focusView = mClaveDistribuidorView;
            cancel = true;
        }

        if (TextUtils.isEmpty(vendedor)) {
            mClaveVendedorView.setError(getString(R.string.error_vendedor_vacio));
            focusView = mClaveVendedorView;
            cancel = true;
        }


        if (!isDigitValid(distribuidor)) {
            mClaveDistribuidorView.setError(getString(R.string.error_distribuidor_solo_numeros));
            focusView = mClaveDistribuidorView;
            cancel = true;
        }


        if (!isDigitValid(vendedor)) {
            mClaveVendedorView.setError(getString(R.string.error_vendedor_solo_numeros));
            focusView = mClaveVendedorView;
            cancel = true;
        }


        if (isLongitudValid(distribuidor)) {
            mClaveDistribuidorView.setError(getString(R.string.error_distribuidor_corto));
            focusView = mClaveDistribuidorView;
            cancel = true;
        }

        if (isLongitudValid(vendedor)) {
            mClaveVendedorView.setError(getString(R.string.error_vendedor_corto));
            focusView = mClaveVendedorView;
            cancel = true;
        }



        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuthTask = new UserLoginTask(distribuidor, vendedor);
            mAuthTask.execute((Void) null);
        }
    }




    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        final String NAMESPACE = "http://ws.telcel.com/";
        final String URL="https://www.r7.telcel.com/wscadenas/wsActivaMobile?wsdl";
        final String METHOD_NAME = "realiza_autenticacion";
        final String SOAP_ACTION = "\"http://ws.telcel.com/realiza_autenticacion\"";
        final String distribuidor;
        final String vendedor;
        SoapPrimitive codigo=   null;
        SoapPrimitive  mensaje=    null;
        private ProgressDialog progreso;


        UserLoginTask(String distribuidor, String vendedor) {
            this.distribuidor = distribuidor;
            this.vendedor = vendedor;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = new ProgressDialog(LoginActivity.this);
            progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progreso.setMessage("Validando Crendenciales R7............");
            progreso.setCancelable(false);
            progreso.show();

        }

        @Override
        protected Boolean doInBackground(Void... params) {


            if (!isAvailableWSDL(URL)) {

                Log.e("RVISOR MOBILE", "El WS "+URL+" no esta en linea ");
                return false;
            }

            // Create the outgoing message
            SoapObject requestObject = new SoapObject(NAMESPACE, METHOD_NAME);
            System.out.println("cod_distribuidor "+distribuidor);
            System.out.println("cod_vendedor 11"+vendedor);
            requestObject.addProperty("cod_distribuidor",distribuidor);
            requestObject.addProperty("cod_vendedor",vendedor);


            // Create soap envelop .use version 1.1 of soap
            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);

            // add the outgoing object as the request
            envelope.setOutputSoapObject(requestObject);
            envelope.dotNet = false;
           // HttpTransportSE ht = new HttpTransportSE(URL);

            java.net.URL url= null;
            try {
                url = new URL(URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String host = url.getHost();
            int port = url.getPort();
            String file = url.getPath();

            if (port == -1) {
                port = 443;
            }

            Log.d("RVISORMOVILE", "host -> " + host);
            Log.d("RVISORMOVILE", "port -> " + port);
            Log.d("RVISORMOVILE", "file -> " + file);



//            HttpsTransportSE hts = new KeepAliveHttpsTransportSE()
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




            HttpsTls12TransportSE ht =new HttpsTls12TransportSE(host, port, file, 10000);

        //    ht.
            ht.debug = true;
            // call and Parse Result.

            try {
                ht.call(SOAP_ACTION, envelope);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            Log.i("RVISOR MOBILE", "La cadena de envio del WS!! es la siguiente: "+ht.requestDump);
            String theXmlString = ht.responseDump;
            Log.i("RVISOR MOBILE", "La respuesta del WS es la siguiente: "+theXmlString);
            SoapObject soap = (SoapObject) envelope.bodyIn;
            SoapObject soapResult = (SoapObject)soap.getProperty(0);
            Log.i("RVISOR MOBILE","TOTAL PROPIEDADES S: "+soapResult.getPropertyCount());
            if (soapResult != null) {
                SoapObject soapResult1 = (SoapObject)soap.getProperty(0);
                Log.i("RVISOR MOBILE","Propiedad ---"+soapResult1.getPropertyCount());
                codigo=    (SoapPrimitive) soapResult1.getProperty(0);
                mensaje=    (SoapPrimitive) soapResult1.getProperty(1);
                mensajeFinal=mensaje.toString();
                codigoeFinal=codigo.toString();
                Log.i("RVISOR MOBILE","codigo respuesta WS "+codigo.toString());
                Log.i("RVISOR MOBILE","mensaje respuesta WS "+mensaje.toString());
            }

            if(codigo.toString().equals("100"))
                return true;
            else
                return false;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            progreso.dismiss();
            // showProgress(false);
            if (success) {
                Log.i("RVISOR MOBILE", "Entro a  guardar la session con el distribuidor: "+distribuidor+" y vendedor "+vendedor);
                session.createLoginSession(distribuidor, vendedor);
                // Store values at the time of the login attempt.
                Intent intent =               new Intent(getApplicationContext(),ConsultaActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                if(mensajeFinal.toString().startsWith("Clave de distribuidor")){

                    mClaveDistribuidorView.setError(getString(R.string.error_distribuidor_incorrecto));
                    mClaveDistribuidorView.requestFocus();
                }else if (mensajeFinal.toString().startsWith("Clave de vendedor")) {
                    mClaveVendedorView.setError(getString(R.string.error_vendedor_incorrecto));
                    mClaveVendedorView.requestFocus();

                }else if (mensajeFinal.toString().startsWith("WebService")){
                    showAlertDialog(LoginActivity.this, getString(R.string.error_ws_nodisponible),
                            mensajeFinal, false);
                }


            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progreso.dismiss();
            // showProgress(false);
        }
    }



    protected Boolean estaConectado(){
        if(conectadoWifi()){
            return true;
        }else{
            if(conectadoRedMovil()){
                return true;
            }else{
                showAlertDialog(LoginActivity.this, getString(R.string.error_titulo_conexion_nodisponible),
                        getString(R.string.error_conexion_nodisponible), false);
                return false;
            }
        }
    }



    protected Boolean conectadoWifi(){
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

    protected Boolean conectadoRedMovil(){
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


    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this,R.style.myDialog);
        alert.setTitle(title);
        alert.setMessage(message+ "\n"
        );
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                startActivity(getIntent());
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }


    public boolean isAvailableWSDL(String url) {
        Integer httpStatusCode=0;
        try {
            httpStatusCode= conexion.isAvailableWSDLCode(url);
            Log.d("RVISOR MOBILE", "El WS me responde un codigo "+httpStatusCode);
            if(httpStatusCode==200)
                return true;
            else
               return false;

        } catch (Exception e) {
            Log.e("RVISOR MOBILE", "No levanta el WS por "+e.getMessage());
            mensajeFinal="WebService: "+e.getMessage();
            codigoeFinal=httpStatusCode.toString();
            return false;
        }

    }

    private boolean isDigitValid(String clave){
        return TextUtils.isDigitsOnly(clave);
    }


    private boolean isLongitudValid(String clave) {

        return clave.length() > 5;
    }


}

