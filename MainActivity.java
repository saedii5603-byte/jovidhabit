package com.jovid.habit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.FrameLayout;
import android.content.SharedPreferences;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;
import java.util.Calendar;

public class MainActivity extends Activity {

    int count = 0;
    int streak = 0;
    int bestStreak = 0;
    TextView textDays;
    ProgressBar progressBar;
    Button btnDone, btnReset;
    String PREFS_NAME = "HabitPrefs";

    TextView textPercent;
    TextView textMotivation;
    TextView textHabitName;
    TextView textStreak;
    Button btnUndo, btnStats, btnTheme, btnAbout, btnReminder;

    ScrollView mainScroll;
    LinearLayout cardCounter, cardProgress;
    TextView textDaysLabel, textProgressLabel, textFromLabel;

    ConfettiView confettiView;

    String habitName = "";
    boolean isDarkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // ===== لایه Confetti روی همه چیز =====
        confettiView = new ConfettiView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT);
        confettiView.setLayoutParams(params);
        confettiView.setClickable(false);
        addContentView(confettiView, params);

        textDays = findViewById(R.id.textDays);
        progressBar = findViewById(R.id.progressBar);
        btnDone = findViewById(R.id.btnDone);
        btnReset = findViewById(R.id.btnReset);
        textPercent = findViewById(R.id.textPercent);
        textMotivation = findViewById(R.id.textMotivation);
        textHabitName = findViewById(R.id.textHabitName);
        textStreak = findViewById(R.id.textStreak);
        btnUndo = findViewById(R.id.btnUndo);
        btnStats = findViewById(R.id.btnStats);
        btnTheme = findViewById(R.id.btnTheme);
        btnAbout = findViewById(R.id.btnAbout);
        btnReminder = findViewById(R.id.btnReminder);

        mainScroll = findViewById(R.id.mainScroll);
        cardCounter = findViewById(R.id.cardCounter);
        cardProgress = findViewById(R.id.cardProgress);
        textDaysLabel = findViewById(R.id.textDaysLabel);
        textProgressLabel = findViewById(R.id.textProgressLabel);
        textFromLabel = findViewById(R.id.textFromLabel);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        count = settings.getInt("saved_count", 0);
        habitName = settings.getString("habit_name", "");
        isDarkMode = settings.getBoolean("dark_mode", false);
        streak = settings.getInt("streak", 0);
        bestStreak = settings.getInt("best_streak", 0);

        checkStreakBreak();
        applyTheme();

        if (habitName.isEmpty()) {
            askHabitName();
        } else {
            textHabitName.setText(habitName);
        }

        updateUI();

        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDarkMode = !isDarkMode;
                SharedPreferences s = getSharedPreferences(PREFS_NAME, 0);
                s.edit().putBoolean("dark_mode", isDarkMode).commit();
                applyTheme();
                Toast.makeText(MainActivity.this, 
                    isDarkMode ? "🌙 حالت شب" : "☀️ حالت روز", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count < 30) {
                    count++;
                    incrementStreak();
                    updateUI();
                    saveCount();
                    animateNumber();

                    if (count == 10 || count == 20 || count == 30) {
                        showConfetti();
                    }

                    showMotivationIfSpecial();
                    askForNote();

                    if (count == 30) {
                        Toast.makeText(MainActivity.this, 
                            "تبریک! ۳۰ روز رو کامل کردی! قهرمان!", 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, 
                        "قبلاً ۳۰ روز رو کامل کردی!", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count > 0) {
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("برگشت")
                        .setMessage("می‌خواهی یکی از روزها را کم کنی؟")
                        .setPositiveButton("بله", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                count--;
                                updateUI();
                                saveCount();
                                animateNumber();
                                Toast.makeText(MainActivity.this, 
                                    "یکی کم شد", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("انصراف", null)
                        .show();
                } else {
                    Toast.makeText(MainActivity.this, 
                        "چیزی برای کم کردن نیست!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReminderDialog();
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("ریست کردن")
                    .setMessage("مطمئنی می‌خواهی همه‌چیز را از اول شروع کنی؟")
                    .setPositiveButton("بله", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int w) {
                            count = 0;
                            streak = 0;
                            bestStreak = 0;
                            SharedPreferences s = getSharedPreferences(PREFS_NAME, 0);
                            s.edit().putInt("streak", 0).commit();
                            s.edit().putInt("best_streak", 0).commit();
                            s.edit().remove("last_date").commit();
                            updateUI();
                            saveCount();
                            clearAllNotes();
                        }
                    })
                    .setNegativeButton("انصراف", null)
                    .show();
            }
        });
    }

    // ============================================
    // ========== Confetti (جشن) ==================
    // ============================================

    private void showConfetti() {
        if (confettiView != null) {
            confettiView.bringToFront();
            confettiView.start();
        }
    }

    // ============================================
    // ========== Reminder (یادآوری) ==============
    // ============================================

    private void openReminderDialog() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean isOn = settings.getBoolean("reminder_on", false);
        int savedHour = settings.getInt("reminder_hour", 20);
        int savedMinute = settings.getInt("reminder_minute", 0);

        final EditText inputHour = new EditText(this);
        inputHour.setHint("ساعت (0-23)");
        inputHour.setText(String.valueOf(savedHour));
        inputHour.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputHour.setPadding(40, 30, 40, 30);

        final EditText inputMinute = new EditText(this);
        inputMinute.setHint("دقیقه (0-59)");
        inputMinute.setText(String.valueOf(savedMinute));
        inputMinute.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputMinute.setPadding(40, 30, 40, 30);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        TextView label1 = new TextView(this);
        label1.setText("⏰ ساعت یادآوری:");
        label1.setTextSize(14);
        label1.setTextColor(0xFF666666);
        container.addView(label1);
        container.addView(inputHour);

        TextView label2 = new TextView(this);
        label2.setText("⏱ دقیقه:");
        label2.setTextSize(14);
        label2.setTextColor(0xFF666666);
        label2.setPadding(0, 20, 0, 0);
        container.addView(label2);
        container.addView(inputMinute);

        TextView info = new TextView(this);
        if (isOn) {
            info.setText("\n✅ یادآوری فعال است\n" +
                "هر روز ساعت " + savedHour + ":" + 
                String.format("%02d", savedMinute));
            info.setTextColor(0xFF4CAF50);
        } else {
            info.setText("\n⚠️ یادآوری خاموش است");
            info.setTextColor(0xFFF44336);
        }
        info.setTextSize(13);
        info.setPadding(0, 20, 0, 0);
        container.addView(info);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⏰ یادآوری روزانه");
        builder.setView(container);

        builder.setPositiveButton("✅ تنظیم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int w) {
                try {
                    int hour = Integer.parseInt(inputHour.getText().toString().trim());
                    int minute = Integer.parseInt(inputMinute.getText().toString().trim());
                    
                    if (hour < 0 || hour > 23) {
                        Toast.makeText(MainActivity.this, 
                            "ساعت باید بین ۰ تا ۲۳ باشه!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (minute < 0 || minute > 59) {
                        Toast.makeText(MainActivity.this, 
                            "دقیقه باید بین ۰ تا ۵۹ باشه!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    setReminder(hour, minute);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, 
                        "لطفاً عدد وارد کن!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNeutralButton("🔕 خاموش", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int w) {
                cancelReminder();
            }
        });

        builder.setNegativeButton("انصراف", null);
        builder.show();
    }

    private void setReminder(int hour, int minute) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putBoolean("reminder_on", true).commit();
        settings.edit().putInt("reminder_hour", hour).commit();
        settings.edit().putInt("reminder_minute", minute).commit();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        if (c.getTimeInMillis() < System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, flags);

        AlarmManager alarmManager = (AlarmManager) 
            getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                c.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            );
        }

        Toast.makeText(this, 
            "✅ یادآوری تنظیم شد برای ساعت " + hour + ":" + 
            String.format("%02d", minute), 
            Toast.LENGTH_LONG).show();
    }

    private void cancelReminder() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putBoolean("reminder_on", false).commit();

        Intent intent = new Intent(this, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, flags);

        AlarmManager alarmManager = (AlarmManager) 
            getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        Toast.makeText(this, 
            "🔕 یادآوری خاموش شد", 
            Toast.LENGTH_SHORT).show();
    }

    // ============================================
    // ========== Streak ==========================
    // ============================================

    private String getTodayDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }

    private String getYesterdayDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }

    private void checkStreakBreak() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String lastDate = settings.getString("last_date", "");

        if (lastDate.isEmpty()) return;
        
        String today = getTodayDate();
        if (lastDate.equals(today)) return;
        
        String yesterday = getYesterdayDate();

        if (!lastDate.equals(yesterday)) {
            if (streak > 0) {
                streak = 0;
                settings.edit().putInt("streak", 0).commit();
                Toast.makeText(MainActivity.this, 
                    "💔 Streak صفر شد! ناراحت نباش، از امروز دوباره شروع کن 💪", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void incrementStreak() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String lastDate = settings.getString("last_date", "");
        String today = getTodayDate();

        if (lastDate.equals(today)) return;

        String yesterday = getYesterdayDate();

        if (lastDate.equals(yesterday)) {
            streak++;
        } else {
            streak = 1;
        }

        if (streak > bestStreak) {
            bestStreak = streak;
            settings.edit().putInt("best_streak", bestStreak).commit();
        }

        settings.edit().putInt("streak", streak).commit();
        settings.edit().putString("last_date", today).commit();
    }

    // ============================================
    // ========== تم (روز / شب) ===================
    // ============================================

    private void applyTheme() {
        if (isDarkMode) {
            btnTheme.setText("☀️");
            mainScroll.setBackgroundColor(0xFF121212);

            android.graphics.drawable.GradientDrawable card = 
                new android.graphics.drawable.GradientDrawable();
            card.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            card.setColor(0xFF1E1E1E);
            card.setCornerRadius(40);
            card.setStroke(2, 0xFF333333);
            cardCounter.setBackground(card);
            cardProgress.setBackground(card);

            textHabitName.setTextColor(0xFFFFFFFF);
            textStreak.setTextColor(0xFFFFA726);
            textDaysLabel.setTextColor(0xFFBBBBBB);
            textMotivation.setTextColor(0xFFAAAAAA);
            textProgressLabel.setTextColor(0xFFFFFFFF);
            textFromLabel.setTextColor(0xFF888888);

            btnTheme.setBackgroundColor(0xFF333333);
            btnTheme.setTextColor(0xFFFFFFFF);
        } else {
            btnTheme.setText("🌙");
            mainScroll.setBackgroundColor(0xFFF0F2F5);

            android.graphics.drawable.GradientDrawable card = 
                new android.graphics.drawable.GradientDrawable();
            card.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            card.setColor(0xFFFFFFFF);
            card.setCornerRadius(40);
            card.setStroke(2, 0xFFE0E0E0);
            cardCounter.setBackground(card);
            cardProgress.setBackground(card);

            textHabitName.setTextColor(0xFF333333);
            textStreak.setTextColor(0xFFFF6600);
            textDaysLabel.setTextColor(0xFF666666);
            textMotivation.setTextColor(0xFF888888);
            textProgressLabel.setTextColor(0xFF333333);
            textFromLabel.setTextColor(0xFF999999);

            btnTheme.setBackgroundColor(0xFFFFFFFF);
            btnTheme.setTextColor(0xFF333333);
        }
    }

    // ============================================
    // ========== اسم عادت ========================
    // ============================================

    private void askHabitName() {
        final String[] habits = {
            "ورزش 🏃", "مطالعه 📚", "آب خوردن 💧",
            "مدیتیشن 🧘", "خواب زودتر 😴", "ترک عادت بد 🚭",
            "شکرگزاری 🙏", "✏️ خودم می‌نویسم"
        };

        ListView listView = new ListView(MainActivity.this);
        listView.setDivider(null);
        listView.setDividerHeight(0);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1, habits) {
            @Override
            public View getView(int position, View convertView, 
                    android.view.ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setTextSize(17);
                tv.setPadding(50, 35, 50, 35);
                tv.setTextColor(0xFF333333);
                tv.setGravity(android.view.Gravity.CENTER);
                return v;
            }
        };

        listView.setAdapter(adapter);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
            .setTitle("🎉 خوش آمدی!")
            .setView(listView)
            .setCancelable(false)
            .create();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, 
                    int position, long id) {
                dialog.dismiss();
                if (position == habits.length - 1) {
                    openCustomHabitDialog();
                } else {
                    habitName = habits[position];
                    saveHabitName();
                    textHabitName.setText(habitName);
                    Toast.makeText(MainActivity.this, 
                        "عالی! بزن بریم 🚀", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void openCustomHabitDialog() {
        final EditText input = new EditText(MainActivity.this);
        input.setHint("مثلاً: پیاده‌روی روزانه");
        input.setPadding(40, 40, 40, 40);

        LinearLayout container = new LinearLayout(MainActivity.this);
        container.setPadding(40, 20, 40, 20);
        container.addView(input);

        new AlertDialog.Builder(MainActivity.this)
            .setTitle("✏️ عادت خودت را بنویس")
            .setView(container)
            .setPositiveButton("ذخیره", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int w) {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(MainActivity.this, 
                            "اسم عادت نمی‌تواند خالی باشد!", 
                            Toast.LENGTH_SHORT).show();
                        askHabitName();
                    } else {
                        habitName = name;
                        saveHabitName();
                        textHabitName.setText(habitName);
                        Toast.makeText(MainActivity.this, 
                            "عالی! بزن بریم 🚀", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setCancelable(false)
            .show();
    }

    private void saveHabitName() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("habit_name", habitName).commit();
    }

    // ============================================
    // ====== جمله‌های انگیزشی ====================
    // ============================================

    private void showMotivationIfSpecial() {
        String message = "";
        String title = "";
        int color = 0xFF4CAF50;

        if (count == 1) {
            title = "🌟 روز اول!";
            message = "شروع کردی!\nاولین قدم همیشه سخت‌ترینه 💪";
        } else if (count == 3) {
            title = "🌱 روز ۳!";
            message = "۳ روز " + habitName + "!\nداری عادت رو می‌سازی 🌱";
        } else if (count == 5) {
            title = "🔥 روز ۵!";
            message = "۵ روز " + habitName + "!\nداری جا می‌افتی توی مسیر 🔥";
        } else if (count == 7) {
            title = "⭐ یک هفته!";
            message = "یک هفته کامل " + habitName + "!\nاین یعنی پشتکار ⭐";
        } else if (count == 10) {
            title = "🎉 ۱۰ روز!";
            message = "۱۰ روز " + habitName + "!\nتو داری ثابت می‌کنی که می‌تونی 💯";
            color = 0xFFFF9800;
        } else if (count == 14) {
            title = "🌟 دو هفته!";
            message = "۲ هفته " + habitName + "!\nتغییر توی وجودت شروع شده 🌟";
            color = 0xFFFF9800;
        } else if (count == 15) {
            title = "🚀 نیمه راه!";
            message = "نصف راه رو اومدی!\n۱۵ روز " + habitName + "! 🚀";
            color = 0xFF2196F3;
        } else if (count == 20) {
            title = "🔥 ۲۰ روز!";
            message = "۲۰ روز " + habitName + "!\nاین دیگه یه عادت واقعیه 🔥";
            color = 0xFF2196F3;
        } else if (count == 25) {
            title = "⚡ ۲۵ روز!";
            message = "۲۵ روز " + habitName + "!\nفقط ۵ روز مونده! ⚡";
            color = 0xFF9C27B0;
        } else if (count == 29) {
            title = "😱 ۲۹ روز!";
            message = "فردا روز قهرمانیه!\nیک روز مونده! 😱";
            color = 0xFF9C27B0;
        } else if (count == 30) {
            title = "🏆 قهرمان شدی!";
            message = "۳۰ روز " + habitName + "!\nتو یک قهرمان واقعی هستی! 🏆";
            color = 0xFFFFD700;
        } else {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("ادامه بده 💪", null);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
    }

    private void animateNumber() {
        textDays.setScaleX(1.4f);
        textDays.setScaleY(1.4f);

        android.animation.ObjectAnimator scaleX = 
            android.animation.ObjectAnimator.ofFloat(textDays, "scaleX", 1.4f, 1.0f);
        android.animation.ObjectAnimator scaleY = 
            android.animation.ObjectAnimator.ofFloat(textDays, "scaleY", 1.4f, 1.0f);

        scaleX.setDuration(300);
        scaleY.setDuration(300);

        android.animation.AnimatorSet set = new android.animation.AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.start();
    }

    private void updateUI() {
        textDays.setText(String.valueOf(count));
        progressBar.setProgress(count);

        if (!habitName.isEmpty()) {
            textHabitName.setText(habitName);
        }

        if (streak > 0) {
            textStreak.setText("🔥 " + streak + " روز پشت‌سرهم");
        } else {
            textStreak.setText("🔥 هنوز streak نداری");
        }

        int percent = (count * 100) / 30;
        textPercent.setText(percent + "%");

        android.graphics.drawable.Drawable drawable = progressBar.getProgressDrawable();
        if (drawable != null) {
            int color;
            if (count < 10) color = 0xFFF44336;
            else if (count < 20) color = 0xFFFFC107;
            else color = 0xFF4CAF50;
            drawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (count < 10) textDays.setTextColor(0xFFF44336);
        else if (count < 20) textDays.setTextColor(0xFFFFC107);
        else textDays.setTextColor(0xFF4CAF50);

        if (count >= 30) {
            btnDone.setEnabled(false);
            btnDone.setAlpha(0.5f);
        } else {
            btnDone.setEnabled(true);
            btnDone.setAlpha(1.0f);
        }

        String motivation;
        if (count == 0) motivation = "بیا شروع کنیم! 💪";
        else if (count < 5) motivation = "شروع خوبی بود! ادامه بده 🌱";
        else if (count < 10) motivation = "داری پیش می‌ری! 🔥";
        else if (count < 20) motivation = "فوق‌العاده‌ای! 🌟";
        else if (count < 30) motivation = "نزدیک قهرمانی! ⚡";
        else motivation = "🏆 قهرمان شدی! تبریک!";
        textMotivation.setText(motivation);
    }

    private void saveCount() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt("saved_count", count).commit();
    }

    private void askForNote() {
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("یادداشت روز " + count)
            .setMessage("می‌خواهی برای امروز یادداشتی بنویسی؟")
            .setPositiveButton("بله", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int w) {
                    openNoteEditor();
                }
            })
            .setNegativeButton("نه", null)
            .show();
    }

    private void openNoteEditor() {
        final EditText input = new EditText(MainActivity.this);
        input.setHint("امروز چطور بود؟");
        input.setPadding(30, 30, 30, 30);
        input.setMinLines(3);
        input.setGravity(android.view.Gravity.TOP);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String oldNote = settings.getString("note_" + count, "");
        if (!oldNote.isEmpty()) input.setText(oldNote);

        LinearLayout container = new LinearLayout(MainActivity.this);
        container.setPadding(40, 20, 40, 20);
        container.addView(input);

        new AlertDialog.Builder(MainActivity.this)
            .setTitle("یادداشت روز " + count)
            .setView(container)
            .setPositiveButton("ذخیره", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int w) {
                    String note = input.getText().toString().trim();
                    if (!note.isEmpty()) {
                        saveNote(note);
                        Toast.makeText(MainActivity.this, 
                            "یادداشت ذخیره شد ✅", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("انصراف", null)
            .show();
    }

    private void saveNote(String note) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("note_" + count, note).commit();
    }

    private void clearAllNotes() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 1; i <= 30; i++) {
            editor.remove("note_" + i);
        }
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        count = settings.getInt("saved_count", 0);
        habitName = settings.getString("habit_name", "");
        isDarkMode = settings.getBoolean("dark_mode", false);
        streak = settings.getInt("streak", 0);
        bestStreak = settings.getInt("best_streak", 0);

        checkStreakBreak();

        if (!habitName.isEmpty()) {
            textHabitName.setText(habitName);
        }
        applyTheme();
        updateUI();
    }
}