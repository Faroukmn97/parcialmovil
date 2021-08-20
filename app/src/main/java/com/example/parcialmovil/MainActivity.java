package com.example.parcialmovil;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class MainActivity extends AppCompatActivity {

    private String URL = "http://www.geognos.com/";
    private RequestQueue requestQueue;

    private static final String TAG = "MyTag";
    TextView tvResult;
    Button btnChoosePic;

    private static final int STORAGE_PERMISSION_CODE = 133;

    ActivityResultLauncher<Intent> intentActivityResultLauncher;

    InputImage inputImage;
    TextRecognizer textRecognizer;

    // variables para mantener los datos
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        tvResult = findViewById(R.id.tvResult);
        btnChoosePic = findViewById(R.id.btnChoosePic);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);


        intentActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        //Handle picture here
                         Intent data = result.getData();
                        Uri imageUri = data.getData();

                        convertImageToText(imageUri);
                    }
                });

        btnChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intentActivityResultLauncher.launch(intent);
            }
        });
    }
    private void goprocess(){
        Intent i = new Intent(this, ProcesActivity.class);
        // bandera para que no se creen nuevas actividades innecesarias
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
    // para convertir la primera letra en mayuscula y el resto de la cadena en minuscula
    public static String upperCaseFirst(String val) {
        Log.d("Entrando","Extrxxa");
        char[] arr = val.toCharArray();
        arr[0] = Character.toUpperCase(arr[0]);
        return new String(arr);
    }

    public int ContarEspacios(String texto, int index)
    {
        int cont = 0;

        if(index == texto.length())
            return cont;

        if(texto.charAt(index) ==  ' ')
        {
            cont++;
        }
        index++;
        return cont + ContarEspacios(texto, index);
    }
    private void init(){
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
    }

    private void convertImageToText(Uri imageUri) {
        //Preparar input  imagen
        try {
            inputImage = inputImage.fromFilePath(getApplicationContext(), imageUri);

            // get Text from input image

            Task<Text> result = textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(@NonNull Text text) {
                            boolean band = false;
                            char[] caracteres = text.getText().toCharArray();
                            if(ContarEspacios(text.getText(),0) > 0){

                                caracteres[0] = Character.toUpperCase(caracteres[0]);
                                // el -2 es para evitar una excepción al caernos del arreglo
                                for (int i = 0; i < text.getText().length()- 2; i++)
                                    // Es 'palabra'
                                    if (caracteres[i] == ' ' || caracteres[i] == '.' || caracteres[i] == ',')
                                    {
                                        // Reemplazamos
                                        caracteres[i + 1] = Character.toUpperCase(caracteres[i + 1]);
                                    }
                                band = true;
                            }
                            if(band){
                                Log.d("texto",new String(caracteres));
                                tvResult.setText(new String(caracteres));
                                send(new String(caracteres));
                            }else {
                                 String output = upperCaseFirst(text.getText().toLowerCase());
                                Log.d("texto",output);
                                tvResult.setText(output);
                                send(output);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            tvResult.setText("Error :" + e.getMessage());
                            Log.d(TAG, " Error :" + e.getMessage());
                        }
                    });
        }catch (Exception e){
            Log.d(TAG, "convertImageToText: Error :" + e.getMessage());
        }
    }
    // caracteres especiales
    public static String fixEncoding(String response) {
        try {
            byte[] u = response.toString().getBytes(
                    "ISO-8859-1");
            response = new String(u, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    private void send(String output) {
        StringRequest request = new StringRequest(
                Request.Method.GET,
                URL + "api/en/countries/info/all.json",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int size = response.length();
                        boolean band = false;
                        response = fixEncoding(response);
                        JSONObject json_transform = null;
                        int pos = response.indexOf(output);
                        if (pos != -1){
                            try {
                               // Log.d("Respuesta", response);
                                // Log.d("Posicion", String.valueOf(pos));
                                char[] caracteres = response.toCharArray();
                                String Alpha2Code = caracteres[pos-15] +""+ caracteres[pos-14];
                                Log.d("Alpha2Code", Alpha2Code);
                                json_transform = new JSONObject(response);
                                if(json_transform.getString("StatusMsg").equals("OK")){
                                    Log.d("Status", "TODO BIEN");
                                   // String basejson = json_transform.getJSONObject("Results").getString(Alpha2Code);
                                    String basejson = json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).toString();
                                    Log.d("JSON RICOLINO", basejson);

                                    //    private String geowest,geoeast,geonorth,geosouth;

                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("Alpha2Code",Alpha2Code);
                                    editor.putString("pais",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getString("Name"));
                                    editor.putString("capital",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("Capital").getString("Name"));
                                    editor.putString("CodeISO2",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("CountryCodes").getString("iso2"));
                                    editor.putString("CodeISONum",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("CountryCodes").getString("isoN"));
                                    editor.putString("CodeISO3",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("CountryCodes").getString("iso3"));
                                    editor.putString("CodeFIPS",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("CountryCodes").getString("fips"));
                                    editor.putString("Center",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getString("GeoPt"));
                                    editor.putString("imgbandera",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getString("CountryInfo"));

                                    // puntos
                                    editor.putString("geowest",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("GeoRectangle").getString("West"));
                                    editor.putString("geoeast",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("GeoRectangle").getString("East"));
                                    editor.putString("geonorth",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("GeoRectangle").getString("North"));
                                    editor.putString("geosouth",json_transform.getJSONObject("Results").getJSONObject(Alpha2Code).getJSONObject("GeoRectangle").getString("South"));
                                    //http://www.geognos.com/api/en/countries/flag/EC.png
                                    editor.commit();
                                    goprocess();

                                }else{
                                    Log.d("Status", "ERROR");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else{
                            Toast.makeText(MainActivity.this, "El pais no se encuentra en la lista", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=utf-8");
                params.put("Accept", "application/json");
                return params;
            }
        };
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request);
        } else {
            requestQueue.add(request);
        }
        // requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        // Permisos de Storage

    }

    public void checkPermission(String permission, int requestCode){
        // Checking
        if(ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED)
        {
            // Take Permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                Toast.makeText(MainActivity.this, "Storage permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "Storage permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}