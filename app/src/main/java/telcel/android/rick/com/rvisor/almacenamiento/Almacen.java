package telcel.android.rick.com.rvisor.almacenamiento;


import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Created by Ricky on 19/01/2017.
 */
public class Almacen {

    // public  String FICHERO = Environment.getExternalStorageDirectory()+"/GForcer_record_3.txt";
    //public  String FICHERO;
    private Context context;

    public Almacen(Context context) {       this.context = context;    }


    public void guardarinfoCSV(double time, double acelX, double acely, double altitude, double latitude , String fichero, double VSPD, double Distance) {
        try{
            String stadoSD = Environment.getExternalStorageState();
            if (!stadoSD.equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(context, "No puedo escribir en la memoria externa", Toast.LENGTH_LONG).show();
                return;
            }else {
                String texto = String.valueOf(time) + "," + String.valueOf(acelX) + "," + String.valueOf(acely) + ","+
                        String.valueOf(latitude) + "," + String.valueOf(altitude) + "\r\n";
             /*   FileOutputStream f = new FileOutputStream(fichero, true);

                f.write(texto.getBytes());
                f.close();
 */
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fichero, true)));
                    out.println(texto);
                    out.close();
                } catch (IOException e) {
                   System.out.print("proooooo"+e.getMessage());
                }

            }
        }catch (Exception e) {
            Log.e("Gforce",e.getMessage(),e);
        }
    }





}
