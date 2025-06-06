package com.CS2D.taskyapp.misc

import android.content.Context
import androidx.room.Room
import com.CS2D.taskyapp.room.TodoDatabase
import com.CS2D.taskyapp.screens.scheduleNotification
import com.CS2D.taskyapp.screens.setRepeatingAlarm
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Recursor {

    fun rescheduleTasks(context: Context){
        val todoDatabase = Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "todo_database"
        ).build()

        val todoList = todoDatabase.todoDao().getAllTodos()

        todoList.observeForever {todos->
            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            todos.forEach {todo->
                if(!todo.isCompleted && (todo.date!!.isNotEmpty() && todo.time!!.isNotEmpty())){
                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val parsedDate = format.parse(todo.date!!)
                    val calendar = Calendar.getInstance().apply {
                        time = parsedDate!!
                        set(Calendar.HOUR_OF_DAY, todo.time!!.substringBefore(":").toInt())
                        set(Calendar.MINUTE, todo.time!!.substringAfter(":").toInt())
                        set(Calendar.SECOND, 0)
                    }
                    val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val parsedTime = timeFormat.parse("${todo.date} ${todo.time}")
                    val notificationTime = Calendar.getInstance().apply {
                        time = parsedTime!!
                    }
                    val currentTime = Calendar.getInstance().timeInMillis
                    if(calendar >= currentDate && notificationTime.timeInMillis >= currentTime){
                        scheduleNotification(
                            context = context,
                            titleText = todo.title,
                            messageText = todo.todoDescription,
                            time = "${todo.date} ${todo.time}",
                            todo = todo
                        )
                        println("Todo ${todo.title} set!")
                    }
                }
            }
        }
        setRepeatingAlarm(context = context)
        todoList.removeObserver{}
    }

}