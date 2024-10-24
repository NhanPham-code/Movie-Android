package com.example.ojtbadaassignment14;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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

        // get reminder list from SQLite
        getReminderList();

        // set up recycler view
        rvAllReminder.setLayoutManager(new LinearLayoutManager(this));
        allReminderAdapter = new AllReminderAdapter(reminderList, databaseHelper);
        rvAllReminder.setAdapter(allReminderAdapter);

        // Register receiver to update reminder list after notification is push
        IntentFilter filter = new IntentFilter("com.example.ojtbadaassignment14.UPDATE_REMINDER_LIST_FRAGMENT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(updateReminderListFragmentReceiver, filter, Context.RECEIVER_EXPORTED);
        }

    }

    private void init() {
        reminderList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);
        rvAllReminder = findViewById(R.id.rv_all_reminder);
    }

    /**
     * Get reminder list from SQLite
     */
    private void getReminderList() {
        reminderList = databaseHelper.getAllReminders();
    }

    /**
     * Receiver to update reminder list in recycler view after notification is pushed in AllReminderActivity
     */
    private final BroadcastReceiver updateReminderListFragmentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // update reminder list in recycler view by get new list from SQLite and set adapter
            getReminderList();
            allReminderAdapter = new AllReminderAdapter(reminderList, databaseHelper);
            rvAllReminder.setAdapter(allReminderAdapter);
        }

    };

}