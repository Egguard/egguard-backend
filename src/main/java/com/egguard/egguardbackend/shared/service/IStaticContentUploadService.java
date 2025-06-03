package com.egguard.egguardbackend.shared.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IStaticContentUploadService {
    public String uploadImage(MultipartFile file) throws IOException;
}
