package telcel.android.rick.com.rvisor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import telcel.android.rick.com.rvisor.pojo.Credencial;
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
    private View mProgressView;
    private View mLoginFormView;
    private String mensajeFinal;
    private String codigoeFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Session class instance
        session = new SessionManager(getApplicationContext());
        boolean firstRun = session.isFirstRun();

        if (!firstRun) {
            Intent intent = new Intent(this, ConsultaActivity.class);
            startActivity(intent);
            Log.d("APPLICATIONMOBILE", "firstRun(false): " + Boolean.valueOf(firstRun).toString());
        } else {

            Log.d("APPLICATIONMOBILE", "firstRun(true): " + Boolean.valueOf(firstRun).toString());
            // Set up the login form.
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
            mProgressView = findViewById(R.id.login_progress);

        }
    }




    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        estaConectado();
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mClaveDistribuidorView.setError(null);
        mClaveVendedorView.setError(null);

        // Store values at the time of the login attempt.
        String distribuidor = mClaveDistribuidorView.getText().toString();
        String vendedor = mClaveVendedorView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(distribuidor)) {
            mClaveDistribuidorView.setError("La clave de distribuidor no debe estar vacia");
            focusView = mClaveDistribuidorView;
            cancel = true;
        }

        if (TextUtils.isEmpty(vendedor)) {
            mClaveVendedorView.setError("La clave de vendedor no debe estar vacia");
            focusView = mClaveVendedorView;
            cancel = true;
        }


        if (!isDigitValid(distribuidor)) {
            mClaveDistribuidorView.setError("La clave de distribuidor deben ser digitos");
            focusView = mClaveDistribuidorView;
            cancel = true;
        }


        if (!isDigitValid(vendedor)) {
            mClaveVendedorView.setError("La clave de vendedor deben ser digitos");
            focusView = mClaveVendedorView;
            cancel = true;
        }


        if (isLongitudValid(distribuidor)) {
            mClaveDistribuidorView.setError("La clave de distribuidor deben ser menor a 5 digitos");
            focusView = mClaveDistribuidorView;
            cancel = true;
        }

        if (isLongitudValid(vendedor)) {
            mClaveVendedorView.setError("La clave de vendedor deben ser menor a 5 digitos");
            focusView = mClaveVendedorView;
            cancel = true;
        }



        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(distribuidor, vendedor);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isDigitValid(String clave){
        return TextUtils.isDigitsOnly(clave);
    }


    private boolean isLongitudValid(String clave) {
        //TODO: Replace this with your own logic
        return clave.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

        UserLoginTask(String distribuidor, String vendedor) {
            this.distribuidor = distribuidor;
            this.vendedor = vendedor;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            if (!isAvailableWSDL(URL)) {
                System.out.println("NO esta arriba LOGin WSSSSSSSSSSSSSS");

                return false;
            }

            // Create the outgoing message
            SoapObject requestObject = new SoapObject(NAMESPACE, METHOD_NAME);
            System.out.println("cod_distribuidor "+distribuidor);
            System.out.println("cod_vendedor 11"+vendedor);
            //   requestObject.addProperty(propInfo2);

            requestObject.addProperty("cod_distribuidor",distribuidor);
            requestObject.addProperty("cod_vendedor",vendedor);


            // Create soap envelop .use version 1.1 of soap
            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);

            // add the outgoing object as the request
            envelope.setOutputSoapObject(requestObject);
            envelope.dotNet = false;
            HttpTransportSE ht = new HttpTransportSE(URL);
            ht.debug = true;
            // call and Parse Result.
            try {
                ht.call(SOAP_ACTION, envelope);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            String theXmlString = ht.responseDump;
            Log.i("Resultado T: ",theXmlString);
            SoapObject soap = (SoapObject) envelope.bodyIn;
            SoapObject soapResult = (SoapObject)soap.getProperty(0);
            Log.i("TOTAL PROPIEDADES S: ",""+soapResult.getPropertyCount());
/*            for(int i=0;i<soapResult.getPropertyCount();i++)
            {
                String result = null;
                SoapPrimitive so =null;
                try {
                    so=    (SoapPrimitive) soapResult.getProperty(i);

                    result = so.toString();
                }catch(java.lang.ClassCastException e){
                    Log.i("Error falta un campo: ",e.getMessage());
                    continue;
                }
                //String result1 = so.getProperty(1).toString();
                //here, you can get data from xml using so.getProperty("PublicationID")
                //or the other tag in xml file.
                // String result = (String)so.getProperty("apellidos");
                Log.i("Resultado S: ",result);
                //Log.i("Resultado S1: ",result1);
            }
*/
           if (soapResult != null) {

                SoapObject soapResult1 = (SoapObject)soap.getProperty(0);
                Log.i("TOTAL PROPIEDADES S: ",""+soapResult1.getPropertyCount());
                 codigo=    (SoapPrimitive) soapResult1.getProperty(0);
                  mensaje=    (SoapPrimitive) soapResult1.getProperty(1);

               mensajeFinal=mensaje.toString();
               codigoeFinal=codigo.toString();
                Log.i("codigo ",codigo.toString());
                Log.i("mensaje ",mensaje.toString());


            }




           // Log.i("Resultado S: ","proper"+soap.getPropertyCount());



            if(codigo.toString().equals("100"))
                return true;
            else
                return false;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                System.out.println("Entre y guardo");
                session.createLoginSession(distribuidor, vendedor);
                // Store values at the time of the login attempt.
                Credencial credencial = new Credencial();
                credencial.setClaveVendedor(vendedor);
                credencial.setClaveDistribuidor(distribuidor);
                Intent intent =               new Intent(getApplicationContext(),ConsultaActivity.class);
            //    intent.putExtra("credencial", credencial);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                        if(mensajeFinal.toString().startsWith("Clave de distribuidor")){
                          //  mClaveDistribuidorView.setError(getString(R.string.error_incorrect_password));
                            mClaveDistribuidorView.setError("Clave de Distribuidor incorrecta");
                            mClaveDistribuidorView.requestFocus();
                   }else if (mensajeFinal.toString().startsWith("Clave de vendedor")) {
                            mClaveVendedorView.setError("Clave de Vendedor incorrecta");
                            mClaveVendedorView.requestFocus();

                        }else if (mensajeFinal.toString().startsWith("WebService NO DISPONIBLE:")){
                            showAlertDialog(LoginActivity.this, "Problemas WS",
                                    mensajeFinal, false);
                        }


            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }



    protected Boolean estaConectado(){
        if(conectadoWifi()){
            //  showAlertDialog(LoginActivity.this, "Conexion a Internet",
            //      "Tu Dispositivo tiene Conexion a Wifi.", true);
            return true;
        }else{
            if(conectadoRedMovil()){
                //    showAlertDialog(LoginActivity.this, "Conexion a Internet",
                //        "Tu Dispositivo tiene Conexion Movil.", true);
                return true;
            }else{
                showAlertDialog(LoginActivity.this, "Conexion a Internet",
                        "Tu Dispositivo no tiene Conexion a Internet.", false);
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
        HttpURLConnection c = null;
        Integer httpStatusCode=0;
        try {
            URL siteURL = new URL(url);
            c = (HttpURLConnection) siteURL
                    .openConnection();
            c.setRequestMethod("HEAD");
            c.setConnectTimeout(1000); //set timeout to 5 seconds
            c.setReadTimeout(1000);
            c.connect();
            httpStatusCode = c.getResponseCode(); //200, 404 etc.
            System.out.println("Arriba !!!!!!!!!!"+httpStatusCode);
            if(httpStatusCode==200)
            return true;
            else {
                mensajeFinal="WebService NO DISPONIBLE: "+codigoeFinal;
                codigoeFinal=httpStatusCode.toString();
                return false;

            }

        } catch (Exception e) {
            System.out.println("No levante "+e.getMessage());
            mensajeFinal="WebService NO DISPONIBLE: "+e.getMessage();
            codigoeFinal=httpStatusCode.toString();
            return false;
        } finally {
            if (c != null) {
                c.disconnect();
                c = null;
            }
        }

    }


}

