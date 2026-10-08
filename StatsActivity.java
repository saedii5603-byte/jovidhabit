package com.jovid.habit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class StatsActivity extends Activity {

    String PREFS_NAME = "HabitPrefs";
    int count = 0;
    String habitName = "";
    boolean isDarkMode = false;

    TextView textStatHabitName;
    TextView textStatDone, textStatLeft, textStatPercent, textStatStatus;
    LinearLayout notesContainer;

    // ===== برای تم =====
    ScrollView statsScroll;
    LinearLayout statCard;
    TextView statTitle, notesTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        count = settings.getInt("saved_count", 0);
        habitName = settings.getString("habit_name", "عادت من");
        isDarkMode = settings.getBoolean("dark_mode", false);

        // ===== وصل کردن ویوها =====
        textStatHabitName = findViewById(R.id.textStatHabitName);
        textStatDone = findViewById(R.id.textStatDone);
        textStatLeft = findViewById(R.id.textStatLeft);
        textStatPercent = findViewById(R.id.textStatPercent);
        textStatStatus = findViewById(R.id.textStatStatus);
        notesContainer = findViewById(R.id.notesContainer);

        statsScroll = findViewById(R.id.statsScroll);
        statCard = findViewById(R.id.statCard);
        statTitle = findViewById(R.id.statTitle);
        notesTitle = findViewById(R.id.notesTitle);

        Button btnBack = findViewById(R.id.btnBack);
        Button btnEditHabit = findViewById(R.id.btnEditHabit);

        // ===== اعمال تم =====
        applyTheme();

        // ===== داده‌ها =====
        textStatHabitName.setText("🎯 " + habitName);

        int percent = (count * 100) / 30;
        int left = 30 - count;
        if (left < 0) left = 0;

        textStatDone.setText("✅ انجام‌شده: " + count + " روز");
        textStatLeft.setText("⏳ باقی‌مانده: " + left + " روز");
        textStatPercent.setText("📈 درصد: " + percent + "%");

        String status;
        if (count == 0) {
            status = "🌱 تازه شروع کردی — بیا شروع کنیم!";
        } else if (count < 5) {
            status = "🌿 شروع خوبی بود! ادامه بده";
        } else if (count < 10) {
            status = "🔥 داری پیش می‌ری!";
        } else if (count < 20) {
            status = "🌟 فوق‌العاده‌ای!";
        } else if (count < 30) {
            status = "⚡ نزدیک قهرمانی!";
        } else {
            status = "🏆 قهرمان شدی! تبریک!";
        }
        textStatStatus.setText(status);

        // ===== یادداشت‌ها =====
        boolean hasAny = false;
        for (int i = 1; i <= count; i++) {
            String note = settings.getString("note_" + i, "");
            if (!note.isEmpty()) {
                hasAny = true;
                addNoteCard(notesContainer, i, note);
            }
        }

        if (!hasAny) {
            TextView empty = new TextView(this);
            empty.setText("هنوز هیچ یادداشتی ننوشتی.\n\nدفعه بعد که «انجام دادم» را زدی، می‌توانی یادداشت بنویسی 📝");
            empty.setPadding(30, 40, 30, 40);
            empty.setTextSize(15);
            empty.setTextColor(isDarkMode ? 0xFF888888 : 0xFF888888);
            empty.setGravity(android.view.Gravity.CENTER);
            notesContainer.addView(empty);
        }

        // ===== دکمه ویرایش عادت =====
        btnEditHabit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					editHabitName();
				}
			});

        // ===== دکمه برگشت =====
        btnBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
    }

    // ============================================
    // ========== تم (روز / شب) ===================
    // ============================================

    private void applyTheme() {
        if (isDarkMode) {
            // ===== حالت شب =====
            statsScroll.setBackgroundColor(0xFF121212);

            android.graphics.drawable.GradientDrawable card = 
                new android.graphics.drawable.GradientDrawable();
            card.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            card.setColor(0xFF1E1E1E);
            card.setCornerRadius(40);
            card.setStroke(2, 0xFF333333);
            statCard.setBackground(card);

            statTitle.setTextColor(0xFFFFFFFF);
            notesTitle.setTextColor(0xFFFFFFFF);
            textStatStatus.setTextColor(0xFFAAAAAA);
        } else {
            // ===== حالت روز =====
            statsScroll.setBackgroundColor(0xFFF0F2F5);

            android.graphics.drawable.GradientDrawable card = 
                new android.graphics.drawable.GradientDrawable();
            card.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            card.setColor(0xFFFFFFFF);
            card.setCornerRadius(40);
            card.setStroke(2, 0xFFE0E0E0);
            statCard.setBackground(card);

            statTitle.setTextColor(0xFF333333);
            notesTitle.setTextColor(0xFF333333);
            textStatStatus.setTextColor(0xFF666666);
        }
    }

    // ============================================
    // ========== ویرایش عادت =====================
    // ============================================

    private void editHabitName() {
        final EditText input = new EditText(this);
        input.setText(habitName);
        input.setPadding(40, 40, 40, 40);

        LinearLayout container = new LinearLayout(this);
        container.setPadding(40, 20, 40, 20);
        container.addView(input);

        new AlertDialog.Builder(this)
            .setTitle("✏️ ویرایش عادت")
            .setView(container)
            .setPositiveButton("ذخیره", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int w) {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(StatsActivity.this, 
									   "اسم نمی‌تواند خالی باشد!", Toast.LENGTH_SHORT).show();
                    } else {
                        habitName = name;
                        SharedPreferences settings = 
                            getSharedPreferences(PREFS_NAME, 0);
                        settings.edit().putString("habit_name", habitName).commit();
                        textStatHabitName.setText("🎯 " + habitName);
                        Toast.makeText(StatsActivity.this, 
									   "ذخیره شد ✅", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("انصراف", null)
            .show();
    }

    // ============================================
    // ========== کارت یادداشت ====================
    // ============================================

    private void addNoteCard(LinearLayout parent, int day, String note) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 20, 24, 20);

        // کارت پس‌زمینه داینامیک
        android.graphics.drawable.GradientDrawable cardBg = 
            new android.graphics.drawable.GradientDrawable();
        cardBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        cardBg.setCornerRadius(40);

        if (isDarkMode) {
            cardBg.setColor(0xFF1E1E1E);
            cardBg.setStroke(2, 0xFF333333);
        } else {
            cardBg.setColor(0xFFFFFFFF);
            cardBg.setStroke(2, 0xFFE0E0E0);
        }
        card.setBackground(cardBg);
        card.setElevation(4f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);

        TextView title = new TextView(this);
        title.setText("📌  روز " + day);
        title.setTextSize(15);
        title.setTextColor(0xFF2196F3);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 8);
        card.addView(title);

        TextView body = new TextView(this);
        body.setText(note);
        body.setTextSize(15);
        body.setTextColor(isDarkMode ? 0xFFDDDDDD : 0xFF333333);
        body.setLineSpacing(4, 1);
        card.addView(body);

        parent.addView(card);
    }
}
