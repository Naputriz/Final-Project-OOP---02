package com.kelompok2.frontend.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.kelompok2.frontend.Main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {

    // GANTI PORT INI SESUAI DENGAN PORT BACKEND ANDA (Default Spring Boot biasanya 8080)
    private static final int BACKEND_PORT = 8080;
    // Berapa lama launcher akan menunggu sebelum menyerah (dalam detik)
    private static final int MAX_TIMEOUT_SECONDS = 60;

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired())
            return;

        // 1. Start Backend
        startBackend();

        // 2. Tunggu Backend Ready (BLOCKING)
        if (backendProcess != null) {
            boolean isReady = waitForBackend();
            if (!isReady) {
                System.err.println("[Launcher] Backend gagal start atau timeout. Game mungkin tidak berjalan dengan benar.");
                // Opsi: Anda bisa memilih untuk 'return;' di sini jika ingin membatalkan buka game
            } else {
                System.out.println("[Launcher] Backend ready! Membuka game...");
            }
        }

        // 3. Start Frontend Game
        createApplication();
    }

    private static Process backendProcess;

    private static void startBackend() {
        try {
            java.io.File backendJar = new java.io.File("backend.jar");
            if (backendJar.exists()) {
                System.out.println("[Launcher] Found backend.jar, starting server...");

                // --- PERUBAHAN UTAMA DI SINI ---
                // Gunakan path ke java yang sedang menjalankan program ini
                String javaBin = System.getProperty("java.home")
                    + java.io.File.separator + "bin"
                    + java.io.File.separator + "java";

                // Gunakan javaBin, bukan "java" saja
                ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", "backend.jar");
                // -------------------------------

                pb.directory(new java.io.File("."));
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                backendProcess = pb.start();

                // Add shutdown hook to kill backend when game exits
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("[Launcher] Stopping backend server...");
                    if (backendProcess != null) {
                        backendProcess.destroy();
                    }
                }));
            } else {
                System.out.println("[Launcher] backend.jar not found in " + backendJar.getAbsolutePath()
                    + ". Skipping backend startup.");
            }
        } catch (Exception e) {
            System.err.println("[Launcher] Failed to start backend: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method untuk menunggu port backend terbuka
    private static boolean waitForBackend() {
        System.out.println("[Launcher] Menunggu backend active di port " + BACKEND_PORT + "...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (MAX_TIMEOUT_SECONDS * 1000);

        while (System.currentTimeMillis() < endTime) {
            // Cek apakah proses backend masih hidup. Jika backend crash/mati, berhenti menunggu.
            if (!backendProcess.isAlive()) {
                System.err.println("[Launcher] Backend process mati tiba-tiba saat startup!");
                return false;
            }

            try (Socket socket = new Socket()) {
                // Coba koneksi ke localhost:PORT
                socket.connect(new InetSocketAddress("localhost", BACKEND_PORT), 500);
                // Jika baris ini tereksekusi tanpa error, berarti koneksi berhasil
                return true;
            } catch (IOException e) {
                // Gagal connect, tunggu 1 detik lalu coba lagi
                try {
                    Thread.sleep(1000);
                    System.out.print("."); // Indikator loading
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        System.err.println("\n[Launcher] Timeout: Backend tidak merespon setelah " + MAX_TIMEOUT_SECONDS + " detik.");
        return false;
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Maestra Trials");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
