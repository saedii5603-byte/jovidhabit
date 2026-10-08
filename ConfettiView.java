package com.jovid.habit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import java.util.Random;

public class ConfettiView extends View {

    private Paint paint;
    private Random random;
    private Confetti[] confettis;
    private boolean running = false;
    private long startTime = 0;
    private static final long DURATION = 3000; // 3 ثانیه
    private static final int COUNT = 60;

    private int width = 0;
    private int height = 0;

    private static final int[] COLORS = {
        0xFFF44336, // قرمز
        0xFFE91E63, // صورتی
        0xFF9C27B0, // بنفش
        0xFF673AB7, // بنفش تیره
        0xFF3F51B5, // نیلی
        0xFF2196F3, // آبی
        0xFF00BCD4, // فیروزه‌ای
        0xFF009688, // سبز آبی
        0xFF4CAF50, // سبز
        0xFF8BC34A, // سبز روشن
        0xFFCDDC39, // لیمویی
        0xFFFFEB3B, // زرد
        0xFFFFC107, // کهربایی
        0xFFFF9800, // نارنجی
        0xFFFF5722  // نارنجی تیره
    };

    public ConfettiView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        random = new Random();
    }

    public void start() {
        if (getWidth() == 0 || getHeight() == 0) {
            // هنوز layout نشده
            post(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            });
            return;
        }

        width = getWidth();
        height = getHeight();

        confettis = new Confetti[COUNT];
        for (int i = 0; i < COUNT; i++) {
            confettis[i] = createConfetti();
        }

        running = true;
        startTime = System.currentTimeMillis();
        invalidate();
    }

    private Confetti createConfetti() {
        Confetti c = new Confetti();
        c.x = random.nextInt(width);
        c.y = -random.nextInt(200) - 50;
        c.size = 15 + random.nextInt(20);
        c.speedY = 8 + random.nextInt(10);
        c.speedX = -3 + random.nextInt(7);
        c.rotation = random.nextInt(360);
        c.rotSpeed = -10 + random.nextInt(20);
        c.color = COLORS[random.nextInt(COLORS.length)];
        c.shape = random.nextInt(3); // 0=دایره، 1=مربع، 2=مستطیل
        return c;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!running) return;

        long elapsed = System.currentTimeMillis() - startTime;

        if (elapsed > DURATION) {
            running = false;
            canvas.drawColor(Color.TRANSPARENT);
            return;
        }

        for (int i = 0; i < confettis.length; i++) {
            Confetti c = confettis[i];
            
            c.y += c.speedY;
            c.x += c.speedX;
            c.rotation += c.rotSpeed;
            
            // اگه از پایین رفت، از بالا دوباره بیاد
            if (c.y > height + 50) {
                c.y = -50;
                c.x = random.nextInt(width);
            }

            paint.setColor(c.color);

            canvas.save();
            canvas.rotate(c.rotation, c.x, c.y);

            if (c.shape == 0) {
                // دایره
                canvas.drawCircle(c.x, c.y, c.size / 2f, paint);
            } else if (c.shape == 1) {
                // مربع
                RectF rect = new RectF(
                    c.x - c.size / 2f, 
                    c.y - c.size / 2f,
                    c.x + c.size / 2f, 
                    c.y + c.size / 2f
                );
                canvas.drawRect(rect, paint);
            } else {
                // مستطیل
                RectF rect = new RectF(
                    c.x - c.size / 2f, 
                    c.y - c.size / 4f,
                    c.x + c.size / 2f, 
                    c.y + c.size / 4f
                );
                canvas.drawRect(rect, paint);
            }

            canvas.restore();
        }

        invalidate();
    }

    private static class Confetti {
        float x, y;
        float size;
        float speedY;
        float speedX;
        float rotation;
        float rotSpeed;
        int color;
        int shape;
    }
}