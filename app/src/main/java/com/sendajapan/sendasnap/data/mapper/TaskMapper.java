package com.sendajapan.sendasnap.data.mapper;

import com.sendajapan.sendasnap.data.dto.TaskDto;
import com.sendajapan.sendasnap.models.Task;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskMapper {

    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_FORMAT_SIMPLE = "yyyy-MM-dd";
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat(DATE_FORMAT_ISO, Locale.getDefault());
    private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat(DATE_FORMAT_SIMPLE, Locale.getDefault());

    public static Task toDomain(TaskDto dto) {
        if (dto == null) {
            return null;
        }

        Task task = new Task();
        if (dto.getId() != null) {
            task.setId(dto.getId());
        }
        task.setTitle(normalizeString(dto.getTitle()));
        task.setDescription(normalizeString(dto.getDescription()));
        
        // Set status from string
        if (dto.getStatus() != null) {
            task.setStatus(parseStatus(dto.getStatus()));
        }
        
        // Set priority from string
        if (dto.getPriority() != null) {
            task.setPriority(parsePriority(dto.getPriority()));
        }
        
        task.setWorkDate(parseDate(dto.getWorkDate()));
        task.setWorkTime(normalizeString(dto.getWorkTime()));
        task.setDueDate(parseDate(dto.getDueDate()));
        task.setCreator(UserMapper.toDomain(dto.getCreator()));
        task.setAssignees(UserMapper.toDomainList(dto.getAssignedUsers()));
        task.setAttachments(AttachmentMapper.toDomainList(dto.getAttachments()));
        task.setCreatedAt(normalizeString(dto.getCreatedAt()));
        task.setUpdatedAt(normalizeString(dto.getUpdatedAt()));
        return task;
    }

    public static List<Task> toDomainList(List<TaskDto> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();
        for (TaskDto dto : dtos) {
            Task task = toDomain(dto);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    private static Task.TaskStatus parseStatus(String statusStr) {
        if (statusStr == null) {
            return Task.TaskStatus.PENDING;
        }
        switch (statusStr.toLowerCase()) {
            case "running":
                return Task.TaskStatus.RUNNING;
            case "pending":
                return Task.TaskStatus.PENDING;
            case "completed":
                return Task.TaskStatus.COMPLETED;
            case "cancelled":
                return Task.TaskStatus.CANCELLED;
            default:
                return Task.TaskStatus.PENDING;
        }
    }

    private static Task.TaskPriority parsePriority(String priorityStr) {
        if (priorityStr == null) {
            return Task.TaskPriority.NORMAL;
        }
        switch (priorityStr.toLowerCase()) {
            case "low":
                return Task.TaskPriority.LOW;
            case "medium":
                return Task.TaskPriority.NORMAL;
            case "high":
            case "urgent":
                return Task.TaskPriority.HIGH;
            default:
                return Task.TaskPriority.NORMAL;
        }
    }

    private static String normalizeString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static String parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        String trimmed = dateString.trim();
        
        // If it's already in YYYY-MM-DD format, return as is
        if (trimmed.length() >= 10 && trimmed.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return trimmed.substring(0, 10);
        }

        // Try to parse ISO format (YYYY-MM-DDTHH:mm:ss...)
        if (trimmed.contains("T")) {
            try {
                Date date = ISO_FORMAT.parse(trimmed);
                return SIMPLE_FORMAT.format(date);
            } catch (ParseException e) {
                // Try to extract just the date part
                if (trimmed.length() >= 10) {
                    return trimmed.substring(0, 10);
                }
            }
        }

        // Try simple format
        try {
            Date date = SIMPLE_FORMAT.parse(trimmed);
            return SIMPLE_FORMAT.format(date);
        } catch (ParseException e) {
            // Return null if can't parse
            return null;
        }
    }
}

