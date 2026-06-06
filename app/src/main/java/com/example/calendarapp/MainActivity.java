package com.example.calendarapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    
    private Calendar currentDate;
    private Calendar selectedDate;
    private Calendar editingDate;
    private GridLayout calendarGrid;
    private TextView currentDayText;
    private TextView currentDateText;
    private TextView displayMonthText;
    private SharedPreferences sharedPreferences;
    private Map<String, String> notesMap;
    private Map<String, String> weatherCache;
    private boolean weatherFetchInProgress = false;
    private GestureDetector gestureDetector;
    private TextView cityNameText;
    private String currentCityUrl = "湖北/天门";
    private String currentCityDisplay = "天门";
    
    private static final String PREFS_NAME = "CalendarNotes";
    private static final String PREF_CITY_URL = "weather_city_url";
    private static final String PREF_CITY_NAME = "weather_city_name";
    private static final SimpleDateFormat DATE_KEY_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault());
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
    
    // 农历计算相关
    private static final int[] lunarInfo = {
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
    };
    
    private static final String[] lunarMonths = {
        "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"
    };
    
    private static final String[] lunarDays = {
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeData();
        setupEventListeners();
        setupGestureDetector();
        renderCalendar();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载笔记并更新日历显示
        loadNotes();
        renderCalendar();
    }
    
    private void initializeViews() {
        calendarGrid = findViewById(R.id.calendarGrid);
        currentDayText = findViewById(R.id.currentDay);
        currentDateText = findViewById(R.id.currentDate);
        displayMonthText = findViewById(R.id.displayMonth);
        cityNameText = findViewById(R.id.cityName);
    }
    
    private void initializeData() {
        currentDate = Calendar.getInstance();
        selectedDate = Calendar.getInstance();
        editingDate = Calendar.getInstance();
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        notesMap = new HashMap<>();
        weatherCache = new HashMap<>();
        
        // 加载保存的笔记
        loadNotes();
        // 加载缓存的天气数据
        loadWeatherCache();
        // 加载保存的城市
        currentCityUrl = sharedPreferences.getString(PREF_CITY_URL, "湖北/天门");
        currentCityDisplay = sharedPreferences.getString(PREF_CITY_NAME, "天门");
    }
    
    private void setupEventListeners() {
        Button prevMonthBtn = findViewById(R.id.prevMonthBtn);
        Button nextMonthBtn = findViewById(R.id.nextMonthBtn);
        Button todayBtn = findViewById(R.id.todayBtn);
        
        prevMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevMonth();
            }
        });
        
        nextMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonth();
            }
        });
        
        todayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToToday();
            }
        });
        
        cityNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityPickerDialog();
            }
        });
    }
    
    private void loadNotes() {
        // 清空现有的笔记映射
        notesMap.clear();
        
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof String) {
                notesMap.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }
    
    private void saveNote(String dateKey, String content) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (content.trim().isEmpty()) {
            editor.remove(dateKey);
            notesMap.remove(dateKey);
        } else {
            editor.putString(dateKey, content);
            notesMap.put(dateKey, content);
        }
        editor.apply();
    }
    
    private String getNoteForDate(Calendar date) {
        String dateKey = DATE_KEY_FORMAT.format(date.getTime());
        String noteData = notesMap.get(dateKey);
        
        if (noteData != null) {
            // 解析标题和内容，检查是否有实际内容
            String[] parts = noteData.split("\\|", 2);
            if (parts.length >= 2) {
                String title = parts[0].trim();
                String content = parts[1].trim();
                // 如果标题和内容都为空，则返回null
                if (title.isEmpty() && content.isEmpty()) {
                    return null;
                }
                return noteData;
            } else {
                // 旧格式，直接检查内容
                if (noteData.trim().isEmpty()) {
                    return null;
                }
                return noteData;
            }
        }
        return null;
    }
    
    private void renderCalendar() {
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        
        // 更新显示
        displayMonthText.setText(MONTH_FORMAT.format(currentDate.getTime()));
        currentDayText.setText(String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH)));
        currentDateText.setText(DISPLAY_DATE_FORMAT.format(selectedDate.getTime()));
        cityNameText.setText(currentCityDisplay + "  ▾");
        
        // 清空日历网格
        calendarGrid.removeAllViews();
        
        // 获取月份的第一天和最后一天
        Calendar firstDay = (Calendar) currentDate.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        Calendar lastDay = (Calendar) currentDate.clone();
        lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        
        int daysInMonth = lastDay.get(Calendar.DAY_OF_MONTH);
        int startingDay = firstDay.get(Calendar.DAY_OF_WEEK);
        
        // 添加空白单元格来对齐星期
        for (int i = 1; i < startingDay; i++) {
            addEmptyDay();
        }
        
        // 添加当前月的日期
        Calendar today = Calendar.getInstance();
        for (int day = 1; day <= daysInMonth; day++) {
            Calendar date = (Calendar) currentDate.clone();
            date.set(Calendar.DAY_OF_MONTH, day);
            addDay(day, date, isToday(date), isSelected(date));
        }
        
        // 添加剩余的空白单元格来保持网格完整
        int totalCells = 42; // 6行 * 7列
        int remainingCells = totalCells - (startingDay - 1 + daysInMonth);
        for (int i = 0; i < remainingCells; i++) {
            addEmptyDay();
        }
        
        // 如果当前月份包含今天，触发天气获取
        triggerWeatherFetch();
    }
    
    private void addEmptyDay() {
        TextView emptyView = new TextView(this);
        emptyView.setVisibility(View.INVISIBLE);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setGravity(Gravity.FILL);
        emptyView.setLayoutParams(params);
        calendarGrid.addView(emptyView);
    }
    
    private void addDay(int day, final Calendar date, boolean isToday, boolean isSelected) {
        // 使用布局文件创建日期单元格
        View dayCell = getLayoutInflater().inflate(R.layout.day_cell, calendarGrid, false);
        TextView dayNumber = dayCell.findViewById(R.id.dayNumber);
        TextView weatherText = dayCell.findViewById(R.id.weatherText);
        ImageView contentDot = dayCell.findViewById(R.id.contentDot);
        
        dayNumber.setText(String.valueOf(day));
        
        // 设置布局参数
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setGravity(Gravity.FILL);
        dayCell.setLayoutParams(params);
        
        // 设置背景和文字颜色
        if (isToday) {
            dayCell.setBackgroundResource(R.drawable.today_background);
            dayNumber.setTextColor(Color.WHITE);
            
            // 显示天气信息（只取 emoji 部分）
            String dateKey = DATE_KEY_FORMAT.format(date.getTime());
            String weather = weatherCache.get(dateKey);
            if (weather != null && !weather.isEmpty()) {
                String[] parts = weather.split("\\|");
                String emoji = parts[0].trim();
                if (!emoji.isEmpty()) {
                    weatherText.setText(emoji);
                    weatherText.setVisibility(View.VISIBLE);
                    weatherText.setTextColor(Color.argb(200, 255, 255, 255));
                }
            }
        } else if (isSelected) {
            dayCell.setBackgroundResource(R.drawable.selected_background);
            dayNumber.setTextColor(Color.WHITE);
        } else {
            dayCell.setBackgroundResource(R.drawable.day_background);
            dayNumber.setTextColor(getResources().getColor(R.color.dark_gray));
        }
        
        // 检查是否有笔记内容，显示标记点
        String note = getNoteForDate(date);
        if (note != null && !note.trim().isEmpty()) {
            contentDot.setVisibility(View.VISIBLE);
        }
        
        // 设置点击事件
        dayCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate(date);
            }
        });
        
        calendarGrid.addView(dayCell);
    }
    
    private boolean isToday(Calendar date) {
        Calendar today = Calendar.getInstance();
        return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
               date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }
    
    private boolean isSelected(Calendar date) {
        return date.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
               date.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
               date.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH);
    }
    
    private void selectDate(Calendar date) {
        selectedDate = (Calendar) date.clone();
        renderCalendar();
        openEditor(date);
    }
    
    // 农历计算方法
    private String getLunarDate(Calendar date) {
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH) + 1;
        int day = date.get(Calendar.DAY_OF_MONTH);
        
        // 简化版农历计算 - 返回固定格式
        // 在实际应用中，这里应该实现完整的农历计算逻辑
        return lunarMonths[(month - 1) % 12] + lunarDays[(day - 1) % 30];
    }
    
    private void openEditor(final Calendar date) {
        // 启动全屏编辑Activity
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra("date", date.getTimeInMillis());
        // 传递天气温度
        String dateKey = DATE_KEY_FORMAT.format(date.getTime());
        String weather = weatherCache.get(dateKey);
        if (weather != null && !weather.isEmpty()) {
            String[] parts = weather.split("\\|");
            if (parts.length >= 2) {
                intent.putExtra("temperature", parts[1].trim());
            }
        }
        // 传递城市名
        intent.putExtra("cityName", currentCityDisplay);
        startActivity(intent);
    }
    
    private void goToToday() {
        Calendar today = Calendar.getInstance();
        currentDate = (Calendar) today.clone();
        selectedDate = (Calendar) today.clone();
        renderCalendar();
    }
    
    // ========== 城市切换 ==========
    
    private void showCityPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("切换城市");
        
        final EditText input = new EditText(this);
        input.setHint("输入城市名，如: 北京、上海、武汉");
        input.setText(currentCityDisplay);
        input.setSelectAllOnFocus(true);
        input.setPadding(40, 20, 40, 20);
        builder.setView(input);
        
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newCity = input.getText().toString().trim();
                if (!newCity.isEmpty()) {
                    changeCity(newCity);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    private void changeCity(String newCityName) {
        currentCityDisplay = newCityName;
        currentCityUrl = newCityName;
        
        // 保存城市到 SharedPreferences
        sharedPreferences.edit()
            .putString(PREF_CITY_URL, currentCityUrl)
            .putString(PREF_CITY_NAME, currentCityDisplay)
            .apply();
        
        // 清除今天的天气缓存，下次自动重新获取
        String todayKey = DATE_KEY_FORMAT.format(Calendar.getInstance().getTime());
        String cacheKey = "weather_" + todayKey;
        sharedPreferences.edit().remove(cacheKey).apply();
        weatherCache.remove(todayKey);
        
        // 重新获取天气
        weatherFetchInProgress = false;
        renderCalendar();
    }
    
    // ========== 天气相关方法 ==========
    
    /**
     * 从SharedPreferences加载缓存的天气数据
     */
    private void loadWeatherCache() {
        weatherCache.clear();
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("weather_") && entry.getValue() instanceof String) {
                String dateKey = entry.getKey().substring("weather_".length());
                weatherCache.put(dateKey, (String) entry.getValue());
            }
        }
    }
    
    /**
     * 判断今天是否在当前显示的月份中
     */
    private boolean isTodayInCurrentMonth() {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
               today.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH);
    }
    
    /**
     * 触发天气获取：如果今天在当前月份且未缓存，异步拉取
     */
    private void triggerWeatherFetch() {
        if (!isTodayInCurrentMonth() || weatherFetchInProgress) {
            return;
        }
        
        Calendar today = Calendar.getInstance();
        String dateKey = DATE_KEY_FORMAT.format(today.getTime());
        
        // 缓存中已有，不用重复获取
        if (weatherCache.containsKey(dateKey)) {
            return;
        }
        
        weatherFetchInProgress = true;
        new FetchWeatherTask().execute(dateKey);
    }
    
    /**
     * 异步获取天气任务
     */
    private class FetchWeatherTask extends AsyncTask<String, Void, String> {
        private String dateKey;
        
        @Override
        protected String doInBackground(String... params) {
            dateKey = params[0];
            try {
                String encodedCity = URLEncoder.encode(currentCityUrl, "UTF-8").replace("%2F", "/");
                URL url = new URL("https://wttr.in/" + encodedCity + "?format=%25c|%25t");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Accept", "text/plain");
                
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    return null;
                }
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8")
                );
                String result = reader.readLine();
                reader.close();
                conn.disconnect();
                
                if (result != null) {
                    result = result.trim();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(String weather) {
            if (weather != null && !weather.isEmpty()) {
                // 保存到缓存和SharedPreferences
                String cacheKey = "weather_" + dateKey;
                sharedPreferences.edit().putString(cacheKey, weather).apply();
                weatherCache.put(dateKey, weather);
                // 重新渲染以显示天气
                renderCalendar();
            }
            weatherFetchInProgress = false;
        }
        
        @Override
        protected void onCancelled() {
            weatherFetchInProgress = false;
        }
    }
    
    // ========== 天气方法结束 ==========
    
    private void prevMonth() {
        currentDate.add(Calendar.MONTH, -1);
        renderCalendar();
    }
    
    private void nextMonth() {
        currentDate.add(Calendar.MONTH, 1);
        renderCalendar();
    }
    
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // 水平滑动
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // 向右滑动 - 上个月
                            prevMonth();
                        } else {
                            // 向左滑动 - 下个月
                            nextMonth();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        
        // 设置触摸监听器
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}
