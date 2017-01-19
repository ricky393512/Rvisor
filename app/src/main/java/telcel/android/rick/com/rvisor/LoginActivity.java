package telcel.android.rick.com.rvisor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

import telcel.android.rick.com.rvisor.exceptions.WebServiceConexionException;
import telcel.android.rick.com.rvisor.net.Conexion;
import telcel.android.rick.com.rvisor.pojo.RespuestaLogueo;
import telcel.android.rick.com.rvisor.telcel.android.rick.com.rvisor.session.SessionManager;

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
    private String distribuidor;
    private String vendedor;
    private Conexion conexion;

    private void updateAndroidSecurityProvider(Activity callingActivity) {
        try {
            Log.e("SecurityException", "Checando !!!!!!!!!!!!!e.");
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), callingActivity, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("SecurityException", "Google Play Services not available.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        updateAndroidSecurityProvider(this);
        conexion = new Conexion(this);
        // Session class instance
        session = new SessionManager(getApplicationContext());
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

       if(!conexion.estaConectado()){
           mostrarAlerta(LoginActivity.this, getString(R.string.error_titulo_conexion_nodisponible), getString(R.string.error_conexion_nodisponible), false);
                   return;
       }


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
        final int timeOut=70000;
        final String distribuidor;
        final String vendedor;
        RespuestaLogueo respuestaLogueo=null;
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
           updateAndroidSecurityProvider(LoginActivity.this);
           try {
               if (!conexion.isAvailableWSDL(URL)) {
                   Log.e("RVISOR MOBILE", "El WS " + URL + " no esta en linea ");
                   mensajeFinal= "Web Service: No Disponible";
                   return false;
               }
           }catch(Exception e){
               mensajeFinal= e.getMessage();
               return false;
           }
           // Create the outgoing message
            respuestaLogueo = new RespuestaLogueo();
            SoapObject requestObject = new SoapObject(NAMESPACE, METHOD_NAME);
            requestObject.addProperty("cod_distribuidor",distribuidor);
            requestObject.addProperty("cod_vendedor",vendedor);
            SoapSerializationEnvelope envelope = conexion.getSoapSerializationEnvelope(requestObject);
            HttpTransportSE ht = conexion.getHttpTransportSE(URL,timeOut);
            try{

                Object retObj = conexion.llamadaAlWS(envelope,ht,SOAP_ACTION);
                respuestaLogueo = conexion.obtenerCredencialesSoap((SoapObject)retObj);
            }catch (WebServiceConexionException e){
                e.printStackTrace();
                respuestaLogueo.setCodigo(-1);
                respuestaLogueo.setMensaje(e.getMessage());
            }catch(Exception e){
                e.printStackTrace();
                respuestaLogueo.setCodigo(-1);
                respuestaLogueo.setMensaje(e.getMessage());
            }

            if(respuestaLogueo.getCodigo()==100)
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
                if(respuestaLogueo.getMensaje().startsWith("Clave de distribuidor")){
                    mClaveDistribuidorView.setError(getString(R.string.error_distribuidor_incorrecto));
                    mClaveDistribuidorView.requestFocus();
                }else if (respuestaLogueo.getMensaje().startsWith("Clave de vendedor")) {
                    mClaveVendedorView.setError(getString(R.string.error_vendedor_incorrecto));
                    mClaveVendedorView.requestFocus();
                }else {
                    mostrarAlerta(LoginActivity.this, getString(R.string.error_ws_nodisponible),
                            respuestaLogueo.getMensaje(), false);
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




    protected void mostrarAlerta(Context context, String title, String message, Boolean status) {
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



    private boolean isDigitValid(String clave){
        return TextUtils.isDigitsOnly(clave);
    }


    private boolean isLongitudValid(String clave) {

        return clave.length() > 5;
    }


}

