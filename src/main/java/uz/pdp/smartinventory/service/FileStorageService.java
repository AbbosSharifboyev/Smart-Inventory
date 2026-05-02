package uz.pdp.smartinventory.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // Rasmlar saqlanadigan papka yo'li
    private final Path root = Paths.get("uploads");

    public String saveImage(MultipartFile file){
        try {
            // Agar uploads papkasi bo'lmasa, yaratamiz
            if (!Files.exists(root)){
                Files.createDirectories(root);
            }
            // Rasmni nomi bir xil bo'lib qolmasligi uchun UUID qo'shamiz
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(),this.root.resolve(fileName));

            return fileName; // Bazaga saqlash uchun rasm nomini qaytaramiz
        }catch (Exception e){
            throw new RuntimeException("Rasmni saqlashda xatolik: " + e.getMessage());
        }
    }

    public void deleteImage(String fileName){
        try {
            Path file = root.resolve(fileName);
            Files.deleteIfExists(file);
        }catch (IOException e){
            throw new RuntimeException("Faylni o'chirishda xatolik: " + e.getMessage());
        }
    }
}
