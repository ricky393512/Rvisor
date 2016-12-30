package telcel.android.rick.com.rvisor.ws;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import telcel.android.rick.com.rvisor.R;
import telcel.android.rick.com.rvisor.pojo.TipoProducto;

/**
 * Created by PIN7025 on 27/12/2016.
 */
public class CoopelWS  extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private TextView txtResultado;
    private List<TipoProducto> listaProductos;
    private Spinner mySpinner;
    /*final String NAMESPACE = "http://hello_webservice/";
    final String URL="http://10.131.5.40:8080/HelloWorldWS/hello?wsdl";
    final String METHOD_NAME = "getProductos";
    final String SOAP_ACTION = "http://hello_webservice/WSConsultaLdap/getProductos";
*/
    final String NAMESPACE = "http://ws.telcel.com/";
    final String URL="https://www.r7.telcel.com/wscadenas/wsActivaMobile?wsdl";
    final String METHOD_NAME = "listado_productos";
    final String SOAP_ACTION = "\"http://ws.telcel.com/listado_productos\"";
    final String codigoDistribuidor;

    public CoopelWS(Context context, Spinner mySpinner,String codigoDistribuidor){
        this.context=context;
        this.mySpinner = mySpinner;
        this.codigoDistribuidor=codigoDistribuidor;

    }

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
       requestObject.addProperty("cod_distribuidor",codigoDistribuidor);
        // Create soap envelop .use version 1.1 of soap
        SoapSerializationEnvelope envelope =
                new SoapSerializationEnvelope(SoapEnvelope.VER11);
        // add the outgoing object as the request
        envelope.setOutputSoapObject(requestObject);

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
     /*   SoapObject soap = (SoapObject) envelope.bodyIn;
        SoapObject soapResult = (SoapObject)soap.getProperty(0);
        Log.i("TOTAL PROPIEDADES S: ",""+soapResult.getPropertyCount());
        for(int i=0;i<soapResult.getPropertyCount();i++)
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
      SoapObject resSoap = (SoapObject) envelope.bodyIn;



        List<SoapObject> result = new ArrayList<SoapObject>();
        if (resSoap != null) {
            SoapObject list = ((SoapObject) resSoap);

            for(int i=0; i<list.getPropertyCount(); i++) {
                SoapObject item = (SoapObject)list.getProperty(i);
                result.add(item);
            }
        }


        TipoProducto[] listaP = null;
     listaP = new TipoProducto[resSoap.getPropertyCount()];

        for (int i = 0; i < listaP.length; i++)
        {
            SoapObject ic = (SoapObject)resSoap.getProperty(i);

            TipoProducto tp = new TipoProducto();
            Log.i("Debbbbb", "id  "+ic.getProperty(0).toString());
            tp.setDescripcion(ic.getProperty(0).toString());
            tp.setIdModalidad(Integer.parseInt(ic.getProperty(1).toString()));
            tp.setIdProducto(Integer.parseInt(ic.getProperty(1).toString()));
    //        tp.setId(ic.getProperty(0).toString());;
           // Log.i("Debbbbb1112", "nombre  "+ic.getProperty(1).toString());
      //     tp.setNombre(ic.getProperty(1).toString());


          //  Log.i("Debug", "Us  "+tp.getId()+" ac  --> "+tp.getNombre());

         listaP[i] = tp;
           listaProductos.add(tp);

        }



        return true;
    }


    @Override
    protected void onPostExecute(final Boolean success) {
        if(success==false){
            Toast.makeText(context, "Usuario No Valido", Toast.LENGTH_LONG).show();


            if (!isAvailableWSDL(URL)) {
                System.out.println("NO esta arriba");
            //    mostrarAlerta();
           //     return false;
            }



            List<TipoProducto> listTP = new ArrayList<>();
            TipoProducto tp1 = new TipoProducto();

//            tp1.setIdModalidad(0);
            tp1.setDescripcion("NO DISPONIBLE");

            listTP.add(tp1);

            ArrayAdapter adapter = new ArrayAdapter(context, R.layout.row,  listTP);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mySpinner.setAdapter(adapter);
//              mySpinner.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, nombreProductos));


        }
        else{
            Toast.makeText(context, "Acceso Concedido: ", Toast.LENGTH_LONG).show();
           // txtResultado.setText("Hola");
            List<String> nombreProductos = new ArrayList<String>();

            for(TipoProducto t:listaProductos){
                nombreProductos.add(t.getDescripcion());
            }


            ArrayAdapter adapter = new ArrayAdapter(context, R.layout.row, listaProductos);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mySpinner.setAdapter(adapter);
            //mySpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.spinner_style, nombreProductos));




                    //this, R.layout., nombreProductos)));
        }


    }

    @Override
    protected void onCancelled() {
        Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
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
                c=null;
            }
        }

    }




}



