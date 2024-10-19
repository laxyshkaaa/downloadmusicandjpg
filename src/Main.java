import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

class Downloader {
    private static final Map<String, byte[]> FILE_SIGNATURES = new HashMap<>();

    static {
        FILE_SIGNATURES.put("mp3", new byte[]{(byte) 0xFF, (byte) 0xFB});
        FILE_SIGNATURES.put("mp3", new byte[]{(byte) 0x49, (byte) 0x44, (byte) 0x33}); // ID3
        FILE_SIGNATURES.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

    }

    public static void main(String[] args) {
        new Thread(() -> downloadFile("https://cdn16.deliciouspeaches.com/get/music/20190912/Yung_Trappa_feat_Baksh_-_Odna_noch_66538071.mp3", "anthem.mp3")).start();
        new Thread(() -> downloadFile("https://i.artfile.ru/2880x1800_729861_[www.ArtFile.ru].jpg", "cat_image.jpg")).start();
        new Thread(() -> downloadFile("https://download.samplelib.com/mp4/sample-5s.mp4", "sample_video.mp4")).start();
        new Thread(() -> downloadFile("https://fastfine.ru/storage/app/uploads/public/64f/094/2ed/64f0942edc6ac997802569.docx", "document.docx")).start();
    }

    private static void openFile(String filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", filePath);
            pb.start();
        } catch (IOException e) {
            System.out.println("Ошибка при открытии файла: " + e.getMessage());
        }
    }

    public static void downloadFile(String url, String fileName) {
        try {
            URLConnection connection = new URL(url).openConnection();
            try (InputStream input = connection.getInputStream();
                 ByteArrayOutputStream memoryBuffer = new ByteArrayOutputStream()) {

                byte[] header = new byte[3];  // Читаем первые байты для проверки сигнатуры
                input.read(header);

                if (!validateFile(header, fileName)) {
                    System.out.println("Ошибка: файл " + fileName + " имеет неверный тип.");
                    return;
                }

                memoryBuffer.write(header);  // Записываем проверенные байты в буфер
                memoryBuffer.write(input.readAllBytes());  // Записываем остальные байты

                try (OutputStream output = new FileOutputStream(fileName)) {
                    memoryBuffer.writeTo(output);  // Сохраняем все байты на диск
                }

                openFile(fileName);  // Открываем файл
            }
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке: " + e.getMessage());
        }
    }

    private static boolean validateFile(byte[] header, String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        byte[] expectedSignature = FILE_SIGNATURES.get(extension);

        if (expectedSignature == null) {
            System.out.println("Неизвестное расширение файла: " + extension);
            return false;
        }

        // Проверяем соответствие сигнатуры (первые байты файла)
        for (int i = 0; i < expectedSignature.length; i++) {
            if (header[i] != expectedSignature[i]) {
                return false;
            }
        }
        return true;
    }
}