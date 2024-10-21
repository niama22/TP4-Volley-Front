package com.example.back_end;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Add extends AppCompatActivity {

    private EditText nom, prenom;
    private Spinner ville;
    private RadioGroup sexeGroup;
    private RadioButton homme, femme;
    private ImageView imageView;
    private Button add, chooseImage;
    private Bitmap bitmap;
    private RequestQueue requestQueue;

    private static final int PICK_IMAGE_REQUEST = 1;
    private String insertUrl = "http://10.0.2.2/php-mobile2/ws/createEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_etudiant);
        MaterialToolbar matriel = findViewById(R.id.materialToolbar);
        setSupportActionBar(matriel);
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        ville = findViewById(R.id.ville);
        sexeGroup = findViewById(R.id.sexe);
        homme = findViewById(R.id.homme);
        femme = findViewById(R.id.femme);
        imageView = findViewById(R.id.imageView);
        add = findViewById(R.id.add);
        chooseImage = findViewById(R.id.choose_image);

        requestQueue = Volley.newRequestQueue(this);

        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEtudiant();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addEtudiant() {
        if (bitmap == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nom.getText().toString().isEmpty() || prenom.getText().toString().isEmpty() || ville.getSelectedItem() == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Server Response", response);
                        Toast.makeText(Add.this, "Étudiant ajouté", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Log.e("Volley Error", "Status Code: " + error.networkResponse.statusCode);
                }
                Log.e("Volley Error", error.getMessage());
                Toast.makeText(Add.this, "Error adding student: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String sexe = sexeGroup.getCheckedRadioButtonId() == R.id.homme ? "homme" : "femme";
                String encodedImage = encodeImageToBase64(bitmap);
                Log.d("Image Base64 Length", String.valueOf(encodedImage.length()));

                Map<String, String> params = new HashMap<>();
                params.put("nom", nom.getText().toString());
                params.put("prenom", prenom.getText().toString());
                params.put("ville", ville.getSelectedItem().toString());
                params.put("sexe", sexe);
                params.put("image", encodedImage);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private Bitmap getScaledBitmap(Bitmap bitmap) {
        int newWidth = 200; // Set desired width
        int newHeight = (bitmap.getHeight() * newWidth) / bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        bitmap = getScaledBitmap(bitmap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            Toast.makeText(this, "Share selected", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_add_student) {
            Toast.makeText(this, "You are already in Add Student", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
