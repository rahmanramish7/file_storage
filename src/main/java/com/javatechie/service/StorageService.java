package com.javatechie.service;

import com.javatechie.entity.FileData;
import com.javatechie.entity.ImageData;
import com.javatechie.repository.FileDataRepository;
import com.javatechie.repository.StorageRepository;
import com.javatechie.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Service
public class StorageService {
    @Autowired
    private FileDataRepository fileDataRepository;
    @Autowired
    private StorageRepository repository;

    public String uploadImage(MultipartFile file) throws IOException {
        ImageData imageData = repository.save(ImageData.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .imageData(ImageUtils.compressImage(file.getBytes()))
                .build()
        );

        if (imageData != null) {
            return "file uploaded succesfully" + file.getOriginalFilename();
        }
        return null;
    }

    public byte[] downloadImage(String fileName) {
        Optional<ImageData> dbImageData = repository.findByName(fileName);
        byte[] images = ImageUtils.decompressImage(dbImageData.get().getImageData());
        return images;

    }

    private final String FOLDER_PATH = "C:/Users/rahma/OneDrive/Desktop/Myfile";


    public String uploadImageToFileSystem(MultipartFile file) throws IOException {

        String filepath = FOLDER_PATH + "/" + file.getOriginalFilename();
        FileData fileData = fileDataRepository.save(FileData.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .filePath(filepath).build()
        );
        file.transferTo(new File(filepath));

        if (fileData != null) {
            return "file Uploaded Sucessfully" + filepath;
        }
        return null;
    }


    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<FileData> fileData = fileDataRepository.findByName(fileName);
        String filePath = fileData.get().getFilePath();
        byte[] images = Files.readAllBytes(new File(filePath).toPath());
        return images;

    }


    public String deleteImageFromFileSystem(String fileName) throws IOException {

        Optional<FileData> fileData = fileDataRepository.findByName(fileName);

        if (fileData.isPresent()) {

            String filePath = fileData.get().getFilePath();

            // Delete physical file
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            // Delete DB record
            fileDataRepository.delete(fileData.get());

            return "File deleted successfully: " + fileName;
        }

        return "File not found: " + fileName;
    }


}



