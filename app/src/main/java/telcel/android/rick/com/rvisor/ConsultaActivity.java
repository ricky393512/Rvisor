package telcel.android.rick.com.rvisor;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import telcel.android.rick.com.rvisor.pojo.Credencial;
import telcel.android.rick.com.rvisor.pojo.TipoProducto;
import telcel.android.rick.com.rvisor.telcel.android.rick.com.rvisor.session.SessionManager;

public class ConsultaActivity extends AppCompatActivity {

    EditText campo_imei;
    EditText campo_iccid;
    EditText campo_codigo_ciudad;
    TextView txtClaveDistribuidor,txtClaveVendedor,txtResultado;
    // Session Manager Class
    SessionManager session;
    private NotificationManager notifyMgr;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
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
            return(true);
    }
        return(super.onOptionsItemSelected(item));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);
        session = new SessionManager(getApplicationContext());
        session.firstRun();
        Button  btnIccid = (Button) findViewById(R.id.btnIccid);
        Button btnImei = (Button) findViewById(R.id.btnImei);
        Button btnActivar= (Button) findViewById(R.id.boton_aceptar);
        Button btnNueva= (Button) findViewById(R.id.btnNueva);

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

        try{

        Credencial credencial = (Credencial)getIntent().getExtras().getSerializable("credencial");


            txtClaveVendedor = (TextView) findViewById(R.id.txtClaveVendedor);
            txtClaveDistribuidor = (TextView) findViewById(R.id.txtClaveDistribuidor);


            txtClaveVendedor.setText(credencial.getClaveVendedor());
            txtClaveDistribuidor.setText(credencial.getClaveDistribuidor());

        }catch(NullPointerException e){

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
               notification4(1, R.drawable.ic_telefono,"Aviso"," 2222222222");
                txtResultado.setText("El numero es 222222222");
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

        nombreProductos.add(0,"Productyo 1");
        nombreProductos.add(0,"Productyo 2");
        nombreProductos.add(0,"Productyo 3");


        Spinner mySpinner = (Spinner)findViewById(R.id.my_spinner);
        mySpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, nombreProductos));

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


    private void limpiaPantalla(){

        campo_imei.setText(null);
        campo_iccid.setText(null);
        campo_codigo_ciudad.setText(null);
        txtResultado.setText(null);

    }


    private void salir(){
        session.logoutUser();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
           if(requestCode ==1)
            campo_imei.setText(data.getStringExtra("SCAN_RESULT"));
            else if(requestCode==2)
               campo_iccid.setText(data.getStringExtra("SCAN_RESULT"));
            // En asyncTask
            // ISBN.callISBNService();
         /*   if (pref.getBoolean("checkServer", true)) { // Sensores
                ISBNTask tarea = new ISBNTask();
                tarea.execute(0);
            }*/
        }
    }


    public void notification4(int id, int iconId, String titulo, String contenido ) {
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

}
