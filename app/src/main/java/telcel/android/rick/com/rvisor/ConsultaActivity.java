package telcel.android.rick.com.rvisor;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import telcel.android.rick.com.rvisor.almacenamiento.Almacen;
import telcel.android.rick.com.rvisor.exceptions.WebServiceConexionException;
import telcel.android.rick.com.rvisor.net.Conexion;
import telcel.android.rick.com.rvisor.net.Constantes;
import telcel.android.rick.com.rvisor.net.Mensaje;
import telcel.android.rick.com.rvisor.pojo.Activacion;
import telcel.android.rick.com.rvisor.pojo.Credencial;
import telcel.android.rick.com.rvisor.pojo.RespuestaActivacion;
import telcel.android.rick.com.rvisor.pojo.TipoProducto;
import telcel.android.rick.com.rvisor.session.SessionManager;
import telcel.android.rick.com.rvisor.ws.CoopelWS;

public class ConsultaActivity extends AppCompatActivity {
    private String file_name_save;
    private Calendar fechaActual = Calendar.getInstance();
    private String FICHERO_DIR;
    private static final int SOLICITA_PERMISO_STORAGE_CODE = 1;
    private Almacen almacen;
    View focusView = null;
    EditText campo_imei;
    EditText campo_iccid;
    EditText campo_codigo_ciudad;
    TextView txtClaveDistribuidor;
    TextView txtClaveVendedor;
    TextView txtResultado;
    Spinner mySpinner;
    CoopelWS coopelWS = null;
    // Session Manager Class
    SessionManager session;
    private NotificationManager notifyMgr;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    Credencial credencial= new Credencial();
    private Conexion conexion;
    private  Mensaje mensaje;
    private BarcodeDetector detector;
    private Uri imageUri;
    private final String LOG_TAG="RVISOR";
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


    public void acercaDe(){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(ConsultaActivity.this);

        View child = getLayoutInflater().inflate(R.layout.midialogo, null);
        builder.setView(child);
        builder.setMessage("Version 1.0")
                .setTitle("Acerca de ...")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

         builder.create();
        builder.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                salir();
                return (true);
            case R.id.versionRvisor:
                acercaDe();
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }


    @Override
    public void onBackPressed() {
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


    public void confirmaAcciones(){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(ConsultaActivity.this);

        View child = getLayoutInflater().inflate(R.layout.midialogo, null);
        builder.setView(child);

        builder.setMessage("¿Estas seguro de mandar la activación?")
                .setTitle("Confirmacion")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()  {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("Dialogos", "Confirmacion Aceptada.");
                      dialog.dismiss();
                        realizaActivacion();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("Dialogos", "Confirmacion Cancelada.");
                        dialog.dismiss();
                    }
                });

