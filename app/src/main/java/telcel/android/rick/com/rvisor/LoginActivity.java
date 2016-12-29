package telcel.android.rick.com.rvisor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import telcel.android.rick.com.rvisor.pojo.Credencial;
import telcel.android.rick.com.rvisor.telcel.android.rick.com.rvisor.session.SessionManager;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {



    // Session Manager Class
    private SessionManager session;



    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world", "28709:123"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsuarioCoppelView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

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
            Log.d("TAG1", "firstRun(false): " + Boolean.valueOf(firstRun).toString());
        } else {

            Log.d("TAG1", "firstRun(true): " + Boolean.valueOf(firstRun).toString());



            // Set up the login form.
            mUsuarioCoppelView = (AutoCompleteTextView) findViewById(R.id.distribuidor);

            mPasswordView = (EditText) findViewById(R.id.vendedor);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
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
        mUsuarioCoppelView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String usuario = mPasswordView.getText().toString();
        String password = mUsuarioCoppelView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(usuario)) {
            mUsuarioCoppelView.setError("La clave de vendedor no debe estar vacia");
            focusView = mUsuarioCoppelView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("La clave de distribuidor no debe estar vacia");
            focusView = mPasswordView;
            cancel = true;
        }


        if (!isDigitValid(usuario)) {
            mUsuarioCoppelView.setError("La clave de vendedor deben ser digitos");
            focusView = mUsuarioCoppelView;
            cancel = true;
        }


        if (!isDigitValid(password)) {
            mPasswordView.setError("La clave de distribuidor deben ser digitos");
            focusView = mPasswordView;
            cancel = true;
        }


        if (isLongitudValid(usuario)) {
            mUsuarioCoppelView.setError("La clave de vendedor deben ser menor a 5 digitos");
            focusView = mUsuarioCoppelView;
            cancel = true;
        }

        if (isLongitudValid(password)) {
            mPasswordView.setError("La clave de distribuidor deben ser menor a 5 digitos");
            focusView = mPasswordView;
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
            mAuthTask = new UserLoginTask(usuario, password);
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsuarioCoppelView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        final String NAMESPACE = "http://ws.telcel.com/";
        final String URL="http://www.r7.telcel.com/activaciones_mobile_ws/activacionMobileWS?wsdl";
        final String METHOD_NAME = "realiza_autenticacion";
        final String SOAP_ACTION = "http://ws.telcel.com/realiza_autenticacion";
        private final String distribuidor=null;
        private final String vendedor=null;


        UserLoginTask(String distribuidor, String vendedor) {
            distribuidor = distribuidor;
            vendedor = vendedor;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            /*
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            */

            if (!isAvailableWSDL(URL)) {
                System.out.println("NO esta arriba LOGin WSSSSSSSSSSSSSS");

                return false;
            }

            // Create the outgoing message
            SoapObject requestObject = new SoapObject(NAMESPACE, METHOD_NAME);

            // Set Parameter
            System.out.println("cod_distribuidor "+distribuidor);
            requestObject.addProperty("cod_distribuidor","1222");

            System.out.println("cod_vendedor "+vendedor);
            requestObject.addProperty("cod_vendedor","32323");



            // Create soap envelop .use version 1.1 of soap
            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);

            // add the outgoing object as the request
            envelope.setOutputSoapObject(requestObject);
            envelope.dotNet = true;
            //       envelope.addMapping(NAMESPACE, "Productividad", Productividad.class);
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
            //  SoapObject resSoap=(SoapObject)envelope.bodyIn;
          //  Log.e("Object response", response.toString());
           // SoapObject resSoap = (SoapObject) envelope.bodyIn;
            Object response =null;
            try {
                 response = envelope.getResponse();
                Log.e("Object response", response.toString());
            } catch (SoapFault soapFault) {
                soapFault.printStackTrace();
            }

          /*  if (resSoap != null) {

                SoapObject soapResult = (SoapObject)resSoap.getProperty(0);
                Log.i("TOTAL PROPIEDADES S: ",""+soapResult.getPropertyCount());
                SoapPrimitive codigo=    (SoapPrimitive) soapResult.getProperty(0);
                SoapPrimitive  mensaje=    (SoapPrimitive) soapResult.getProperty(1);

             //   codigoAct = codigo.toString();
             //   mensajeAct= mensaje.toString();

                 /*   for(int i=0;i<soapResult.getPropertyCount();i++)
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

                        Log.i("Resultado S: ",result);

                    }
                   */
//            }





            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                System.out.println("Entre y guardo");
                session.createLoginSession("Android Hive", "anroidhive@gmail.com");
                // Store values at the time of the login attempt.

                String vendedor = mPasswordView.getText().toString();
                String distribuidor = mUsuarioCoppelView.getText().toString();


                Credencial credencial = new Credencial();

                credencial.setClaveVendedor(vendedor);
                credencial.setClaveDistribuidor(distribuidor);

                Intent intent =               new Intent(getApplicationContext(),ConsultaActivity.class);
                intent.putExtra("credencial", credencial);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
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
        try {
            URL siteURL = new URL(url);
            c = (HttpURLConnection) siteURL
                    .openConnection();
            c.setRequestMethod("HEAD");
            c.setConnectTimeout(1000); //set timeout to 5 seconds
            c.setReadTimeout(1000);
            c.connect();

            return true;

        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) {
                c.disconnect();
                c = null;
            }
        }

    }


}

