package com.sendajapan.sendasnap.data.dto;

import com.google.gson.annotations.SerializedName;

public class AttachmentDto {
    @SerializedName("id")
    private String id;

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("file_path")
    private String filePath;

    @SerializedName("file_type")
    private String fileType;

    @SerializedName("file_size")
    private Long fileSize;

    @SerializedName("file_url")
    private String fileUrl;

    public AttachmentDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}