         builder.create();
        builder.show();

    }



    public void  realizaActivacion(){
        final TipoProducto tipoProducto = (TipoProducto) ((Spinner) findViewById(R.id.my_spinner)).getSelectedItem();
        Log.i("RVISOR MOBILE","VAlor de producto seleccionado !!!"+tipoProducto.getIdProducto());
        if(tipoProducto.getIdProducto()==-1){
            Log.e("RVISOR MOBILE","VAlor -1 de producto seleccionado !!!");
            mensaje.getMostrarAlerta(ConsultaActivity.this,"Error !!!","Se ha presentado el siguiente problema con el WS de Catalogos \n"+
             "Favor de recargar el catalogo para que puedas elegir un producto valido","REGRESAR A LA ACTIVACION"
            );
            return;
        }


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

            activacion.setCodigoDistribuidor(credencial.getClaveDistribuidor());
            activacion.setCodigoVendedor(credencial.getClaveVendedor());

            new AsyncTask<Void, Void, Boolean>() {
                private TextView txtResultado;
                private List<TipoProducto> listaProductos;

                private ProgressDialog progreso;
                RespuestaActivacion respuestaActivacion = new RespuestaActivacion();


                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progreso = new ProgressDialog(ConsultaActivity.this);
                    progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progreso.setMessage("Activando ............");
                    progreso.setCancelable(false);
                    progreso.show();

                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    listaProductos = new ArrayList<TipoProducto>();
                    try {
                        if (!conexion.isAvailableWSDL(Constantes.URL)) {
                            Log.e("RVISOR MOBILE", "El WS " + Constantes.URL + " no esta en linea ");
                            return false;
                        }
                    }catch(Exception e){
                        return false;
                    }
                    // Create the outgoing message
                    SoapObject requestObject = new SoapObject(Constantes.NAMESPACE, Constantes.METHOD_NAME_ACTIVA);
                    // Set Parameter
                    requestObject.addProperty("imei", activacion.getImei());
                    requestObject.addProperty("iccid", activacion.getIccid());
                    requestObject.addProperty("cod_ciudad", activacion.getCodigoCiudad());
                    requestObject.addProperty("cod_distribuidor", activacion.getCodigoDistribuidor());
                    requestObject.addProperty("cod_vendedor", activacion.getCodigoVendedor());
                    requestObject.addProperty("id_tipo_producto", tipoProducto.getIdProducto());
                    requestObject.addProperty("id_modalidad_activacion", tipoProducto.getIdModalidad());



                    SoapSerializationEnvelope envelope =conexion.getSoapSerializationEnvelope(requestObject);
                    HttpTransportSE ht = conexion.getHttpTransportSE(Constantes.URL,Constantes.TIME_OUT);

                    try{

                        Object retObj = conexion.llamadaAlWS(envelope,ht,Constantes.SOAP_ACTION_ACTIVA);
                        respuestaActivacion = conexion.obtenerRespuestaActivaSoap((SoapObject)retObj);
                    }catch (WebServiceConexionException e){
                        e.printStackTrace();
                        respuestaActivacion.setCodigo(-1);
                        respuestaActivacion.setMensaje(e.getMessage());
                    }catch(Exception e){
                        e.printStackTrace();
                        respuestaActivacion.setCodigo(-1);
                        respuestaActivacion.setMensaje(e.getMessage());
                    }

                    if(respuestaActivacion.getCodigo()==100)
                            return true;
                      else
                            return false;
                }


                @Override
                protected void onPostExecute(final Boolean success) {

                    progreso.dismiss();

                    if (!success) {
                        String titulo="Error !!!";
                        String mensaje1="Se ha presentado el siguiente problema: \n"
                                + respuestaActivacion.getMensaje()+
                                " con codigo "+respuestaActivacion.getCodigo();
                        mensaje.getMostrarAlerta(ConsultaActivity.this, titulo,
                                mensaje1,"OK");



                        mensaje.getNotificationError(1, R.drawable.ic_telefono, "Error de Activacion", "Se ha presentado el siguiente problema con la activacion: \n"
                                + respuestaActivacion.getMensaje()
                                +"\n"
                                );



                    } else {

                        mostrarAlertaExito(ConsultaActivity.this,"Atención","Se ha realizado la activacion correctamente \n"
                                + respuestaActivacion.getMensaje()
                                +"\n"
                                +"Con telefono: "

                                +respuestaActivacion.getTelefono()
                                + "\n"
                                +"Y monto: "

                                +respuestaActivacion.getMonto()
                                + "\n","NUEVA ACTIVACION"
                          );
                        mensaje.getNotificationExito(1, R.drawable.ic_telefono, "Aviso de Activacion", "Se ha realizado la activacion correctamente \n"
                                + respuestaActivacion.getMensaje()
                                +"\n"
                                +"Con telefono: "

                                +respuestaActivacion.getTelefono()
                                + "\n"
                                +"Y monto: "

                                +respuestaActivacion.getMonto()
                                + "\n",respuestaActivacion.getTelefono(),respuestaActivacion.getMonto());
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(ConsultaActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }

            case SOLICITA_PERMISO_STORAGE_CODE:
                if (requestCode == SOLICITA_PERMISO_STORAGE_CODE) {
                    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        validate_external_permission();
                    } else {
                 //       Snackbar.make(vista, "Sin el permiso, no puedo realizar la" + "acción, Se define guardara en memoria interna", Snackbar.LENGTH_SHORT).show();
                        System.out.println("El dossssssssssssssssssssss");
                        almacen = new Almacen(this);

                    }
                }

        }
    }
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, 3);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        session = new SessionManager(getApplicationContext());
        conexion = new Conexion(getApplicationContext());
        session.firstRun();
        Button btnIccid = (Button) findViewById(R.id.btnIccid);
        Button btnImei = (Button) findViewById(R.id.btnImei);
        Button btnActivar = (Button) findViewById(R.id.boton_aceptar);
        Button btnNueva = (Button) findViewById(R.id.btnNueva);
        Button btnProducto = (Button) findViewById(R.id.btnProducto);
        Button btnConsultar = (Button)findViewById(R.id.btnConsultar);

        mySpinner = (Spinner) findViewById(R.id.my_spinner);
        campo_imei = (EditText) findViewById(R.id.campo_imei);
        campo_iccid = (EditText) findViewById(R.id.campo_iccid);
        campo_codigo_ciudad = (EditText) findViewById(R.id.campo_ciudad);
        txtResultado = (TextView) findViewById(R.id.txtResultado);
        HashMap<String,String> mapaCrendenciales= session.getUserDetails();
        credencial.setClaveDistribuidor(mapaCrendenciales.get("distribuidor"));
        credencial.setClaveVendedor(mapaCrendenciales.get("vendedor"));
        txtClaveVendedor = (TextView) findViewById(R.id.txtClaveVendedor);
        txtClaveDistribuidor = (TextView) findViewById(R.id.txtClaveDistribuidor);
        txtClaveVendedor.setText(credencial.getClaveVendedor());
        txtClaveDistribuidor.setText(credencial.getClaveDistribuidor());
        mensaje=new Mensaje(getApplicationContext());

        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
          /*      System.out.println("Entroooooooo!");
                ActivityCompat.requestPermissions(ConsultaActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
*/

                file_name_save = "datos_escaneados.txt";
                FICHERO_DIR = Environment.getExternalStorageDirectory() + "/" + file_name_save;
                almacen = new Almacen(ConsultaActivity.this);
                almacen.guardarinfoCSV(1.1, 1.1, 1.1,
                        1.1, 1.1, FICHERO_DIR, 1.1,1.1);

            }
        });







        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if (!detector.isOperational()) {
            campo_iccid.setText("Could not set up the detector!");
            return;
        }








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
               // realizaActivacion();
                if(conexion.estaConectado()){
                    confirmaAcciones();
               }else{

                    mensaje.getMostrarAlerta(ConsultaActivity.this, getString(R.string.error_titulo_conexion_nodisponible),
                            getString(R.string.error_conexion_nodisponible),"OK");
                }

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





        cargaCatalogoProductos();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    private void validate_external_permission() {


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            almacen = new Almacen(this);
          //  permiso_Alma_OK = true;

        } else {
            request_premission_External();
        }

    }

    public void request_premission_External() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(this.focusView, "Se requieren persmisos para grabar en Memoria Externa",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(ConsultaActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SOLICITA_PERMISO_STORAGE_CODE);

                                }
                            }
                    );


        } else {
            ActivityCompat.requestPermissions(ConsultaActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SOLICITA_PERMISO_STORAGE_CODE);

        }
    }



    private void cargaCatalogoProductos(){
        updateAndroidSecurityProvider(this);

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                //   add = new UsuarioDAO().insert(usuario, fotoPerfil);
                try {
                    if (!conexion.isAvailableWSDL(Constantes.URL))
                     //   Log.e("RVISOR MOBILE", "El WS " + Constantes.URL + " no esta en linea ");
                        return false;

                } catch (Exception e) {
                    return false;
                }
                return true;

            }

            @Override
            protected void onPostExecute(Boolean result) {
                // progress.dismiss();
                if(!result){

                    mensaje.getMostrarAlerta(ConsultaActivity.this,"Atención",
                            "El WS de catalogos no esta disponible \n"
                                    + "Favor de comunicarse con el area comercial\n","OK");

                }
            }
        }.execute("");


        coopelWS = new CoopelWS(getApplicationContext(), mySpinner,credencial.getClaveDistribuidor());
        coopelWS.execute();

    }

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


    private void ScanEAN() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        //intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
        this.startActivityForResult(intent, 1);
    }


    private void ScanIccid() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        //intent.putExtra("SCAN_MODE","PRODUCT_MODE");
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
            else if (requestCode == 3){


                launchMediaScanIntent();
                try {
                    Bitmap bitmap = decodeBitmapUri(this, imageUri);
                    if (detector.isOperational() && bitmap != null) {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<Barcode> barcodes = detector.detect(frame);
                        for (int index = 0; index < barcodes.size(); index++) {
                            Barcode code = barcodes.valueAt(index);
                            campo_iccid.setText(campo_iccid.getText() + code.displayValue + "\n");

                            //Required only if you need to extract the type of barcode
                            int type = barcodes.valueAt(index).valueFormat;
                            switch (type) {
                                case Barcode.CONTACT_INFO:
                                    Log.i(LOG_TAG, code.contactInfo.title);
                                    break;
                                case Barcode.EMAIL:
                                    Log.i(LOG_TAG, code.email.address);
                                    break;
                                case Barcode.ISBN:
                                    Log.i(LOG_TAG, code.rawValue);
                                    break;
                                case Barcode.PHONE:
                                    Log.i(LOG_TAG, code.phone.number);
                                    break;
                                case Barcode.PRODUCT:
                                    Log.i(LOG_TAG, code.rawValue);
                                    break;
                                case Barcode.SMS:
                                    Log.i(LOG_TAG, code.sms.message);
                                    break;
                                case Barcode.TEXT:
                                    Log.i(LOG_TAG, code.rawValue);
                                    break;
                                case Barcode.URL:
                                    Log.i(LOG_TAG, "url: " + code.url.url);
                                    break;
                                case Barcode.WIFI:
                                    Log.i(LOG_TAG, code.wifi.ssid);
                                    break;
                                case Barcode.GEO:
                                    Log.i(LOG_TAG, code.geoPoint.lat + ":" + code.geoPoint.lng);
                                    break;
                                case Barcode.CALENDAR_EVENT:
                                    Log.i(LOG_TAG, code.calendarEvent.description);
                                    break;
                                case Barcode.DRIVER_LICENSE:
                                    Log.i(LOG_TAG, code.driverLicense.licenseNumber);
                                    break;
                                default:
                                    Log.i(LOG_TAG, code.rawValue);
                                    break;
                            }
                        }
                        if (barcodes.size() == 0) {
                            campo_iccid.setText("Scan Failed: Found nothing to scan");
                        }
                    } else {
                        campo_iccid.setText("Could not set up the detector!");
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                            .show();
                    Log.e(LOG_TAG, e.toString());
                }
            }
        }
    }



    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
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








    public void mostrarAlertaExito(Context context, String title, String message,String titlePositiveButton) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ConsultaActivity.this,R.style.myDialog);
        alert.setTitle(title);
        alert.setMessage(message+ "\n"
        );
        alert.setPositiveButton(titlePositiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                dialog.dismiss();
                finish();
                startActivity(getIntent());

            }
        });
        AlertDialog dialog = alert.create();
        dialog.setCancelable(false);
        dialog.show();
    }


}
