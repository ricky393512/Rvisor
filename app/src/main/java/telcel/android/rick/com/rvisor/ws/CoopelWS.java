package telcel.android.rick.com.rvisor.ws;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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

    public CoopelWS(Context context, Spinner mySpinner){
        this.context=context;
        this.mySpinner = mySpinner;

    }

    @Override
    protected Boolean doInBackground(Void... params) {
// TODO: attempt authentication against a network service.
//WebService - Opciones




            listaProductos = new ArrayList<TipoProducto>();
        final String NAMESPACE = "http://hello_webservice/";
        final String URL="http://10.131.5.40:8080/HelloWorldWS/hello?wsdl";
        final String METHOD_NAME = "getProductos";
        final String SOAP_ACTION = "http://hello_webservice/WSConsultaLdap/getProductos";

        if (!isAvailableWSDL(URL)) {
            System.out.println("NO esta arriba");
            return false;
        }

        // Create the outgoing message
        SoapObject requestObject = new SoapObject(NAMESPACE, METHOD_NAME);

        // Set Parameter
    //        requestObject.addProperty("arg0", anio);


        // Create soap envelop .use version 1.1 of soap
        SoapSerializationEnvelope envelope =
                new SoapSerializationEnvelope(SoapEnvelope.VER11);

        // add the outgoing object as the request
        envelope.setOutputSoapObject(requestObject);
//     envelope.addMapping(NAMESPACE, "Productividad", Productividad.class);
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

      //  SoapObject resSoap= (SoapObject) response;
        TipoProducto[] listaP = null;
try {
     listaP = new TipoProducto[resSoap.getPropertyCount()];
}catch (NullPointerException e){
    return false;
}
        for (int i = 0; i < listaP.length; i++)
        {
            SoapObject ic = (SoapObject)resSoap.getProperty(i);

            TipoProducto tp = new TipoProducto();
            Log.i("Debbbbb", "id  "+ic.getProperty(0).toString());
            tp.setId(ic.getProperty(0).toString());;
            Log.i("Debbbbb1112", "nombre  "+ic.getProperty(1).toString());
           tp.setNombre(ic.getProperty(1).toString());


            Log.i("Debug", "Us  "+tp.getId()+" ac  --> "+tp.getNombre());
//            lista.add(emc);
            listaP[i] = tp;
            listaProductos.add(tp);

        }



  //      return lista;


/*
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

       // request.addProperty("usuario", usuario);
        //request.addProperty("password", password);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);


        HttpTransportSE ht = new HttpTransportSE(URL);
        ht.debug = true;
        try {
            ht.call(SOAP_ACTION, envelope);
            //  SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
            // SoapObject response=(SoapObject)envelope.bodyIn;
            String theXmlString = ht.responseDump;
            Log.i("Resultado T: ",theXmlString);
            SoapObject soap = (SoapObject) envelope.bodyIn;
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





            Log.i("Resultado S: ","proper"+soap.getPropertyCount());
            //    Log.i("Resultado S: ","atribute"+response.getAttributeCount());

        }
        catch (Exception e)
        {
            Log.i("Error: ",e.getMessage());
            e.printStackTrace();
            return false;
        }
*/
        return false;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if(success==false){
            Toast.makeText(context, "Usuario No Valido", Toast.LENGTH_LONG).show();

            ArrayList<TipoProducto> productos = new ArrayList<TipoProducto>();
            // you can use this array to find the school ID based on name
            ArrayList<String> nombreProductos = new ArrayList<String>();

            nombreProductos.add(0,"Productyo 1");
            nombreProductos.add(0,"Productyo 2");
            nombreProductos.add(0,"Productyo 3");
            nombreProductos.add(0,"Productyo 1");
            nombreProductos.add(0,"Productyo 2");
            nombreProductos.add(0,"Productyo 3");
            nombreProductos.add(0,"Productyo 1");
            nombreProductos.add(0,"Productyo 2");
            nombreProductos.add(0,"Productyo 3");
            nombreProductos.add(0,"Productyo 1");
            nombreProductos.add(0,"Productyo 2");
            nombreProductos.add(0,"Productyo 3");
            nombreProductos.add(0,"Productyo 1");
            nombreProductos.add(0,"Productyo 2");
            nombreProductos.add(0,"Productyo 3");
            nombreProductos.add(0,"Productyo 1");
            nombreProductos.add(0,"Productyo 2");
            nombreProductos.add(0,"Productyo 3");

            List<TipoProducto> listTP = new ArrayList<>();
            TipoProducto tp1 = new TipoProducto();
            TipoProducto tp2 = new TipoProducto();
            tp1.setId("1");
            tp1.setNombre("R1");
            tp2.setId("2");
            tp2.setNombre("R2");

            listTP.add(tp1);
            listTP.add(tp2);

            ArrayAdapter adapter = new ArrayAdapter(context, R.layout.row, listTP);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mySpinner.setAdapter(adapter);
//              mySpinner.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, nombreProductos));


        }
        else{
            Toast.makeText(context, "Acceso Concedido: ", Toast.LENGTH_LONG).show();
           // txtResultado.setText("Hola");
            List<String> nombreProductos = new ArrayList<String>();

            for(TipoProducto t:listaProductos){
                nombreProductos.add(t.getNombre());
            }


            mySpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.spinner_style, nombreProductos));




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



