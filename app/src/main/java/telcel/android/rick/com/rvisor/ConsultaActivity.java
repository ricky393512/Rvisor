package telcel.android.rick.com.rvisor;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import telcel.android.rick.com.rvisor.pojo.Activacion;
import telcel.android.rick.com.rvisor.pojo.Credencial;
import telcel.android.rick.com.rvisor.pojo.TipoProducto;
import telcel.android.rick.com.rvisor.telcel.android.rick.com.rvisor.session.SessionManager;
import telcel.android.rick.com.rvisor.ws.CoopelWS;

public class ConsultaActivity extends AppCompatActivity {

    EditText campo_imei;
    EditText campo_iccid;
    EditText campo_codigo_ciudad;
    TextView txtClaveDistribuidor, txtClaveVendedor, txtResultado;
    CoopelWS coopelWS = null;
    // Session Manager Class
    SessionManager session;
    private NotificationManager notifyMgr;
    final String URL = "http://10.131.5.40:8080/HelloWorldWS/hello?wsdl";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
       /* case R.id.add:
            //add the function to perform here
            return(true);
        case R.id.reset:
            //add the function to perform here
            return(true);
        case R.id.about:
            //add the function to perform here
            return(true);
        */
            case R.id.exit:
                salir();
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    public void onBackPressed() {
     /*   DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

*/
        moveTaskToBack(true);

    }

    public void  realizaActivacion(){
        TipoProducto tipoProducto = (TipoProducto) ((Spinner) findViewById(R.id.my_spinner)).getSelectedItem();
        final Activacion activacion = new Activacion();
        activacion.setImei(campo_imei.getText().toString());
        activacion.setIccid(campo_iccid.getText().toString());
        activacion.setCodigoCiudad(Integer.parseInt(campo_codigo_ciudad.getText().toString()));
        activacion.setProducto(tipoProducto.getId());


        new AsyncTask<Void, Void, Boolean>() {
            private TextView txtResultado;
            private List<TipoProducto> listaProductos;
            private Spinner mySpinner;
            final String NAMESPACE = "http://hello_webservice/";
            final String URL="http://10.131.5.40:8080/HelloWorldWS/hello?wsdl";
            final String METHOD_NAME = "activaTelefono";
            final String SOAP_ACTION = "http://hello_webservice/WSConsultaLdap/activaTelefono";




            @Override
            protected void onPreExecute() {
                super.onPreExecute();



            }

            @Override
            protected Boolean doInBackground(Void... params) {
// TODO: attempt authentication against a network service.
//WebService - Opciones




                listaProductos = new ArrayList<TipoProducto>();

                if (!isAvailableWSDL(URL)) {
                    System.out.println("NO esta arriba");

                    return false;
                }

                // Create the outgoing message
                SoapObject requestObject = new SoapObject(NAMESPACE, METHOD_NAME);

                // Set Parameter
                System.out.println(" im eie "+activacion.getImei());
                    requestObject.addProperty("arg0", activacion);


                // Create soap envelop .use version 1.1 of soap
                SoapSerializationEnvelope envelope =
                        new SoapSerializationEnvelope(SoapEnvelope.VER11);

                // add the outgoing object as the request
                envelope.setOutputSoapObject(requestObject);
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

                SoapObject resSoap = (SoapObject) envelope.bodyIn;

                List<SoapObject> result = new ArrayList<SoapObject>();
                if (resSoap != null) {
                    SoapObject list = ((SoapObject) resSoap);

                    for(int i=0; i<list.getPropertyCount(); i++) {
                        SoapObject item = (SoapObject)list.getProperty(i);
                        result.add(item);
                    }
                }





                return true;
            }


            @Override
            protected void onPostExecute(final Boolean success) {
                if(success==false){
                    Toast.makeText(ConsultaActivity.this, "Usuario No Valido", Toast.LENGTH_LONG).show();


                }
                else{
                    Toast.makeText(ConsultaActivity.this, "Acceso Concedido: ", Toast.LENGTH_LONG).show();
                }


            }

            @Override
            protected void onCancelled() {
                Toast.makeText(ConsultaActivity.this, "Error", Toast.LENGTH_LONG).show();
            }





        }.execute();



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);
        session = new SessionManager(getApplicationContext());
        session.firstRun();
        Button btnIccid = (Button) findViewById(R.id.btnIccid);
        Button btnImei = (Button) findViewById(R.id.btnImei);
        Button btnActivar = (Button) findViewById(R.id.boton_aceptar);
        Button btnNueva = (Button) findViewById(R.id.btnNueva);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /*KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();

        lock.disableKeyguard();

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);

         lock.reenableKeyguard();
*/
        campo_imei = (EditText) findViewById(R.id.campo_imei);
        campo_iccid = (EditText) findViewById(R.id.campo_iccid);
        campo_codigo_ciudad = (EditText) findViewById(R.id.campo_ciudad);
        txtResultado = (TextView) findViewById(R.id.txtResultado);

