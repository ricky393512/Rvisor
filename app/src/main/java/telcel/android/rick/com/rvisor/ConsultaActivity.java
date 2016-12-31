package telcel.android.rick.com.rvisor;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import telcel.android.rick.com.rvisor.pojo.Activacion;
import telcel.android.rick.com.rvisor.pojo.Credencial;
import telcel.android.rick.com.rvisor.pojo.TipoProducto;
import telcel.android.rick.com.rvisor.telcel.android.rick.com.rvisor.session.SessionManager;
import telcel.android.rick.com.rvisor.ws.CoopelWS;

public class ConsultaActivity extends AppCompatActivity {
    View focusView = null;
    EditText campo_imei;
    EditText campo_iccid;
    EditText campo_codigo_ciudad;
    TextView txtClaveDistribuidor, txtClaveVendedor, txtResultado;
    CoopelWS coopelWS = null;
    // Session Manager Class
    SessionManager session;
    private NotificationManager notifyMgr;
    final String URL = "https://www.r7.telcel.com/wscadenas/wsActivaMobile?wsdl";
    Credencial credencial= new Credencial();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    String codigoAct;
    String mensajeAct;
    String montoAct;
    String telefonoAct;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

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


    public boolean validacionActivacion(){

        boolean cancel = false;
        String imei =campo_imei.getText().toString();
        String iccid =campo_iccid.getText().toString();

        if (TextUtils.isEmpty(campo_iccid.getText().toString())) {
            campo_iccid.setError("Debes ingresar un iccid");
            focusView = campo_iccid;
            cancel = true;
            return cancel;
        }

  /*      if(campo_iccid.getText().toString().length()>20 || campo_iccid.getText().toString().length()<18){
            campo_iccid.setError("Debes ingresar un iccid DE 18 ó 19 digitos");
            focusView = campo_iccid;
            cancel = true;
            return cancel;
        }
*/
        if (TextUtils.isEmpty(imei)) {
            campo_imei.setError("Debes ingresar un imei");
            focusView = campo_imei;
            cancel = true;
            return cancel;
        }

  /*      if(campo_imei.getText().toString().length()>15 || campo_imei.getText().toString().length()<15){
            campo_imei.setError("Debes ingresar un imei de 15 digitos");
            focusView = campo_imei;
            cancel = true;
            return cancel;
        }

*/

        if (TextUtils.isEmpty(campo_codigo_ciudad.getText().toString().trim())) {
            campo_codigo_ciudad.setError("Debes ingresar un codigo de ciudad");
            focusView = campo_codigo_ciudad;
            cancel = true;
            return cancel;
        }

  /*      if(campo_codigo_ciudad.getText().toString().length()>3 || campo_codigo_ciudad.getText().toString().length()<3){
            campo_codigo_ciudad.setError("Debes ingresar un codigo ciudad de 3 digitos");
            focusView = campo_codigo_ciudad;
            cancel = true;
            return cancel;
        }
*/
        return cancel;
    }



