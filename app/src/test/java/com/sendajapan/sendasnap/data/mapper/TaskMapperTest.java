package com.sendajapan.sendasnap.data.mapper;

import com.sendajapan.sendasnap.data.dto.TaskDto;
import com.sendajapan.sendasnap.models.Task;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskMapperTest {

    @Test
    public void testToDomain_withValidDto() {
        TaskDto dto = new TaskDto();
        dto.setId(1);
        dto.setTitle("Test Task");
        dto.setDescription("Test Description");
        dto.setStatus("pending");
        dto.setPriority("medium");
        dto.setWorkDate("2025-01-15");
        dto.setWorkTime("10:00");

        Task task = TaskMapper.toDomain(dto);

        assertNotNull(task);
        assertEquals(1, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(Task.TaskStatus.PENDING, task.getStatus());
        assertEquals(Task.TaskPriority.NORMAL, task.getPriority());
        assertEquals("2025-01-15", task.getWorkDate());
        assertEquals("10:00", task.getWorkTime());
    }

    @Test
    public void testToDomain_withNullDto() {
        Task task = TaskMapper.toDomain(null);
        assertNull(task);
    }

    @Test
    public void testToDomain_withEmptyStrings() {
        TaskDto dto = new TaskDto();
        dto.setId(1);
        dto.setTitle("   ");
        dto.setDescription("");
        dto.setStatus("running");

        Task task = TaskMapper.toDomain(dto);

        assertNotNull(task);
        assertNull(task.getTitle());
        assertNull(task.getDescription());
        assertEquals(Task.TaskStatus.RUNNING, task.getStatus());
    }

    @Test
    public void testToDomain_withISODate() {
        TaskDto dto = new TaskDto();
        dto.setId(1);
        dto.setTitle("Test");
        dto.setWorkDate("2025-01-15T10:30:00.000000Z");

        Task task = TaskMapper.toDomain(dto);

        assertNotNull(task);
        assertEquals("2025-01-15", task.getWorkDate());
    }

    @Test
    public void testToDomainList_withNullList() {
        java.util.List<Task> tasks = TaskMapper.toDomainList(null);
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
}

