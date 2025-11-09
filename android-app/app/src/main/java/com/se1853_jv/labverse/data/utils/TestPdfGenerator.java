package com.se1853_jv.labverse.data.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class để tạo file PDF test cho việc test upload
 */
public class TestPdfGenerator {
    private static final String TAG = "TestPdfGenerator";
    
    /**
     * Tạo một file PDF test đơn giản
     * @param context Context của app
     * @return URI của file PDF đã tạo, null nếu lỗi
     */
    public static Uri createTestPdf(Context context) {
        try {
            // Tạo file trong internal storage
            File pdfFile = new File(context.getFilesDir(), "test_paper_" + System.currentTimeMillis() + ".pdf");
            
            // Tạo nội dung PDF đơn giản (minimal PDF structure)
            byte[] pdfContent = createMinimalPdf();
            
            // Ghi vào file
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(pdfContent);
            fos.close();
            
            Log.d(TAG, "✅ Test PDF created: " + pdfFile.getAbsolutePath());
            Log.d(TAG, "📄 File size: " + (pdfFile.length() / 1024) + " KB");
            
            return Uri.fromFile(pdfFile);
            
        } catch (IOException e) {
            Log.e(TAG, "❌ Error creating test PDF: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Tạo PDF content đơn giản (minimal valid PDF)
     * Đây là một PDF hợp lệ nhưng rất đơn giản, chỉ để test upload
     */
    private static byte[] createMinimalPdf() {
        String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<<\n" +
                "/Type /Catalog\n" +
                "/Pages 2 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<<\n" +
                "/Type /Pages\n" +
                "/Kids [3 0 R]\n" +
                "/Count 1\n" +
                ">>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<<\n" +
                "/Type /Page\n" +
                "/Parent 2 0 R\n" +
                "/MediaBox [0 0 612 792]\n" +
                "/Contents 4 0 R\n" +
                "/Resources <<\n" +
                "/Font <<\n" +
                "/F1 <<\n" +
                "/Type /Font\n" +
                "/Subtype /Type1\n" +
                "/BaseFont /Helvetica\n" +
                ">>\n" +
                ">>\n" +
                ">>\n" +
                ">>\n" +
                "endobj\n" +
                "4 0 obj\n" +
                "<<\n" +
                "/Length 100\n" +
                ">>\n" +
                "stream\n" +
                "BT\n" +
                "/F1 24 Tf\n" +
                "100 700 Td\n" +
                "(Test PDF for LabVerse) Tj\n" +
                "0 -30 Td\n" +
                "/F1 12 Tf\n" +
                "(Generated: " + System.currentTimeMillis() + ") Tj\n" +
                "ET\n" +
                "endstream\n" +
                "endobj\n" +
                "xref\n" +
                "0 5\n" +
                "0000000000 65535 f \n" +
                "0000000009 00000 n \n" +
                "0000000058 00000 n \n" +
                "0000000115 00000 n \n" +
                "0000000330 00000 n \n" +
                "trailer\n" +
                "<<\n" +
                "/Size 5\n" +
                "/Root 1 0 R\n" +
                ">>\n" +
                "startxref\n" +
                "500\n" +
                "%%EOF";
        
        return pdfContent.getBytes();
    }
    
    /**
     * Kiểm tra xem file test PDF đã tồn tại chưa
     */
    public static boolean testPdfExists(Context context) {
        File filesDir = context.getFilesDir();
        File[] files = filesDir.listFiles((dir, name) -> name.startsWith("test_paper_") && name.endsWith(".pdf"));
        return files != null && files.length > 0;
    }
    
    /**
     * Xóa tất cả file test PDF cũ
     */
    public static void cleanupTestPdfs(Context context) {
        try {
            File filesDir = context.getFilesDir();
            File[] files = filesDir.listFiles((dir, name) -> name.startsWith("test_paper_") && name.endsWith(".pdf"));
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        Log.d(TAG, "Deleted old test PDF: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up test PDFs: " + e.getMessage());
        }
    }
}

