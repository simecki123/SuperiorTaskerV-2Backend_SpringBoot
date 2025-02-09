package com.example.demo.service.impl;

import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.models.dao.Project;
import com.example.demo.models.dao.Task;
import com.example.demo.models.enums.TaskStatus;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.service.ProjectTaskCommonService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ProjectCommonServiceImpl implements ProjectTaskCommonService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    /**
     * Method to update Project completion.
     * THis method is used every time new task is created, task status is changed or if task is deleted.
     * @param id project id
     */
    @Override
    public void updateProjectCompletion(String id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NoProjectFoundException("No project with associated id"));
        log.info("Fetching project ");

        List<Task> taskList = taskRepository.findAllByProjectId(id);
        log.info("returning task list ");
        double completionSum = 0.00;
        for(Task task : taskList) {
            if(task.getStatus().equals(TaskStatus.COMPLETED)){
                completionSum +=1;
            }
        }

        double completion = round((completionSum / taskList.size()) * 100);


        project.setCompletion(completion);

        projectRepository.save(project);
    }

    /**
     * helper method to round completion decimal to 2 decimal points...
     * @param value value that needs rounding.
     * @return new value
     */
    private double round(double value) {

        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
