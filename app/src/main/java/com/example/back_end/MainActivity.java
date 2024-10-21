package com.example.back_end;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import beans.Etudiant;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Etudiant> etudiantList;
    RequestQueue requestQueue;
    public static final int PICK_IMAGE_REQUEST = 1;
    String url = "http://10.0.2.2/php-mobile2/ws/find.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MaterialToolbar matriel = findViewById(R.id.materialToolbar);
        setSupportActionBar(matriel);

        recyclerView = findViewById(R.id.list_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        etudiantList = new ArrayList<>();

        requestQueue = Volley.newRequestQueue(this);
        loadEtudiants();
    }

    private void loadEtudiants() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject etudiantJson = jsonArray.getJSONObject(i);
                        int id = etudiantJson.getInt("id");
                        String nom = etudiantJson.getString("nom");
                        String prenom = etudiantJson.getString("prenom");
                        String ville = etudiantJson.getString("ville");
                        String sexe = etudiantJson.getString("sexe");


                        String imageBase64 = etudiantJson.optString("image", "");
                        Bitmap imageBitmap = null;
                        if (!imageBase64.isEmpty()) {
                            try {
                                byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                                imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            } catch (IllegalArgumentException e) {
                                Toast.makeText(MainActivity.this, "Error decoding image", Toast.LENGTH_SHORT).show();
                            }
                        }

                        Etudiant etudiant = new Etudiant(id, nom, prenom, ville, sexe, imageBitmap);
                        etudiantList.add(etudiant);
                    }


                    EtudiantAdapter adapter = new EtudiantAdapter(MainActivity.this, etudiantList);
                    recyclerView.setAdapter(adapter);
                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(adapter.getSwipeToDeleteCallback());
                    itemTouchHelper.attachToRecyclerView(recyclerView);
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchmenu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEtudiants(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEtudiants(newText);
                return true;
            }
        });

        return true;
    }

    private void filterEtudiants(String query) {
        List<Etudiant> filteredList = new ArrayList<>();

        for (Etudiant etudiant : etudiantList) {
            if (etudiant.getNom().toLowerCase().contains(query.toLowerCase()) ||
                    etudiant.getPrenom().toLowerCase().contains(query.toLowerCase()) ||
                    etudiant.getVille().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(etudiant);
            }
        }


        EtudiantAdapter adapter = new EtudiantAdapter(MainActivity.this, filteredList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_student) {

            Intent addIntent = new Intent(MainActivity.this, Add.class);
            startActivity(addIntent);
            return true;
        } else if (item.getItemId() == R.id.action_share) {

            shareStudentInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageView;

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);

                options.inSampleSize = calculateInSampleSize(options, 200, 200);
                options.inJustDecodeBounds = false;

                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);

                EtudiantAdapter adapter = (EtudiantAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.setSelectedBitmap(bitmap);
                    adapter.notifyDataSetChanged();
                }
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;


            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void shareStudentInfo() {
        if (!etudiantList.isEmpty()) {
            Etudiant etudiantToShare = etudiantList.get(0);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareBody = "Student Info:\n" +
                    "Name: " + etudiantToShare.getNom() + " " + etudiantToShare.getPrenom() + "\n" +
                    "City: " + etudiantToShare.getVille() + "\n" +
                    "Sex: " + etudiantToShare.getSexe();
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Student Info");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } else {
            Toast.makeText(this, "No student data to share", Toast.LENGTH_SHORT).show();
        }

    }
}
