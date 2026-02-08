package com.example.taximobile.feature.admin.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.admin.data.dto.request.UpdateAdminProfileRequestDto;
import com.example.taximobile.feature.admin.data.dto.response.AdminProfileResponseDto;
import com.example.taximobile.feature.common.data.dto.response.ProfileImageUploadResponseDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileRepository {

    private final Context appCtx;
    private final AdminProfileApi api;

    public AdminProfileRepository(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
        this.api = ApiClient.get(appCtx).create(AdminProfileApi.class);
    }

    public interface ProfileCb {
        void onSuccess(AdminProfileResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public interface UpdateCb {
        void onSuccess();
        void onError(String msg, int httpCode);
    }

    public interface UploadCb {
        void onSuccess(ProfileImageUploadResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public void getProfile(long adminId, ProfileCb cb) {
        api.getAdminProfile(adminId).enqueue(new Callback<AdminProfileResponseDto>() {
            @Override
            public void onResponse(Call<AdminProfileResponseDto> call, Response<AdminProfileResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                AdminProfileResponseDto body = res.body();
                if (body == null) {
                    cb.onError("Empty body", res.code());
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<AdminProfileResponseDto> call, Throwable t) {
                cb.onError(t != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    public void updateProfile(long adminId, UpdateAdminProfileRequestDto req, UpdateCb cb) {
        api.updateAdminProfile(adminId, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                cb.onSuccess();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onError(t != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    public void uploadProfileImage(long adminId, Uri contentUri, UploadCb cb) {
        if (contentUri == null) {
            cb.onError("No file selected", -1);
            return;
        }

        String mime = null;
        try { mime = appCtx.getContentResolver().getType(contentUri); } catch (Exception ignored) {}
        if (mime == null || mime.isBlank()) mime = "image/*";

        String filename = getDisplayName(appCtx.getContentResolver(), contentUri);
        if (filename == null || filename.isBlank()) filename = "upload.jpg";

        byte[] bytes;
        try {
            bytes = readAllBytes(appCtx.getContentResolver(), contentUri);
        } catch (IOException e) {
            cb.onError("Cannot read image", -1);
            return;
        }

        RequestBody rb = RequestBody.create(MediaType.parse(mime), bytes);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, rb);

        api.uploadProfileImage(adminId, part).enqueue(new Callback<ProfileImageUploadResponseDto>() {
            @Override
            public void onResponse(Call<ProfileImageUploadResponseDto> call, Response<ProfileImageUploadResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                ProfileImageUploadResponseDto body = res.body();
                if (body == null) {
                    cb.onError("Empty body", res.code());
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<ProfileImageUploadResponseDto> call, Throwable t) {
                cb.onError(t != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    private static byte[] readAllBytes(ContentResolver cr, Uri uri) throws IOException {
        try (InputStream in = cr.openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) throw new IOException("InputStream is null");

            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    private static String getDisplayName(ContentResolver cr, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = cr.query(uri, null, null, null, null);
            if (cursor == null) return null;

            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex < 0) return null;

            if (cursor.moveToFirst()) {
                return cursor.getString(nameIndex);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
