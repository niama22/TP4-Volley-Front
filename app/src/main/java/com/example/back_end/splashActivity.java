package com.example.back_end;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class splashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ImageView imageView = findViewById(R.id.imageView2);

        // Charge l'animation de rotation
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        imageView.startAnimation(rotateAnimation);

        // Délai pour démarrer MainActivity après l'animation
        new Handler().postDelayed(() -> {
            // Démarrer MainActivity
            Intent intent = new Intent(splashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Ferme splashActivity pour ne pas pouvoir revenir en arrière
        }, 2000); // 2000 ms = 2 secondes, ajustez selon la durée de l'animation
    }
}
