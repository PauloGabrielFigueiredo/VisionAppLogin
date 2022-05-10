package VisitPlann.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import VisitPlann.DTOS.UserDto;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private String email, password;
    private String URL = "http://188.251.46.46:80/api/account/login";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_v2);
        email = password = "";
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

    }

    public void login(View view) {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonBody.toString();

        if (!email.equals("") && !password.equals("")) {


            JsonRequest JsonRequest = new JsonRequest(Request.Method.POST, URL, requestBody,

                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            Log.d("On response", response);


                            if (response.equals("200")) {

                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (response.equals("failure")) {
                                Toast.makeText(Login.this, "Invalid Login Id/Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Login.this, error.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("erro", error.toString());

                }
            }) {


                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }


                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    String data= "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        data = new String(response.data);
                        Log.d("data1", data);
                        Log.d("response1", "[raw json]: " + (new String(response.data)));

                        Gson g = new Gson();
                        UserDto s = g.fromJson(data, UserDto.class);
                        Log.d("token", s.token);




                        // can get more details such as response.headers
                    }
                    return Response.success("200", HttpHeaderParser.parseCacheHeaders(response));
                }


            };
            //try not show timeout error
            JsonRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(JsonRequest);
        } else {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        }

    }


    public void register(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
        finish();
    }


}