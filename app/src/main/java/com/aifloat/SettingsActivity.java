package com.aifloat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etApiKey;
    private Spinner spinnerModel;
    private Button btnSave;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("AIFloatPrefs", MODE_PRIVATE);

        etApiKey = findViewById(R.id.etApiKey);
        spinnerModel = findViewById(R.id.spinnerModel);
        btnSave = findViewById(R.id.btnSave);

        setupModelSpinner();
        loadSettings();

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void setupModelSpinner() {
        String[] models = new String[]{
            "qwen-flash",           // 阿里通义千问极速响应（免费）
            "qwen3-8B",             // 阿里通义千问8B（免费）
            "deepseek-v3",          // DeepSeek V3
            "hunyuan-t1-20250321",  // 腾讯混元
            "Doubao-1.5-pro-32k",   // 字节豆包
            "glm-4"                 // 智谱GLM-4
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            models
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(adapter);
    }

    private void loadSettings() {
        String apiKey = prefs.getString("api_key", "sk-UsgwFkmVN540vyqSCkAXFUOSOee36ciBync1sWz8vG91Bpg0");
        String model = prefs.getString("model", "qwen-flash");

        etApiKey.setText(apiKey);
        
        int position = ((ArrayAdapter<String>) spinnerModel.getAdapter()).getPosition(model);
        if (position >= 0) {
            spinnerModel.setSelection(position);
        }
    }

    private void saveSettings() {
        String apiKey = etApiKey.getText().toString().trim();
        String model = spinnerModel.getSelectedItem().toString();

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请输入API密钥", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("api_key", apiKey);
        editor.putString("model", model);
        editor.apply();

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        finish();
    }
}