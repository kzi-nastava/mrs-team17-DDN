package com.example.taximobile.feature.driver.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.driver.data.dto.request.UpdateDriverProfileRequestDto;
import com.example.taximobile.feature.driver.data.dto.response.DriverProfileResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.ProfileChangeRequestResponseDto;
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

public class DriverProfileRepository {

    private final Context appCtx;
    private final DriverApi api;

    public DriverProfileRepository(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
        this.api = ApiClient.get(appCtx).create(DriverApi.class);
    }

    public interface ProfileCb {
        void onSuccess(DriverProfileResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public interface ChangeRequestCb {
        void onSuccess(ProfileChangeRequestResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public interface UploadCb {
        void onSuccess(ProfileImageUploadResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public void getProfile(long driverId, ProfileCb cb) {
        api.getDriverProfile(driverId).enqueue(new Callback<DriverProfileResponseDto>() {
            @Override
            public void onResponse(Call<DriverProfileResponseDto> call, Response<DriverProfileResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                DriverProfileResponseDto body = res.body();
                if (body == null) {
                    cb.onError("Empty body", res.code());
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<DriverProfileResponseDto> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    public void requestProfileChange(long driverId, UpdateDriverProfileRequestDto req, ChangeRequestCb cb) {
        api.requestProfileChange(driverId, req).enqueue(new Callback<ProfileChangeRequestResponseDto>() {
            @Override
            public void onResponse(Call<ProfileChangeRequestResponseDto> call, Response<ProfileChangeRequestResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                ProfileChangeRequestResponseDto body = res.body();
                if (body == null) {
                    body = new ProfileChangeRequestResponseDto();
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<ProfileChangeRequestResponseDto> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    public void uploadProfileImage(long driverId, Uri contentUri, UploadCb cb) {
        if (contentUri == null) {
            cb.onError("No file selected", -1);
            return;
        }

        String mime = null;
        try {
            mime = appCtx.getContentResolver().getType(contentUri);
        } catch (Exception ignored) {}
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

        api.uploadProfileImage(driverId, part).enqueue(new Callback<ProfileImageUploadResponseDto>() {
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
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error", -1);
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
