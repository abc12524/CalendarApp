package com.example.calendarapp;

import android.content.Intent;
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
    private DatabaseHelper dbHelper;
    
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
        
        dbHelper = DatabaseHelper.getInstance(this);
        
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
        String cityName = getIntent().getStringExtra("cityName");
        if (temperature != null && !temperature.isEmpty()) {
            weatherTemp.setText((cityName != null ? cityName : "武汉") + " " + temperature);
            weatherTemp.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadExistingNote() {
        EditText editorTitle = findViewById(R.id.editorTitle);
        EditText editorTextarea = findViewById(R.id.editorTextarea);
        
        String dateKey = DATE_KEY_FORMAT.format(editingDate.getTime());
        Note note = dbHelper.getNote(dateKey);
        
        if (note != null && note.hasContent()) {
            editorTitle.setText(note.getTitle());
            editorTextarea.setText(note.getContent());
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
        
        dbHelper.saveNote(dateKey, title, content);
        
        if (content.isEmpty() && title.isEmpty()) {
            Toast.makeText(this, "内容已清除", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "内容已保存", Toast.LENGTH_SHORT).show();
        }
        
        // 设置结果，通知MainActivity需要更新
        setResult(RESULT_OK);
        finish();
    }
}
