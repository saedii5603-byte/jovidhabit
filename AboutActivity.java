package com.jovid.habit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;

public class AboutActivity extends Activity {

    String PREFS_NAME = "HabitPrefs";
    boolean isDarkMode = false;

    String devName = "امیرعلی صاعدی";
    String devInsta = "amirali.saedi85";
    String devBio = "«هر روز، یه قدم به بهترین نسخه‌ی خودم نزدیک‌تر»";
    String version = "1.0";

    ScrollView aboutScroll;
    LinearLayout aboutCard, aboutInfoCard;
    TextView aboutTitle, aboutName, aboutBio;
    TextView aboutInstaLabel, aboutInstaValue;
    TextView aboutVersionLabel, aboutVersionValue;
    View aboutDivider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        isDarkMode = settings.getBoolean("dark_mode", false);

        aboutScroll = findViewById(R.id.aboutScroll);
        aboutCard = findViewById(R.id.aboutCard);
        aboutInfoCard = findViewById(R.id.aboutInfoCard);
        aboutTitle = findViewById(R.id.aboutTitle);
        aboutName = findViewById(R.id.aboutName);
        aboutBio = findViewById(R.id.aboutBio);
        aboutInstaLabel = findViewById(R.id.aboutInstaLabel);
        aboutInstaValue = findViewById(R.id.aboutInstaValue);
        aboutVersionLabel = findViewById(R.id.aboutVersionLabel);
        aboutVersionValue = findViewById(R.id.aboutVersionValue);
        aboutDivider = findViewById(R.id.aboutDivider);

        Button btnOpenInsta = findViewById(R.id.btnOpenInsta);
        Button btnBack = findViewById(R.id.btnAboutBack);

        aboutName.setText(devName);
        aboutBio.setText(devBio);
        aboutInstaValue.setText(devInsta);
        aboutVersionValue.setText(version);

        applyTheme();

        btnOpenInsta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInstagram();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void applyTheme() {
        if (isDarkMode) {
            aboutScroll.setBackgroundColor(0xFF121212);

            android.graphics.drawable.GradientDrawable card = 
                new android.graphics.drawable.GradientDrawable();
            card.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            card.setColor(0xFF1E1E1E);
            card.setCornerRadius(40);
            card.setStroke(2, 0xFF333333);
            aboutCard.setBackground(card);
            aboutInfoCard.setBackground(card);

            aboutTitle.setTextColor(0xFFFFFFFF);
            aboutName.setTextColor(0xFFFFFFFF);
            aboutBio.setTextColor(0xFFAAAAAA);
            aboutInstaLabel.setTextColor(0xFFFFFFFF);
            aboutVersionLabel.setTextColor(0xFFFFFFFF);
            aboutDivider.setBackgroundColor(0xFF333333);
        } else {
            aboutScroll.setBackgroundColor(0xFFF0F2F5);

            android.graphics.drawable.GradientDrawable card = 
                new android.graphics.drawable.GradientDrawable();
            card.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            card.setColor(0xFFFFFFFF);
            card.setCornerRadius(40);
            card.setStroke(2, 0xFFE0E0E0);
            aboutCard.setBackground(card);
            aboutInfoCard.setBackground(card);

            aboutTitle.setTextColor(0xFF333333);
            aboutName.setTextColor(0xFF333333);
            aboutBio.setTextColor(0xFF888888);
            aboutInstaLabel.setTextColor(0xFF333333);
            aboutVersionLabel.setTextColor(0xFF333333);
            aboutDivider.setBackgroundColor(0xFFE0E0E0);
        }
    }

    private void openInstagram() {
        try {
            Intent instaIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("http://instagram.com/_u/" + devInsta));
            instaIntent.setPackage("com.instagram.android");
            startActivity(instaIntent);
        } catch (Exception e) {
            try {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("https://instagram.com/" + devInsta));
                startActivity(webIntent);
            } catch (Exception e2) {
                Toast.makeText(AboutActivity.this, 
                    "امکان باز کردن اینستاگرام نبود", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
}