        try {

            Credencial credencial = (Credencial) getIntent().getExtras().getSerializable("credencial");


            txtClaveVendedor = (TextView) findViewById(R.id.txtClaveVendedor);
            txtClaveDistribuidor = (TextView) findViewById(R.id.txtClaveDistribuidor);


            txtClaveVendedor.setText(credencial.getClaveVendedor());
            txtClaveDistribuidor.setText(credencial.getClaveDistribuidor());

        } catch (NullPointerException e) {

        }


        btnNueva.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                limpiaPantalla();
            }
        });


        btnActivar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // And to get the actual User object that was selected, you can do this.
                TipoProducto tipoProducto = (TipoProducto) ((Spinner) findViewById(R.id.my_spinner)).getSelectedItem();
                notification4(1, R.drawable.ic_telefono, "Aviso", " 2222222222");
            //    txtResultado.setText("El numero es 222222222   ---" + tipoProducto.getNombre());
                realizaActivacion();



            }
        });

        btnImei.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ScanEAN();
            }
        });


        btnIccid.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ScanIccid();
            }
        });


        // you can use this array to find the school ID based on name
        ArrayList<TipoProducto> productos = new ArrayList<TipoProducto>();
        // you can use this array to find the school ID based on name
        ArrayList<String> nombreProductos = new ArrayList<String>();

        nombreProductos.add(0, "Productyo 1");
        nombreProductos.add(0, "Productyo 2");
        nombreProductos.add(0, "Productyo 3");



        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
             //   add = new UsuarioDAO().insert(usuario, fotoPerfil);
             boolean valor = isAvailableWSDL(URL);
                return valor ;
            }

            @Override
            protected void onPostExecute(Boolean result) {
               // progress.dismiss();
                if(!result){
                    AlertDialog.Builder alert = new AlertDialog.Builder(ConsultaActivity.this,R.style.myDialog);
                    alert.setTitle("Atención");
                    alert.setMessage("El WS de catalogos no esta disponible \n"
                            + "Favor de comunicarse con el area comercial\n");
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(getApplicationContext(), ConsultaActivity.class));
                        }
                    });
                    AlertDialog dialog = alert.create();
                    dialog.show();
                }else{

                }
            }
        }.execute("");


        Spinner mySpinner = (Spinner) findViewById(R.id.my_spinner);
        //  mySpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, nombreProductos));
        coopelWS = new CoopelWS(getApplicationContext(), mySpinner);
        coopelWS.execute();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void ScanEAN() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "PDF_417");

        this.startActivityForResult(intent, 1);
    }


    private void ScanIccid() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "PDF_417");

        this.startActivityForResult(intent, 2);
    }


    private void limpiaPantalla() {

        campo_imei.setText(null);
        campo_iccid.setText(null);
        campo_codigo_ciudad.setText(null);
        txtResultado.setText(null);

    }


    private void salir() {
        session.logoutUser();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1)
                campo_imei.setText(data.getStringExtra("SCAN_RESULT"));
            else if (requestCode == 2)
                campo_iccid.setText(data.getStringExtra("SCAN_RESULT"));
            // En asyncTask
            // ISBN.callISBNService();
         /*   if (pref.getBoolean("checkServer", true)) { // Sensores
                ISBNTask tarea = new ISBNTask();
                tarea.execute(0);
            }*/
        }
    }


    public void notification4(int id, int iconId, String titulo, String contenido) {
        notifyMgr = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);


        // Estructurar la notificación
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(iconId).setLargeIcon(
                        BitmapFactory.decodeResource(
                                getResources(),
                                R.drawable.ic_telefono

                        ))

                        .setContentTitle(titulo)
                        .setContentText(contenido).setNumber(2)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        ;

        // Crear intent
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Crear pending intent
     /*   PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Asignar intent y establecer true para notificar como aviso
        builder.setFullScreenIntent(fullScreenPendingIntent, true);
*/

// API 11 o mayor
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        builder.setLights(Color.YELLOW, 300, 100);

        // Construir la notificación y emitirla
        notifyMgr.notify(id, builder.build());
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Consulta Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://telcel.android.rick.com.rvisor/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Consulta Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://telcel.android.rick.com.rvisor/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
