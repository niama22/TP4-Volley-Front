package com.example.back_end;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beans.Etudiant;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.ViewHolder> {

    private List<Etudiant> etudiantList;
    private Context context;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Bitmap selectedBitmap;

    public EtudiantAdapter(Context context, List<Etudiant> etudiantList) {
        this.context = context;
        this.etudiantList = etudiantList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Etudiant etudiant = etudiantList.get(position);
        holder.nom.setText(etudiant.getNom());
        holder.prenom.setText(etudiant.getPrenom());
        holder.ville.setText(etudiant.getVille());
        holder.sexe.setText(etudiant.getSexe());
        holder.imageView.setImageBitmap(etudiant.getImage());

        holder.itemView.setOnClickListener(v -> showOptionsDialog(etudiant, position));
    }

    @Override
    public int getItemCount() {
        return etudiantList.size();
    }
    public ItemTouchHelper.SimpleCallback getSwipeToDeleteCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // We are not supporting move, so return false
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Get the swiped position
                int position = viewHolder.getAdapterPosition();
                Etudiant etudiant = etudiantList.get(position);

                // Call the delete logic
                confirmDelete(etudiant, position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                // Optionally, you can add a visual effect while swiping (e.g., background color or icon)
            }
        };
    }

    private void showModifyDialog(Etudiant etudiant) {
        android.app.Dialog dialog = new android.app.Dialog(context);
        dialog.setContentView(R.layout.dialog_modify);

        TextView nom = dialog.findViewById(R.id.et_nom);
        TextView prenom = dialog.findViewById(R.id.et_prenom);
        RadioGroup sexeGroup = dialog.findViewById(R.id.rg_sexe);
        ImageView imageView = dialog.findViewById(R.id.image_view);
        Spinner spinnerVille = dialog.findViewById(R.id.ed_ville);
        Button btnChooseImage = dialog.findViewById(R.id.btn_choose_image);
        Button btnSubmit = dialog.findViewById(R.id.btn_update);

        nom.setText(etudiant.getNom());
        prenom.setText(etudiant.getPrenom());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.cities_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVille.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition(etudiant.getVille());
        spinnerVille.setSelection(spinnerPosition);

        if ("homme".equals(etudiant.getSexe())) {
            ((RadioButton) sexeGroup.findViewById(R.id.rb_homme)).setChecked(true);
        } else {
            ((RadioButton) sexeGroup.findViewById(R.id.rb_femme)).setChecked(true);
        }


        imageView.setImageBitmap(etudiant.getImage());

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            ((MainActivity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        btnSubmit.setOnClickListener(v -> {
            String updatedNom = nom.getText().toString();
            String updatedPrenom = prenom.getText().toString();
            String updatedVille = spinnerVille.getSelectedItem().toString();
            String updatedSexe = ((RadioButton) dialog.findViewById(sexeGroup.getCheckedRadioButtonId())).getText().toString();

            if (selectedBitmap == null) {
                selectedBitmap = etudiant.getImage();
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            updateEtudiant(etudiant.getId(), updatedNom, updatedPrenom, updatedVille, updatedSexe, encodedImage);

            dialog.dismiss();

        });

        dialog.show();
        selectedBitmap = null;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    private void updateEtudiant(int id, String nom, String prenom, String ville, String sexe, String image) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Updating...");
        progressDialog.show();

        String url = "http://10.0.2.2/php-mobile2/controller/updateEtudiant.php"; // Change this to your server endpoint
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    // Notify that the student information has been updated
                    for (int i = 0; i < etudiantList.size(); i++) {
                        if (etudiantList.get(i).getId() == id) {
                            etudiantList.get(i).setNom(nom);
                            etudiantList.get(i).setPrenom(prenom);
                            etudiantList.get(i).setVille(ville);
                            etudiantList.get(i).setSexe(sexe);
                            etudiantList.get(i).setImage(selectedBitmap); // Update image
                            notifyItemChanged(i); // Notify adapter of the updated item
                            break;
                        }
                    }
                    Toast.makeText(context, "Étudiant mis à jour avec succès", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                params.put("nom", nom);
                params.put("prenom", prenom);
                params.put("ville", ville);
                params.put("sexe", sexe);
                params.put("image", image);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void setSelectedBitmap(Bitmap bitmap) {
        this.selectedBitmap = bitmap;

    }

    public Bitmap getSelectedBitmap() {
        return selectedBitmap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nom, prenom, ville, sexe;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.nom);
            prenom = itemView.findViewById(R.id.prenom);
            ville = itemView.findViewById(R.id.ville);
            sexe = itemView.findViewById(R.id.sexe);
            imageView = itemView.findViewById(R.id.circleImageView);
        }
    }

    private void showOptionsDialog(Etudiant etudiant, int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Options")
                .setItems(new CharSequence[]{"Modify"}, (dialog, which) -> {
                    if (which == 0) {
                        showModifyDialog(etudiant);
                    }
                })
                .show();
    }


    private void confirmDelete(Etudiant etudiant, int position) {
        android.app.Dialog dialog = new android.app.Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm_delete);

        TextView confirmationMessage = dialog.findViewById(R.id.confirmation_message);
        confirmationMessage.setText("Êtes-vous sûr de vouloir supprimer " + etudiant.getNom() + "?");

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(v -> {dialog.dismiss();
            notifyItemChanged(position);});

        btnConfirm.setOnClickListener(v -> {
            deleteEtudiant(etudiant.getId(), position);
            dialog.dismiss();

        });

        dialog.show();
    }

    private void deleteEtudiant(int id, int position) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://10.0.2.2/php-mobile2/controller/deleteEtudiant.php?id=" + id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    etudiantList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(context, "Erreur lors de la suppression de l'étudiant", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }

}