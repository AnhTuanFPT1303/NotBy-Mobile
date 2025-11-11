package com.example.notby.data.remote;

import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.DiaryEntriesResponse;
import com.example.notby.data.model.DiaryEntry;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DiaryEntriesApi {
    @GET("diary-entries")
    Call<ApiResponse<DiaryEntriesResponse>> getDiaryEntries(@Query("childId") String childId);

    @POST("diary-entries")
    Call<ApiResponse<DiaryEntry>> createDiaryEntry(@Body DiaryEntry diaryEntry);

    @GET("diary-entries/{id}")
    Call<ApiResponse<DiaryEntry>> getDiaryEntryById(@Path("id") String id);

    @PATCH("diary-entries/{id}")
    Call<ApiResponse<DiaryEntry>> updateDiaryEntry(@Path("id") String id, @Body DiaryEntry diaryEntry);

    @DELETE("diary-entries/{id}")
    Call<ApiResponse<Void>> deleteDiaryEntry(@Path("id") String id);
}
