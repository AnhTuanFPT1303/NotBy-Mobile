package com.example.notby.ui.forumpost;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.model.ForumPost;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class ForumPostActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ForumPostAdapter adapter;

    private final String jsonResponse = "{ \"status\": true, \"path\": \"/Forumpost\", \"message\": \"success\", \"statusCode\": 200, \"data\": [ { \"_id\": \"68dfa7bf3c42679f291ab2e9\", \"Title\": \"Bé 8 tháng tuổi chưa biết bò, có bình thường không?\", \"Content\": \"Con mình đã 8 tháng rồi nhưng vẫn chưa biết bò. Bé chỉ biết lăn và ngồi thôi. Các mẹ cho hỏi có bình thường không ạ?\", \"Likes\": 13, \"Views\": 456, \"Author\": { \"_id\": \"68dc9bc344092a1355ccf5d6\", \"isActive\": true, \"deletedAt\": null, \"deletedBy\": null, \"createdBy\": null, \"updateBy\": null, \"firstName\": \"Nguyen Tuan\", \"lastName\": \"(K18 DN)\", \"phoneNumber\": null, \"email\": \"tuannhade180647@fpt.edu.vn\", \"gender\": \"Male\", \"googleId\": \"107014619501844348514\", \"photo\": \"https://lh3.googleusercontent.com/a/ACg8ocK32jr396CjcbeKC2OzkO-GoTSnkWnMVfLnfrm-osRJNRVTTw=s96-c\", \"role\": \"Admin\", \"created_at\": \"2025-10-01T03:10:59.742Z\", \"updated_at\": \"2025-10-01T16:29:14.273Z\", \"__v\": 0, \"id\": \"68dc9bc344092a1355ccf5d6\" }, \"File\": null, \"created_at\": \"2025-10-03T10:38:55.451Z\", \"updated_at\": \"2025-10-11T00:33:51.500Z\", \"__v\": 0, \"id\": \"68dfa7bf3c42679f291ab2e9\" } ], \"timestamp\": \"2025-11-05 17:47:59\" }";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ForumPost> forumPosts = parseForumPosts(jsonResponse);
        adapter = new ForumPostAdapter(forumPosts);
        recyclerView.setAdapter(adapter);
    }

    private List<ForumPost> parseForumPosts(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray dataArray = jsonObject.getJSONArray("data");

            Gson gson = new Gson();
            Type listType = new TypeToken<List<ForumPost>>() {}.getType();
            return gson.fromJson(dataArray.toString(), listType);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
