package VisitPlann.com;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etReenterPassword, etCity,etCountry, etGender;
    private TextView tvStatus;
    private Button btnRegister;
    private String URL = "http://188.251.46.46:80/api/account/register";
    private String name, email, password, reenterPassword,city,country, gender;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_v2);
        etName = findViewById(R.id.etName);
        etEmail= findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etReenterPassword= findViewById(R.id.etReenterPassword);
        etCity= findViewById(R.id.etCity);
        etCountry= findViewById(R.id.etCountry);
        etGender=findViewById(R.id.etGender);


        tvStatus = findViewById(R.id.tvStatus);
        btnRegister= findViewById(R.id.btnRegister);
        name= email= password= reenterPassword=city=country=gender="";



    }
    public void save (View view){
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        reenterPassword = etReenterPassword.getText().toString().trim();
        //city = etCity.getText().toString().trim();
        //country = etCountry.getText().toString().trim();
        //gender = etGender.getText().toString().trim();
        city=country=gender="nada";





        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("city", city);
            jsonBody.put("country", country);
            jsonBody.put("gender", gender);



        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonBody.toString();


        if(!password.equals(reenterPassword)){
            Toast.makeText(this,"Password Mismatch" ,Toast.LENGTH_SHORT).show();
        }
        else if (!email.equals("") && !name.equals("") && !password.equals("")){
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    if (response.equals("200")) {
        //                tvStatus.setText("Successfully registered");
                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        btnRegister.setClickable(false);
                    } else if (response.equals("failure")) {
                        tvStatus.setText("Something went wrong");
                        btnRegister.setClickable(false);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),error.toString().trim() ,Toast.LENGTH_SHORT).show();
                }
            }){
                /*@Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String > data =new HashMap<>();
                    data.put("name", name);
                    data.put("email", email);
                    data.put("password", password);
                    return data;
                }*/




                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }



                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    Log.d("Data", response.data.toString());
                    Log.d("status", String.valueOf(response.statusCode));
                    Log.d("response", "[raw json]: " + (new String(response.data)));




                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }


    }


    public void login(View view) {
        Intent intent= new Intent( this, Login.class);
        startActivity(intent);
        finish();
    }

}
