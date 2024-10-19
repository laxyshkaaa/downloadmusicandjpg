import java.io.*;
import java.net.URL;
import java.net.URLConnection;

class Downloader {
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

                if (!checkFileSignature(header, fileName)) {
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

    private static boolean checkFileSignature(byte[] signature, String fileName) {
        String extension = extractExtension(fileName).toLowerCase();

        switch (extension) {
            case "mp3":
                return validateMp3(signature);
            case "jpg":
                return validateJpg(signature);
            default:
                System.out.println("Неизвестное расширение файла: " + extension);
                return false;
        }
    }

    private static boolean validateMp3(byte[] signature) {
        return (signature[0] == (byte) 0xFF && signature[1] == (byte) 0xFB) ||
                (signature[0] == (byte) 0x49 && signature[1] == (byte) 0x44 && signature[2] == (byte) 0x33);
    }

    private static boolean validateJpg(byte[] signature) {
        return signature[0] == (byte) 0xFF && signature[1] == (byte) 0xD8 && signature[2] == (byte) 0xFF;
    }

    private static String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? "" : fileName.substring(index + 1);
    }
}