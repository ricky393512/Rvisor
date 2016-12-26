package telcel.android.rick.com.rvisor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import telcel.android.rick.com.rvisor.telcel.android.rick.com.rvisor.session.SessionManager;

public class ConsultaActivity extends AppCompatActivity {

    EditText campo_correo;
    // Session Manager Class
    SessionManager session;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);
        session = new SessionManager(getApplicationContext());
      Button  btnScan = (Button) findViewById(R.id.btnScan);
        campo_correo = (EditText) findViewById(R.id.campo_correo);
        btnScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ScanEAN();
            }
        });

        Button boton_cancelar = (Button) findViewById(R.id.boton_cancelar);
        boton_cancelar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                salir();
            }
        });
    }

    private void ScanEAN() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
        this.startActivityForResult(intent, 1);
    }


    private void salir(){
        session.logoutUser();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            campo_correo.setText(data.getStringExtra("SCAN_RESULT"));
            // En asyncTask
            // ISBN.callISBNService();
         /*   if (pref.getBoolean("checkServer", true)) { // Sensores
                ISBNTask tarea = new ISBNTask();
                tarea.execute(0);
            }*/
        }
    }


}
