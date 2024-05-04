package com.nimesa.careers.multithreading_assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Processor {

    private final Queue<TaskRequest> queue = new LinkedBlockingQueue<>();


    Processor(TaskRequest taskRequest) {
        queue.offer(taskRequest);
    }

    Processor(List<TaskRequest> taskRequest) {
        for (TaskRequest request : taskRequest) {
            queue.offer(request);

        }
    }

    public List<TaskResponse> execute() {
//
//        /**
//         *Sequential execution
//         */
//        List<TaskResponse> taskResponses = new ArrayList<>();
//        for (TaskRequest taskRequest : queue) {
//            System.out.println("Starting Task "+taskRequest.getId());
//            Task task = new Task(taskRequest);
//            TaskResponse response = task.run();
//            System.out.println("Completed Task "+response.getId() +"With Status"+response.getStatus());
//            taskResponses.add(response);
//        }
//        return taskResponses;
//    }

        Map<String, Map<String, List<TaskRequest>>> groupedTasks = queue.stream()
                .collect(Collectors.groupingBy(TaskRequest::getId,
                        Collectors.groupingBy(request -> request.getType().toString())));

        List<TaskResponse> taskResponses = new ArrayList<>();

        groupedTasks.entrySet().parallelStream().forEach(userEntry -> userEntry.getValue().entrySet().parallelStream().forEach(taskTypeEntry -> {
            List<TaskRequest> tasks = taskTypeEntry.getValue();

            for (TaskRequest taskRequest : tasks) {
                System.out.println("Starting Task " + taskRequest.getId());
                Task task = new Task(taskRequest);
                TaskResponse response;
                try {
                    response = task.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Completed Task " + response.getId() + " With Status " + response.getStatus());
                synchronized (taskResponses) {
                    taskResponses.add(response);
                }
            }
        }));


        return taskResponses;
    }
}
