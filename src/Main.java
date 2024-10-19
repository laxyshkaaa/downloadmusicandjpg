import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Downloader {
    private static final Map<String, byte[]> FILE_SIGNATURES = new HashMap<>();

    static {
        FILE_SIGNATURES.put("mp3", new byte[]{(byte) 0xFF, (byte) 0xFB});
        FILE_SIGNATURES.put("mp3", new byte[]{(byte) 0x49, (byte) 0x44, (byte) 0x33}); // ID3
        FILE_SIGNATURES.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


        System.out.print("Введите ссылку на скачивание музыки (mp3): ");
        String musicUrl = scanner.nextLine();
        System.out.print("Введите имя файла для сохранения музыки: ");
        String musicFileName = scanner.nextLine();


        System.out.print("Введите ссылку на скачивание фотографии (jpg): ");
        String photoUrl = scanner.nextLine();
        System.out.print("Введите имя файла для сохранения фотографии: ");
        String photoFileName = scanner.nextLine();

        // Создаем потоки для загрузки файлов
        new Thread(() -> downloadFile(musicUrl, musicFileName)).start();
        new Thread(() -> downloadFile(photoUrl, photoFileName)).start();
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

                byte[] header = new byte[4];  // Читаем первые байты для проверки сигнатуры
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
