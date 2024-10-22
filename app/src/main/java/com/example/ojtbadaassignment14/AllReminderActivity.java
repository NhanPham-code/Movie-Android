package com.example.ojtbadaassignment14;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ojtbadaassignment14.adapters.AllReminderAdapter;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.models.Reminder;

import java.util.ArrayList;
import java.util.List;

public class AllReminderActivity extends AppCompatActivity {

    List<Reminder> reminderList;
    DatabaseHelper databaseHelper;

    RecyclerView rvAllReminder;
    AllReminderAdapter allReminderAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reminder);

        init();

        getReminderList();

        rvAllReminder.setLayoutManager(new LinearLayoutManager(this));
        allReminderAdapter = new AllReminderAdapter(reminderList, databaseHelper);
        rvAllReminder.setAdapter(allReminderAdapter);

    }

    private void init() {
        reminderList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);
        rvAllReminder = findViewById(R.id.rv_all_reminder);
    }

    private void getReminderList() {
        reminderList = databaseHelper.getAllReminders();
    }
}