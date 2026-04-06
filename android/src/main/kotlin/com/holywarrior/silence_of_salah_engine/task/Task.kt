package com.holywarrior.silence_of_salah_engine.task

import com.holywarrior.silence_of_salah_engine.foreground_service.*
import kotlinx.coroutines.delay

class Task : BaseForegroundTask<TaskStateController>() {

    override val loopIntervalMillis: Long = 3000

    override suspend fun onStart(
        taskController: ForegroundTaskController,
        notificationController: NotificationController,
        stateController: TaskStateController
    ) {
        stateController.counter = 0
        notificationController
            .setTitle("Example Task")
            .setText("Task started")
            .update()
    }

    override suspend fun onRecover(
        taskController: ForegroundTaskController,
        notificationController: NotificationController,
        stateController: TaskStateController
    ) {
        // Called when system killed/restarted service
        notificationController
            .setTitle("Example Task (Recovering)")
            .setText("Resuming task...")
            .update()
    }

    override suspend fun onLoop(
        taskController: ForegroundTaskController,
        notificationController: NotificationController,
        stateController: TaskStateController
    ) {
        stateController.counter++
        notificationController
            .setText("Loop count: ${stateController.counter}")
            .update()
    }

    override suspend fun onDestroy(
        taskController: ForegroundTaskController,
        notificationController: NotificationController,
        stateController: TaskStateController
    ) {
        notificationController
            .setText("Task finished at count ${stateController.counter}")
            .update()
    }
}

class TaskStateController {
    var counter: Int = 0
}