    public void  realizaActivacion(){




        final TipoProducto tipoProducto = (TipoProducto) ((Spinner) findViewById(R.id.my_spinner)).getSelectedItem();

        System.out.println("Realizadooooooooooooooooooo Validacobes"+tipoProducto.getDescripcion());



        if (validacionActivacion()) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {


            final Activacion activacion = new Activacion();
            activacion.setImei(campo_imei.getText().toString());
            activacion.setIccid(campo_iccid.getText().toString());

            try {
                activacion.setCodigoCiudad(Integer.parseInt(campo_codigo_ciudad.getText().toString()));
            } catch (NumberFormatException e) {
                campo_codigo_ciudad.setError("Debes ingresar un codigo de ciudad");
                campo_codigo_ciudad.requestFocus();
            }


            activacion.setIdProducto(tipoProducto.getIdProducto());
            activacion.setIdModalidad(tipoProducto.getIdModalidad());
            System.out.println();
            activacion.setCodigoDistribuidor(credencial.getClaveDistribuidor());
            activacion.setCodigoVendedor(credencial.getClaveVendedor());

            new AsyncTask<Void, Void, Boolean>() {
                private TextView txtResultado;
                private List<TipoProducto> listaProductos;
                private Spinner mySpinner;
                final String NAMESPACE = "http://ws.telcel.com/";
                final String URL = "https://www.r7.telcel.com/wscadenas/wsActivaMobile?wsdl";
                final String METHOD_NAME = "realiza_activacion";
                final String SOAP_ACTION = "\"http://ws.telcel.com/realiza_activacion\"";


                @Override
                protected void onPreExecute() {
                    super.onPreExecute();


                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    listaProductos = new ArrayList<TipoProducto>();
                    if (!isAvailableWSDL(URL)) {
                        System.out.println("NO esta arriba");
                        return false;
                    }
                    // Create the outgoing message
                    SoapObject requestObject = new SoapObject(NAMESPACE, METHOD_NAME);
                    // Set Parameter
                    System.out.println(" im eie " + activacion.getImei());
                    requestObject.addProperty("imei", activacion.getImei());
                    requestObject.addProperty("iccid", activacion.getIccid());
                    requestObject.addProperty("cod_ciudad", activacion.getCodigoCiudad());
                    requestObject.addProperty("cod_distribuidor", activacion.getCodigoDistribuidor());
                    requestObject.addProperty("cod_vendedor", activacion.getCodigoVendedor());
                    requestObject.addProperty("id_tipo_producto", tipoProducto.getIdProducto());
                    requestObject.addProperty("id_modalidad_activacion", activacion.getIdModalidad());


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


                    if (resSoap != null) {

                        SoapObject soapResult = (SoapObject) resSoap.getProperty(0);
                        Log.i("TOTAL PROPIEDADES S: ", "" + soapResult.getPropertyCount());
                        SoapPrimitive codigo = (SoapPrimitive) soapResult.getProperty(0);
                        SoapPrimitive mensaje = (SoapPrimitive) soapResult.getProperty(1);
                        SoapPrimitive monto = (SoapPrimitive) soapResult.getProperty(2);
                        SoapPrimitive telefono = (SoapPrimitive) soapResult.getProperty(3);

                        codigoAct = codigo.toString();
                        mensajeAct = mensaje.toString();
                        montoAct= monto.toString();
                        telefonoAct= telefono.toString();

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

                        if(codigoAct.equals("100"))
                            return true;
                        else
                            return false;

                    }

                    return true;
                }


                @Override
                protected void onPostExecute(final Boolean success) {
                    if (success == false) {
                        ///   Toast.makeText(ConsultaActivity.this, "Usuario No Valido", Toast.LENGTH_LONG).show();

                        AlertDialog.Builder alert = new AlertDialog.Builder(ConsultaActivity.this, R.style.myDialog);
                        alert.setTitle("Error !!!");
                        alert.setMessage("Se ha presentado el siguiente problema: \n"
                                + mensajeAct



                        );
                        alert.setPositiveButton("NUEVA ACTIVACION", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //   startActivity(new Intent(getApplicationContext(), ConsultaActivity.class));
                                finish();
                                startActivity(getIntent());
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.setCancelable(false);
                        dialog.show();





                    } else {
                        // Toast.makeText(ConsultaActivity.this, "Acceso Concedido: ", Toast.LENGTH_LONG).show();

                        AlertDialog.Builder alert = new AlertDialog.Builder(ConsultaActivity.this, R.style.myDialog);
                        alert.setTitle("Atención");
                        alert.setMessage("Se ha realizado la activacion correctamente \n"
                                + mensajeAct
                                +"\n"
                                +"Con telefono: "

                                +telefonoAct
                                + "\n"
                                +"Y monto: "

                                +montoAct
                                + "\n"





                        );
                        alert.setPositiveButton("NUEVA ACTIVACION", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //   startActivity(new Intent(getApplicationContext(), ConsultaActivity.class));
                                finish();
                                startActivity(getIntent());
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.setCancelable(false);
                        dialog.show();

                        notification4(1, R.drawable.ic_telefono, "Aviso de Activacion", "Se ha realizado la activacion correctamente \n"
                                + mensajeAct
                                +"\n"
                                +"Con telefono: "

                                +telefonoAct
                                + "\n"
                                +"Y monto: "

                                +montoAct
                                + "\n",telefonoAct,montoAct);
                    }


                }

                @Override
                protected void onCancelled() {
                    Toast.makeText(ConsultaActivity.this, "Error", Toast.LENGTH_LONG).show();
                }


            }.execute();

        }

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
        Button btnProducto = (Button) findViewById(R.id.btnProducto);

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

         //    credencial = (Credencial) getIntent().getExtras().getSerializable("credencial");

        HashMap<String,String> mapaCrendenciales= session.getUserDetails();

            credencial.setClaveDistribuidor(mapaCrendenciales.get("distribuidor"));
            credencial.setClaveVendedor(mapaCrendenciales.get("vendedor"));

            txtClaveVendedor = (TextView) findViewById(R.id.txtClaveVendedor);
            txtClaveDistribuidor = (TextView) findViewById(R.id.txtClaveDistribuidor);


            txtClaveVendedor.setText(credencial.getClaveVendedor());
            txtClaveDistribuidor.setText(credencial.getClaveDistribuidor());


        btnProducto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cargaCatalogoProductos();
            }
        });

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
                //TipoProducto tipoProducto = (TipoProducto) ((Spinner) findViewById(R.id.my_spinner)).getSelectedItem();
                realizaActivacion();


