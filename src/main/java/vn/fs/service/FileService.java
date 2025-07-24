package vn.fs.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.fs.exception.BadRequestException;
import vn.fs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {
    public static final String UPLOAD_FOLDER = "image_uploads";

    public FileService() {
        FileUtils.createDirectory(UPLOAD_FOLDER);
    }


    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Không thể tải lên tệp rỗng");
        }

        // Validate file size, file type, file extension, ...

        try {
            // Tạo tên tệp duy nhất với UUID
            String fileName = UUID.randomUUID().toString();

            // Tạo đường dẫn lưu tệp (/image_uploads/fileName)
            Path path = Paths.get(UPLOAD_FOLDER + File.separator + fileName);

            // Lưu tệp
            Files.copy(file.getInputStream(), path);

            return File.separator + UPLOAD_FOLDER + File.separator + fileName; // /image_uploads/fileName
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể tải lên tệp");
        }
    }
    public void deleteFile(String filePath) {
        // Kiểm tra nếu đường dẫn là URL và bỏ qua nếu đúng
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            System.out.println("Skipping deletion of URL: " + filePath);
            return;
        }

        if (filePath.startsWith(File.separator)) {
            filePath = filePath.substring(1);
        }
        if (!filePath.startsWith(UPLOAD_FOLDER)) {
            filePath = UPLOAD_FOLDER + File.separator + filePath;
        }

        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            System.out.println("Xóa file: " + filePath);
        } catch (Exception e) {
            System.out.println("Lỗi khi xóa file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
