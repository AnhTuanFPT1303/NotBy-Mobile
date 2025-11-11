package com.example.notby.ui.library;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.MediaFile;
import com.example.notby.data.remote.ApiClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoUploadManager {

    public interface VideoUploadCallback {
        void onUploadStart();
        void onUploadProgress(String message);
        void onUploadSuccess(MediaFile mediaFile);
        void onUploadError(String error);
    }

    private Context context;
    private TokenManager tokenManager;

    public VideoUploadManager(Context context) {
        this.context = context;
        this.tokenManager = new TokenManager(context);
    }

    public void uploadVideo(Uri videoUri, String fileName, VideoUploadCallback callback) {
        try {
            callback.onUploadStart();
            callback.onUploadProgress("Đang chuẩn bị tệp...");

            // Create a temporary file from the URI
            File tempFile = createTempFileFromUri(videoUri, fileName);
            if (tempFile == null) {
                callback.onUploadError("Không thể đọc tệp video");
                return;
            }

            // Create request body for file upload
            RequestBody requestFile = RequestBody.create(
                MediaType.parse(getMimeType(videoUri)),
                tempFile
            );
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, requestFile);

            callback.onUploadProgress("Đang tải lên Cloudinary...");

            // Upload to Cloudinary first
            ApiClient.getCloudinaryApi().uploadFile(filePart).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    // Clean up temp file
                    tempFile.delete();

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            callback.onUploadProgress("Đang lưu thông tin video...");

                            // Parse Cloudinary response to get file URL
                                Object data = response.body().getData();
                                String fileUrl = (String) data;

                                // Create MediaFile object
                                MediaFile mediaFile = new MediaFile();
                                mediaFile.setFileUrl(fileUrl);
                                mediaFile.setFileName(fileName);
                                mediaFile.setFileType("video");
                                mediaFile.setAuthor(tokenManager.getUserId());

                            // Save to database via MediaFile API
                            ApiClient.getMediafileApi().create(mediaFile).enqueue(new Callback<ApiResponse<MediaFile>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<MediaFile>> call, Response<ApiResponse<MediaFile>> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                        callback.onUploadSuccess(response.body().getData());
                                    } else {
                                        callback.onUploadError("Không thể lưu thông tin video vào cơ sở dữ liệu");
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<MediaFile>> call, Throwable t) {
                                    callback.onUploadError("Lỗi khi lưu video: " + t.getMessage());
                                }
                            });

                        } catch (Exception e) {
                            callback.onUploadError("Lỗi xử lý phản hồi từ Cloudinary: " + e.getMessage());
                        }
                    } else {
                        callback.onUploadError("Lỗi tải lên Cloudinary");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    // Clean up temp file
                    tempFile.delete();
                    callback.onUploadError("Lỗi kết nối Cloudinary: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onUploadError("Lỗi không mong đợi: " + e.getMessage());
        }
    }

    private File createTempFileFromUri(Uri uri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(context.getCacheDir(), "temp_" + fileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private String getMimeType(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        }
        return mimeType != null ? mimeType : "video/*";
    }
}