            //    txtResultado.setText("El numero es 222222222   ---" + tipoProducto.getNombre());



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



        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
             //   add = new UsuarioDAO().insert(usuario, fotoPerfil);
            // boolean valor = isAvailableWSDL(URL);


              //  return valor ;

                if (!isAvailableWSDL(URL)) {
                    System.out.println("NO esta arriba LOGin WSSSSSSSSSSSSSS");

                    return false;
                } else
                  return true;


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
                         //   startActivity(new Intent(getApplicationContext(), ConsultaActivity.class));
                            finish();
                            startActivity(getIntent());
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

    coopelWS = new CoopelWS(getApplicationContext(), mySpinner, credencial.getClaveDistribuidor());
    coopelWS.execute();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void cargaCatalogoProductos(){
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
                          //  startActivity(new Intent(getApplicationContext(), ConsultaActivity.class));
                            finish();
                            startActivity(getIntent());
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
        coopelWS = new CoopelWS(getApplicationContext(), mySpinner,credencial.getClaveDistribuidor());
        coopelWS.execute();

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


    public void notification4(int id, int iconId, String titulo, String contenido,String telefono,String monto) {
        notifyMgr = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);


        // Estructura  la notificación
/*
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(iconId).setLargeIcon(
                        BitmapFactory.decodeResource(
                                getResources(),
                                R.drawable.ic_telefono

                        ))

                        .setContentTitle(telefono)
                        .setContentText(monto)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        ;
*/

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        Bitmap bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_bien);

        builder
                .setContentTitle(titulo)
                .setContentText(contenido +" Telefono: "+telefono+" monto:"+monto)
                .setContentInfo("mas informacion de la activacion")
                .setSmallIcon(R.mipmap.ic_telefononube)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(bm)
        .setTicker("Activacion exitosa");

        ;

                //.setLargeIcon(bitmapIcon);

         new Notification.BigTextStyle(builder)
                .bigText(contenido +" Telefono: "+telefono+" monto:"+monto)
                .setBigContentTitle("Mensaje de Activacion")
                .setSummaryText("Resultado de Activacion")
                .build();


        // Crear intent
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


// API 11 o mayor
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        builder.setLights(Color.YELLOW, 300, 100);
      //  builder.setVibrate(new long[] {0,100,200,300});

        // Construir la notificación y emitirla
        notifyMgr.notify(id, builder.build());
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
                mensajeAct="WebService NO DISPONIBLE: "+codigoAct;
                codigoAct=httpStatusCode.toString();

                return false;

            }



        } catch (Exception e) {
            mensajeAct="WebService NO DISPONIBLE: "+codigoAct;
            codigoAct=httpStatusCode.toString();
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
