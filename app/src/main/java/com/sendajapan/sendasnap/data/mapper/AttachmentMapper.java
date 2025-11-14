package com.sendajapan.sendasnap.data.mapper;

import com.sendajapan.sendasnap.data.dto.AttachmentDto;
import com.sendajapan.sendasnap.models.TaskAttachment;
import java.util.ArrayList;
import java.util.List;

public class AttachmentMapper {

    public static TaskAttachment toDomain(AttachmentDto dto) {
        if (dto == null) {
            return null;
        }

        TaskAttachment attachment = new TaskAttachment();
        attachment.setId(normalizeString(dto.getId()));
        attachment.setFileName(normalizeString(dto.getFileName()));
        attachment.setFilePath(normalizeString(dto.getFilePath()));
        attachment.setFileType(normalizeString(dto.getFileType()));
        attachment.setFileUrl(normalizeString(dto.getFileUrl()));
        if (dto.getFileSize() != null) {
            attachment.setFileSize(dto.getFileSize());
        } else {
            attachment.setFileSize(0);
        }
        return attachment;
    }

    public static List<TaskAttachment> toDomainList(List<AttachmentDto> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<TaskAttachment> attachments = new ArrayList<>();
        for (AttachmentDto dto : dtos) {
            TaskAttachment attachment = toDomain(dto);
            if (attachment != null) {
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    private static String normalizeString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

