package com.egguard.egguardbackend.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private static final String PUBLIC_ID_BASE = "egguard/notifications/";
    /**
     * Uploads an image to Cloudinary
     * 
     * @param file the image file to upload
     * @return the URL of the uploaded image
     * @throws IOException if there's an error uploading the file
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            String publicId =  PUBLIC_ID_BASE + UUID.randomUUID();
            
            // Upload parameters
            Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "image"
            );
            
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully to: {}", imageUrl);
            
            return imageUrl;
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw e;
        }
    }
} 