package com.se1853_jv.labverse.data.api.team;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.Constants;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.request.AddTeamMemberRequest;
import com.se1853_jv.labverse.data.dto.request.CreateTeamRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateTeamRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateMemberRoleRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.TeamMemberResponse;
import com.se1853_jv.labverse.data.dto.response.TeamResponse;
import com.se1853_jv.labverse.data.dto.response.TeamsPageResponse;
import com.se1853_jv.labverse.data.utils.EncoderUtils;
import com.se1853_jv.labverse.data.utils.SessionManager;

import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TeamApiHandler {
    private static final String TAG = "TeamApiHandler";
    private static final String BASE_URL = Constants.ACCOUNT_ENDPOINT_URL;
    
    private final TeamApi teamApi;
    private final SessionManager sessionManager;

    public TeamApiHandler(Context context) {
        this.sessionManager = new SessionManager(context);
        
        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add token interceptor
        Interceptor tokenInterceptor = chain -> {
            Request original = chain.request();
            String token = sessionManager.getBearerToken();
            
            Request.Builder requestBuilder = original.newBuilder();
            if (token != null) {
                requestBuilder.header("Authorization", token);
            }
            
            return chain.proceed(requestBuilder.build());
        };

        var client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .addInterceptor(logging)
                .build();

        var retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        
        teamApi = retrofit.create(TeamApi.class);
    }

    /**
     * Get list of teams with optional filters
     */
    public void getTeams(String search, String researchField, String privacy, 
                        Integer page, Integer size, ApiCallback<TeamsPageResponse> callback) {
        Log.d(TAG, "getTeams: search=" + search + ", researchField=" + researchField);
        Call<BaseJsonResponse<TeamsPageResponse>> call = teamApi.getTeams(search, researchField, privacy, page, size);
        call.enqueue(new Callback<BaseJsonResponse<TeamsPageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<TeamsPageResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<TeamsPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Teams fetched: " + (result != null && result.getContent() != null ? result.getContent().size() : 0));
                } else {
                    String errorMessage = "Failed to fetch teams";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<TeamsPageResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Get team by ID
     * Note: teamId from API response is already encoded, so we use it directly
     */
    public void getTeamById(String id, ApiCallback<TeamResponse> callback) {
        // ID from API response is already encoded, use directly
        Log.d(TAG, "getTeamById: " + id);
        Call<BaseJsonResponse<TeamResponse>> call = teamApi.getTeamById(id);
        call.enqueue(new Callback<BaseJsonResponse<TeamResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<TeamResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<TeamResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Team fetched: " + (result != null ? result.getName() : "null"));
                } else {
                    String errorMessage = "Failed to fetch team";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<TeamResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Create a new team
     */
    public void createTeam(CreateTeamRequest request, ApiCallback<TeamResponse> callback) {
        Log.d(TAG, "createTeam: " + request.getName());
        Call<BaseJsonResponse<TeamResponse>> call = teamApi.createTeam(request);
        call.enqueue(new Callback<BaseJsonResponse<TeamResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<TeamResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<TeamResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Team created: " + (result != null ? result.getName() : "null"));
                } else {
                    String errorMessage = "Failed to create team";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<TeamResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Update team
     * Note: teamId from API response is already encoded, so we use it directly
     */
    public void updateTeam(String teamId, UpdateTeamRequest request, ApiCallback<TeamResponse> callback) {
        // ID from API response is already encoded, use directly
        Log.d(TAG, "updateTeam: teamId=" + teamId + ", name=" + request.getName());
        Call<BaseJsonResponse<TeamResponse>> call = teamApi.updateTeam(teamId, request);
        call.enqueue(new Callback<BaseJsonResponse<TeamResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<TeamResponse>> call,
                                 @NonNull Response<BaseJsonResponse<TeamResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Team updated: " + (result != null ? result.getName() : "null"));
                } else {
                    String errorMessage = "Failed to update team";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<TeamResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Get team members
     * Note: teamId from API response is already encoded, so we use it directly
     */
    public void getTeamMembers(String teamId, ApiCallback<List<TeamMemberResponse>> callback) {
        // ID from API response is already encoded, use directly
        Log.d(TAG, "getTeamMembers: teamId=" + teamId);
        Call<BaseJsonResponse<List<TeamMemberResponse>>> call = teamApi.getTeamMembers(teamId);
        call.enqueue(new Callback<BaseJsonResponse<List<TeamMemberResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<List<TeamMemberResponse>>> call, 
                                 @NonNull Response<BaseJsonResponse<List<TeamMemberResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Team members fetched: " + (result != null ? result.size() : 0));
                } else {
                    String errorMessage = "Failed to fetch team members";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<List<TeamMemberResponse>>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Add a member to team
     * Note: teamId from API response is already encoded, so we use it directly
     */
    public void addTeamMember(String teamId, AddTeamMemberRequest request, ApiCallback<TeamMemberResponse> callback) {
        // ID from API response is already encoded, use directly
        Log.d(TAG, "addTeamMember: teamId=" + teamId + ", userId=" + request.getUserId());
        Call<BaseJsonResponse<TeamMemberResponse>> call = teamApi.addTeamMember(teamId, request);
        call.enqueue(new Callback<BaseJsonResponse<TeamMemberResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<TeamMemberResponse>> call,
                                 @NonNull Response<BaseJsonResponse<TeamMemberResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Team member added: " + (result != null ? result.getUserFullName() : "null"));
                } else {
                    String errorMessage = "Failed to add team member";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<TeamMemberResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Remove a member from team
     * Note: Both teamId and memberId (userId) from API response are already encoded, use directly
     */
    public void removeTeamMember(String teamId, String memberId, ApiCallback<Object> callback) {
        // Both IDs from API response are already encoded, use directly
        Log.d(TAG, "removeTeamMember: teamId=" + teamId + ", memberId=" + memberId);
        Call<BaseJsonResponse<Object>> call = teamApi.removeTeamMember(teamId, memberId);
        call.enqueue(new Callback<BaseJsonResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<Object>> call,
                                 @NonNull Response<BaseJsonResponse<Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                    Log.d(TAG, "Team member removed successfully");
                } else {
                    String errorMessage = "Failed to remove team member";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Update member role
     * Note: Both teamId and memberId from API response are already encoded, use directly
     */
    public void updateMemberRole(String teamId, String memberId, UpdateMemberRoleRequest request, ApiCallback<TeamMemberResponse> callback) {
        // Both IDs from API response are already encoded, use directly
        Log.d(TAG, "updateMemberRole: teamId=" + teamId + ", memberId=" + memberId + ", role=" + request.getRole());
        Call<BaseJsonResponse<TeamMemberResponse>> call = teamApi.updateMemberRole(teamId, memberId, request);
        call.enqueue(new Callback<BaseJsonResponse<TeamMemberResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<TeamMemberResponse>> call,
                                 @NonNull Response<BaseJsonResponse<TeamMemberResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Member role updated: " + (result != null ? result.getRole() : "null"));
                } else {
                    String errorMessage = "Failed to update member role";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<TeamMemberResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}

