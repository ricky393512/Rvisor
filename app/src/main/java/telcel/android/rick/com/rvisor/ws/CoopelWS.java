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
    final String NAMESPACE = "http://ws.telcel.com/";
    final String URL="https://www.r7.telcel.com/wscadenas/wsActivaMobile?wsdl";
    final String METHOD_NAME = "listado_productos";
    final String SOAP_ACTION = "\"http://ws.telcel.com/listado_productos\"";
    final String codigoDistribuidor;
    private String mensajeFinal;
    private String codigoeFinal;

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
        listaProductos = new ArrayList<>();

        if (!isAvailableWSDL(URL)) {
            Log.e("RVISOR MOBILE", "El WS "+URL+" no esta en linea ");
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
        } catch (IOException e ) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        String theXmlString = ht.responseDump;
        Log.i("Resultado T", "La respuesta del WS "+theXmlString);

        SoapObject resSoap = (SoapObject) envelope.bodyIn;

        List<SoapObject> result = new ArrayList<>();
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
            //    Log.i("Debbbbb", "id  "+ic.getProperty(0).toString());
            tp.setDescripcion(ic.getProperty(0).toString());
            tp.setIdModalidad(Integer.parseInt(ic.getProperty(1).toString()));
            tp.setIdProducto(Integer.parseInt(ic.getProperty(2).toString()));
            listaP[i] = tp;
            listaProductos.add(tp);

        }



        return true;
    }


    @Override
    protected void onPostExecute(final Boolean success) {
        if(!success){

            if (!isAvailableWSDL(URL)) {
                Log.e("RVISOR MOBILE", "El WS "+URL+" no esta en linea ");

            }
            List<TipoProducto> listTP = new ArrayList<>();
            TipoProducto tp1 = new TipoProducto();
            tp1.setIdProducto(-1);
            tp1.setDescripcion("NO DISPONIBLE WEB SERVICES  OPRIMIR BOTON PRODUCTOS PARA RECARGAR CATALOGO DE PRODUCTOS");
            listTP.add(tp1);
            ArrayAdapter adapter = new ArrayAdapter(context, R.layout.row,  listTP);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mySpinner.setAdapter(adapter);
        }
        else{
            List<String> nombreProductos = new ArrayList<>();
            for(TipoProducto t:listaProductos){
                nombreProductos.add(t.getDescripcion());
            }
            ArrayAdapter adapter = new ArrayAdapter(context, R.layout.row, listaProductos);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mySpinner.setAdapter(adapter);
        }


    }

    @Override
    protected void onCancelled() {
        Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
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
            Log.i("RVISOR MOBILE", "Arriba !!!!!!!!!!"+httpStatusCode);
            if(httpStatusCode==200)
                return true;
            else {
                mensajeFinal="WebService NO DISPONIBLE: "+codigoeFinal;
                codigoeFinal=httpStatusCode.toString();
                return false;


            }
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



