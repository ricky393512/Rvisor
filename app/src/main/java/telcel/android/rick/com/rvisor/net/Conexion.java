package telcel.android.rick.com.rvisor.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import telcel.android.rick.com.rvisor.exceptions.WebServiceConexionException;
import telcel.android.rick.com.rvisor.pojo.RespuestaActivacion;
import telcel.android.rick.com.rvisor.pojo.RespuestaLogueo;
import telcel.android.rick.com.rvisor.pojo.TipoProducto;

/**
 * Created by PIN7025 on 03/01/2017.
 */
public class Conexion  {

    // Context
    Context context;
    // Constructor

    public Conexion(Context context){
        this.context = context;

    }

    public Boolean estaConectado(){
        if(conectadoWifi()){
            return true;
        }else{
            if(conectadoRedMovil()){
                return true;
            }else{

                return false;
            }
        }
    }


    public Boolean conectadoWifi(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
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

    public Boolean conectadoRedMovil(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
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


    public int isAvailableWSDLCode(String url) throws IOException {
        HttpURLConnection c = null;
        Integer httpStatusCode=0;
        try {
            URL siteURL = new URL(url);
            c = (HttpURLConnection) siteURL
                    .openConnection();
            c.setRequestMethod("HEAD");
            c.setConnectTimeout(5000); //set timeout to 5 seconds
            c.setReadTimeout(5000);
            c.connect();
            httpStatusCode = c.getResponseCode(); //200, 404 etc.
            System.out.println("Codigo de respuesta!!!!!!!!!!"+httpStatusCode);
            return httpStatusCode;
        } catch (Exception e) {
            throw e;
        } finally {
            if (c != null) {
                c.disconnect();
                c = null;
            }
        }

    }

    public boolean isAvailableWSDL(String url) {
        Integer httpStatusCode=0;
        try {
            httpStatusCode= isAvailableWSDLCode(url);
            Log.d("RVISOR MOBILE", "El WS me responde un codigo "+httpStatusCode);
            if(httpStatusCode==200)
                return true;
            else
                return false;

        } catch (Exception e) {
            Log.e("RVISOR MOBILE", "No levanta el WS por "+e.getMessage());
            throw new WebServiceConexionException("El Web Service No Responde me manda el siguiente codigo: "+httpStatusCode.toString()+" con mensaje "+e.getMessage());
         //   mensajeFinal="WebService: "+e.getMessage();
           // codigoeFinal=httpStatusCode.toString();
          //  return false;
        }

    }

    public SoapSerializationEnvelope getSoapSerializationEnvelope(SoapObject request){
        SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.dotNet = false;
        envelope.implicitTypes=true;
        envelope.setAddAdornments(false);
        return envelope;
    }

    public HttpTransportSE getHttpTransportSE(String url,int timeOut){
        HttpTransportSE ht = new HttpTransportSE(url,timeOut);
        ht.debug=true;
        ht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        return ht;
    }

    public  Object llamadaAlWS(SoapSerializationEnvelope envelope, HttpTransportSE ht, String SOAP_ACTION)
    {

        try
        {
            ht.call(SOAP_ACTION, envelope);
            Log.i("RVISOR MOBILE", "La cadena de envio del WS!! es la siguiente: "+ht.requestDump);
            String theXmlString = ht.responseDump;
            Log.i("RVISOR MOBILE", "La respuesta del WS es la siguiente: "+theXmlString);
            Object response = envelope.bodyIn;

            if(response==null){

                Log.i("RVISOR MOBILE","Aqi caigo");
                throw new WebServiceConexionException("La respuesta del WS es nula");
            } else  if (response instanceof SoapFault){
                SoapFault fault = (SoapFault)response;
                throw new WebServiceConexionException(fault.faultstring);


            }


            return response;
   /*
    } catch (IOException | XmlPullParserException e)

    {
        e.printStackTrace();
        throw new WebServiceConexionException("El problema se debe a: "+e.getMessage());
    */
    }  catch(Exception e)
        {
            e.printStackTrace();
            throw new WebServiceConexionException("El problema se debe a: "+e.getMessage());
        }


    }




    public RespuestaActivacion obtenerRespuestaActivaSoap(SoapObject soap) {
        RespuestaActivacion respuestaActivacion = new RespuestaActivacion();
        Log.i("TOTAL PROPIEDADES S: ", "" + soap.getPropertyCount());
        for (int i = 0; i < soap.getPropertyCount(); i++) {
            SoapObject pii = (SoapObject) soap.getProperty(i);
            respuestaActivacion.setCodigo(Integer.parseInt(pii.getProperty(0).toString()));
            respuestaActivacion.setMensaje(pii.getProperty(1).toString());
            if(respuestaActivacion.getCodigo()==100) {
                respuestaActivacion.setMonto(pii.getProperty(2).toString());
                respuestaActivacion.setTelefono(pii.getProperty(3).toString());
            }

        }
        return respuestaActivacion;
    }

    public RespuestaLogueo obtenerCredencialesSoap(SoapObject soap) {
        RespuestaLogueo respuestaLogueo = new RespuestaLogueo();

        for (int i = 0; i < soap.getPropertyCount(); i++) {
            SoapObject pii = (SoapObject) soap.getProperty(i);
            respuestaLogueo.setCodigo(Integer.parseInt(pii.getProperty(0).toString()));
            respuestaLogueo.setMensaje(pii.getProperty(1).toString());

        }
        return respuestaLogueo;
    }
 /*
    public  Category[] RetrieveFromSoap(SoapObject soap)
    {
        Category[] categories = new Category[soap.getPropertyCount()];
        for (int i = 0; i < categories.length; i++) {
            SoapObject pii = (SoapObject)soap.getProperty(i);
            Category category = new Category();
            category.CategoryId = Integer.parseInt(pii.getProperty(0).toString());
            category.Name = pii.getProperty(1).toString();
            category.Description = pii.getProperty(2).toString();
            categories[i] = category;
        }
        return categor*/

    public List<TipoProducto> obtenerCatalogoProductoActivacionFromSoap(SoapObject soap) {

        List<TipoProducto> listaProductos = new ArrayList<>();
        TipoProducto[] productos = new TipoProducto[soap.getPropertyCount()];
        for (int i = 0; i < productos.length; i++) {
            SoapObject ic = (SoapObject) soap.getProperty(i);
            TipoProducto tp = new TipoProducto();
            //    Log.i("Debbbbb", "id  "+ic.getProperty(0).toString());
            tp.setDescripcion(ic.getProperty(0).toString());
            tp.setIdModalidad(Integer.parseInt(ic.getProperty(1).toString()));
            tp.setIdProducto(Integer.parseInt(ic.getProperty(2).toString()));
            productos[i] = tp;
            listaProductos.add(tp);

        }
        return listaProductos;

    }
}
