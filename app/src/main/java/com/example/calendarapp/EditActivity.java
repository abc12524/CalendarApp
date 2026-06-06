package com.example.calendarapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {
    
    private Calendar editingDate;
    private SharedPreferences sharedPreferences;
    
    private static final String PREFS_NAME = "CalendarNotes";
    private static final SimpleDateFormat DATE_KEY_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        
        // 获取传递的日期
        Intent intent = getIntent();
        long dateInMillis = intent.getLongExtra("date", System.currentTimeMillis());
        editingDate = Calendar.getInstance();
        editingDate.setTimeInMillis(dateInMillis);
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initializeViews();
        loadExistingNote();
        loadWeather();
    }
    
    private void initializeViews() {
        EditText editorTitle = findViewById(R.id.editorTitle);
        EditText editorTextarea = findViewById(R.id.editorTextarea);
        Button closeEditorBtn = findViewById(R.id.closeEditorBtn);
        Button cancelEditBtn = findViewById(R.id.cancelEditBtn);
        Button saveEditBtn = findViewById(R.id.saveEditBtn);
        
        closeEditorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        cancelEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        saveEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }
    
    private void loadWeather() {
        TextView weatherTemp = findViewById(R.id.weatherTemp);
        String temperature = getIntent().getStringExtra("temperature");
        if (temperature != null && !temperature.isEmpty()) {
            weatherTemp.setText(temperature);
            weatherTemp.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadExistingNote() {
        EditText editorTitle = findViewById(R.id.editorTitle);
        EditText editorTextarea = findViewById(R.id.editorTextarea);
        
        String dateKey = DATE_KEY_FORMAT.format(editingDate.getTime());
        String existingNote = sharedPreferences.getString(dateKey, null);
        
        if (existingNote != null) {
            // 解析标题和内容
            String[] parts = existingNote.split("\\|", 2);
            if (parts.length >= 2) {
                editorTitle.setText(parts[0]);
                editorTextarea.setText(parts[1]);
            } else {
                // 如果没有分隔符，说明是旧格式，全部作为内容
                editorTitle.setText("");
                editorTextarea.setText(existingNote);
            }
        } else {
            editorTitle.setText("");
            editorTextarea.setText("");
        }
    }
    
    private void saveNote() {
        EditText editorTitle = findViewById(R.id.editorTitle);
        EditText editorTextarea = findViewById(R.id.editorTextarea);
        
        String title = editorTitle.getText().toString().trim();
        String content = editorTextarea.getText().toString().trim();
        String dateKey = DATE_KEY_FORMAT.format(editingDate.getTime());
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (content.isEmpty() && title.isEmpty()) {
            // 如果标题和内容都为空，删除该日期的笔记
            editor.remove(dateKey);
            Toast.makeText(this, "内容已清除", Toast.LENGTH_SHORT).show();
        } else {
            // 保存标题和内容
            String noteData = title + "|" + content;
            editor.putString(dateKey, noteData);
            Toast.makeText(this, "内容已保存", Toast.LENGTH_SHORT).show();
        }
        editor.apply();
        
        // 设置结果，通知MainActivity需要更新
        setResult(RESULT_OK);
        finish();
    }
}
