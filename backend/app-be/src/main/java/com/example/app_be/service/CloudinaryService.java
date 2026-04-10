package com.example.app_be.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.app_be.config.CloudinaryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public Map upload(MultipartFile file) {
        try {
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "android-app/images/products",
                            "resource_type", "image")
            );
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + file.getOriginalFilename(), e);
        }
    }

    public String getUrl(String publicId, int width, int height) {
        return cloudinary.url()
                .transformation(new Transformation()
                        .width(width).height(height).crop("fill")
                )
                .generate(publicId);
    }

    public Map delete(String publicId) throws Exception {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.asMap());
    }

}
