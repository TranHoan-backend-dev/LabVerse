package com.se1853_jv.labverse.presentation.user;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.domain.db.AppDatabase;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.enumerate.Role;
import com.se1853_jv.labverse.domain.infrastructure.role.model.Roles;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;
import com.se1853_jv.labverse.domain.infrastructure.user.repo.UserRepository;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        AppDatabase db = DatabaseClient.getInstance(this).getAppDatabase();
//        UserRepository userRepo = db.userRepository();
//
//        new Thread(() -> {
//            Roles role = new Roles("1", Role.INTERN);
//            db.roleRepository().create(role);
//
//            Users user = new Users();
//            user.setId("1");
//            user.setEmail("test@example.com");
//            user.setPassword("123456");
//            user.setName("Test User");
//            user.setUsername("testuser");
//            user.setCreatedDate(System.currentTimeMillis());
//            user.setUpdatedDate(System.currentTimeMillis());
//            user.setRoleId("1");
//
//            userRepo.create(user);
//
//            Users users = userRepo.getById("1");
//
//            runOnUiThread(() -> {
//                Toast.makeText(
//                        this,
//                        users.toString(),
//                        Toast.LENGTH_LONG
//                ).show();
//            });
//        }).start();
    }
}