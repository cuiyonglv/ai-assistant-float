package com.aifloat;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FloatWindowService extends Service {

    private WindowManager windowManager;
    private View floatView;
    private View floatMenu;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams menuParams;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private boolean isMenuVisible = false;
    private SharedPreferences prefs;
    private OkHttpClient httpClient;
    private Handler handler;
    private Runnable longPressRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        prefs = getSharedPreferences("AIFloatPrefs", MODE_PRIVATE);
        httpClient = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
        
        createFloatButton();
        createFloatMenu();
        setupSpeechRecognizer();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createFloatButton() {
        floatView = LayoutInflater.from(this).inflate(R.layout.layout_float_button, null);
        
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        FrameLayout floatContainer = (FrameLayout) floatView;
        ImageView ivFloatButton = floatContainer.findViewById(R.id.ivFloatButton);
        TextView tvFloatStatus = floatContainer.findViewById(R.id.tvFloatStatus);

        floatView.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private float initialTouchX, initialTouchY;
            private long touchStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        touchStartTime = System.currentTimeMillis();
                        
                        longPressRunnable = () -> showMenu();
                        handler.postDelayed(longPressRunnable, 500);
                        
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = (int) (initialX + event.getRawX() - initialTouchX);
                        params.y = (int) (initialY + event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatView, params);
                        
                        if (handler.hasCallbacks(longPressRunnable)) {
                            handler.removeCallbacks(longPressRunnable);
                        }
                        
                        return true;

                    case MotionEvent.ACTION_UP:
                        long touchDuration = System.currentTimeMillis() - touchStartTime;
                        
                        if (handler.hasCallbacks(longPressRunnable)) {
                            handler.removeCallbacks(longPressRunnable);
                        }
                        
                        if (touchDuration < 300) {
                            handleSingleClick();
                        }
                        
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(floatView, params);
    }

    private void createFloatMenu() {
        floatMenu = LayoutInflater.from(this).inflate(R.layout.layout_float_menu, null);
        
        menuParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        
        menuParams.gravity = Gravity.TOP | Gravity.START;
        menuParams.x = 200;
        menuParams.y = 300;
        
        floatMenu.setVisibility(View.GONE);

        TextView tvMenuSettings = floatMenu.findViewById(R.id.tvMenuSettings);
        TextView tvMenuClose = floatMenu.findViewById(R.id.tvMenuClose);

        tvMenuSettings.setOnClickListener(v -> openSettings());
        tvMenuClose.setOnClickListener(v -> hideMenu());

        windowManager.addView(floatMenu, menuParams);
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                updateStatus("聆听中...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                updateStatus("处理中...");
            }

            @Override
            public void onError(int error) {
                isListening = false;
                updateStatus("AI");
                if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                    Toast.makeText(FloatWindowService.this, "语音识别错误: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    updateStatus("处理中...");
                    sendToAI(text);
                } else {
                    updateStatus("AI");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void handleSingleClick() {
        if (isListening) {
            stopListening();
        } else {
            startListening();
        }
    }

    private void startListening() {
        if (!isListening) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            
            speechRecognizer.startListening(intent);
            isListening = true;
        }
    }

    private void stopListening() {
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            updateStatus("AI");
        }
    }

    private void showMenu() {
        menuParams.x = params.x + 100;
        menuParams.y = params.y + 50;
        floatMenu.setVisibility(View.VISIBLE);
        windowManager.updateViewLayout(floatMenu, menuParams);
        isMenuVisible = true;
    }

    private void hideMenu() {
        floatMenu.setVisibility(View.GONE);
        windowManager.updateViewLayout(floatMenu, menuParams);
        isMenuVisible = false;
    }

    private void openSettings() {
        hideMenu();
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateStatus(String status) {
        TextView tvFloatStatus = floatView.findViewById(R.id.tvFloatStatus);
        if (tvFloatStatus != null) {
            tvFloatStatus.setText(status);
        }
    }

    private void sendToAI(String text) {
        String apiKey = prefs.getString("api_key", "");
        String model = prefs.getString("model", "qwen-flash");

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请先设置API密钥", Toast.LENGTH_SHORT).show();
            updateStatus("AI");
            return;
        }

        try {
            String jsonBody = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                model, text
            );

            RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody
            );

            Request request = new Request.Builder()
                .url("https://api.dmxapi.cn/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handler.post(() -> {
                        Toast.makeText(FloatWindowService.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        updateStatus("AI");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    handler.post(() -> {
                        updateStatus("AI");
                        if (response.isSuccessful()) {
                            String responseText = extractResponseText(responseBody);
                            Toast.makeText(FloatWindowService.this, responseText, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(FloatWindowService.this, "API错误: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            handler.post(() -> {
                Toast.makeText(this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                updateStatus("AI");
            });
        }
    }

    private String extractResponseText(String json) {
        try {
            int contentStart = json.indexOf("\"content\":\"") + 11;
            if (contentStart > 10) {
                int contentEnd = json.indexOf("\"", contentStart);
                if (contentEnd > contentStart) {
                    String content = json.substring(contentStart, contentEnd);
                    return content.replace("\\n", "\n").replace("\\\"", "\"");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatView != null) {
            windowManager.removeView(floatView);
        }
        if (floatMenu != null) {
            windowManager.removeView(floatMenu);
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}