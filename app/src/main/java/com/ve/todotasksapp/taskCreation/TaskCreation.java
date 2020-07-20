package com.ve.todotasksapp.taskCreation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ve.todotasksapp.DBHelper;
import com.ve.todotasksapp.R;
import com.ve.todotasksapp.taskShow.Task;


//this class is used by both cration and updation
public class TaskCreation extends AppCompatActivity implements View.OnClickListener {

    private static final String TASK_PREFERENCE = "TASK_UNIQUE_ID";
    EditText taskNameTxt;
    SeekBar likablityMeasure;
    ImageView taskTimeImg;
    Task newTask;
    TextView taskTimeTxt;
    Button taskScheduleBtn;
    Toolbar toolbar;
    DBHelper helper;
    ProgressBar pb;
    TextView progressText;
    TaskAlarmCreator creator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_creation);

        //get view reference and enable them for selection
        wireUpListeners();

        //get previously specified time when device is rotated and activity is recreated
        if(savedInstanceState!=null)
            taskTimeTxt.setText(savedInstanceState.getString("selected_time"));

        //check if it is an update or create task
        Task taskToUpdate=(Task)getIntent().getSerializableExtra("task");
        if(taskToUpdate!=null) {
            newTask = taskToUpdate;
            fillWidgetsWithData();
        }
        else
            newTask = new Task();

        //taskAlarmCreator deals with all specs in creating an alarm
        creator=new TaskAlarmCreator(this,newTask);

        //to store the alarm for later retrieval
        helper=new DBHelper(this);

    }

    //if update fill widgets with data
    private void fillWidgetsWithData() {
        taskNameTxt.setText(newTask.getName());
        taskTimeTxt.setText(newTask.getTime());
        likablityMeasure.setProgress(newTask.getTasklikeablity());
    }

    //all view reference are obtained and enabled for selection
    private void wireUpListeners() {
        taskNameTxt=(EditText)findViewById(R.id.task_name);
        taskTimeImg=(ImageView)findViewById(R.id.task_image_time);
        taskScheduleBtn=(Button)findViewById(R.id.task_schedule);
        taskTimeTxt=(TextView)findViewById(R.id.task_time);
        likablityMeasure=(SeekBar)findViewById(R.id.task_likablity);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Task Scheduling");
        taskTimeTxt.setOnClickListener(this);
        taskTimeImg.setOnClickListener(this);
        taskScheduleBtn.setOnClickListener(this);
        pb=(ProgressBar)findViewById(R.id.progress_bar);
        progressText=(TextView)findViewById(R.id.creating_alarm_label);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selected_time",taskTimeTxt.getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            //create alarm command
            case R.id.task_schedule:sheduleTask();
                                    break;

            //display a time dialog when clicked on time img and time text
            case R.id.task_time:
            case R.id.task_image_time:createTimeDialog();
                                    break;
        }
    }

    private void createTimeDialog() {
        TimePickerFragment fragment=new TimePickerFragment();
        fragment.setTime(taskTimeTxt);
        fragment.show(getSupportFragmentManager(),"time_dialog");
    }


    private void sheduleTask() {
        //display a progress bar and a text
        pb.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        //check for validity
        if(validateTask())
        {

            //create an alarm
            creator.createAlarm();

            //insert it into the db
            if(helper.insertTask(newTask))
            {

                //display text
                Toast.makeText(this,"task scheduled successfully",Toast.LENGTH_LONG).show();
                finishAcitivity();
            }
        }
        pb.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);

    }

    private void finishAcitivity() {
        Intent intent=new Intent();
        intent.putExtra("new_task",newTask);
        setResult(RESULT_OK,intent);
        finish();
    }

    //check all fields are entered with correct data
    private boolean validateTask() {
        if(taskNameTxt.getText().toString().length()==0 || taskNameTxt.getText().toString().trim().length()==0)
        {
            taskNameTxt.setError("please enter a valid taskName");
            return false;
        }
        if(taskTimeTxt.getText().toString().length()==0)
        {
            taskTimeTxt.setError("please enter a valid time");
            return false;
        }
        newTask.setTaskId(getUniqueTaskId());
        newTask.setName(taskNameTxt.getText().toString());
        newTask.setTime(taskTimeTxt.getText().toString());
        newTask.setTasklikeablity(likablityMeasure.getProgress());
        return true;
    }


    //each task is given a uniqueId for later deletion and cancel if needed
    private int getUniqueTaskId() {

        //we retrived previously assigned value from app's sandBox
       SharedPreferences shared=getSharedPreferences(TASK_PREFERENCE, Context.MODE_PRIVATE);

       int val=shared.getInt("taskUniqueId",0);
       SharedPreferences.Editor edit=shared.edit();

       //add one to it for creating current alarm
       edit.putInt("taskUniqueId",val+1);
       edit.commit();
       return val;
    }

}
