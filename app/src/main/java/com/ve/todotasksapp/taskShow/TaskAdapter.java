package com.ve.todotasksapp.taskShow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ve.todotasksapp.R;
import com.ve.todotasksapp.taskCreation.TaskAlarmCreator;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> implements Filterable {
    AlertDialog dialog;
    ArrayList<Task> taskList;
    ArrayList<Task> taskListCopy;
    Context context;
    TaskAdapter(Context context, ArrayList<Task> taskList)
    {
        this.context=context;
        this.taskList=taskList;
        this.taskListCopy=taskList;
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.task_list,parent,false);
        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {

        //update task details
        holder.taskName.setText(taskList.get(position).getName());
        holder.taskTime.setText(taskList.get(position).getTime());
        holder.taskPos.setText((position+1)+".");
        int currentLike=taskList.get(position).getTasklikeablity();

        //based on likablity each task gets a different color when displayed
        if(currentLike==0)
        {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.HighStressColor));
        }
        else if(currentLike==1)
        {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.mediumStressColor));
        }
        else
        {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.lowStressColor));
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @Override
    public Filter getFilter() {
        return taskFilter;
    }

    //filter to search a task by its name
    Filter taskFilter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String search=constraint.toString();
            ArrayList<Task> filteredList=new ArrayList<Task>();
            if(search==null || search.length()==0)
                filteredList=taskListCopy;
            else
            {
                for(Task t:taskListCopy) {
                    if (t.Name.toLowerCase().startsWith(search.toLowerCase())) {
                        filteredList.add(t);
                    }
                }

            }
            FilterResults results=new FilterResults();
            results.values=filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
                taskList=new ArrayList<Task>();
                taskList.addAll((ArrayList<Task>)results.values);
                notifyDataSetChanged();
        }
    };

    public class TaskHolder extends RecyclerView.ViewHolder {
        TextView taskTime;
        TextView taskName;
        TextView taskPos;
        public TaskHolder(@NonNull View itemView) {
            super(itemView);
            taskTime=(TextView) itemView.findViewById(R.id.task_time);
            taskName=(TextView) itemView.findViewById(R.id.task_name);
            taskPos=(TextView)itemView.findViewById(R.id.task_position);

            //click enables to update task
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)context).upDateTask(taskList.get(getAdapterPosition()));
                }
            });

            //long click deletes selected task
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    buildAlertDialog(taskList.get(getAdapterPosition()));
                    return true;
                }
            });
        }
    }

    private void buildAlertDialog(final Task taskToDelete) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=(View)inflater.inflate(R.layout.alert_dialog_delete,null);
        alertDialogBuilder.setView(view);

        //show a dialog prompting the  user to delete
        view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //delete the alarm
                deleteRow(taskToDelete);

                //cancel the dialog
                dialog.cancel();

                //update the adapter
                notifyDataSetChanged();

            }
        });
        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.setCancelable(false);
        dialog=alertDialogBuilder.create();
        dialog.show();
    }

    private void deleteRow(Task taskToDelete) {

        //delete alarm from database
        ((MainActivity)context).helper.deleteTask(taskToDelete.getTaskId());

        //cancel the alarm from alarm manager
        TaskAlarmCreator.cancelAlarm(context,taskToDelete.getTaskId());

        //remove them from the list
        taskList.remove(taskToDelete);

        //display a toast to user
        Toast.makeText(context,"task deleted sucessfully",Toast.LENGTH_LONG).show();
    }
}
