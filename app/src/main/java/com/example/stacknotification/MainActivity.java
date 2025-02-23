package com.example.stacknotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    // 알림 채널 ID (안드로이드 8.0 이상에서 필요)
    private static final String CHANNEL_ID = "stack_notification_channel";
    // 알림 그룹 키 (스택 알림을 묶기 위해 사용)
    private static final String GROUP_KEY = "stack_notification_group";

    // 권한 요청을 위한 ActivityResultLauncher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        // 권한이 허용되면 알림을 생성
                        createNotificationChannel();
                        showStackNotification(1, "첫 번째 알림입니다!");
                        showStackNotification(2, "두 번째 알림입니다!");
                    } else {
                        // 권한이 거부되었을 경우 처리
                        // 사용자에게 알림 권한이 필요함을 알려줌
                        Toast.makeText(MainActivity.this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 안드로이드 13 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        } else {
            // 안드로이드 12 이하에서는 권한 요청 필요 없음
            createNotificationChannel();
            showStackNotification(1, "첫 번째 알림입니다!");
            showStackNotification(2, "두 번째 알림입니다!");
        }
    }

    /**
     * 알림 권한을 체크하고, 권한이 없으면 요청합니다.
     */
    private void checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            // 권한 요청을 위한 인텐트 실행
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // 권한이 이미 있으면 알림 생성
            createNotificationChannel();
            showStackNotification(1, "첫 번째 알림입니다!");
            showStackNotification(2, "두 번째 알림입니다!");
        }
    }

    /**
     * 알림 채널을 생성합니다.
     * 안드로이드 8.0 (API 레벨 26) 이상에서 필요합니다.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Stack Notifications"; // 사용자에게 보이는 채널 이름
            String description = "앱의 스택 알림을 표시합니다."; // 채널 설명
            int importance = NotificationManager.IMPORTANCE_HIGH; // 알림 중요도

            // NotificationChannel 객체 생성
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // NotificationManager에 채널 등록
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 스택 알림을 생성하고 표시합니다.
     *
     * @param notificationId 알림 ID (각 알림마다 고유한 ID 필요)
     * @param message        알림 메시지
     */
    private void showStackNotification(int notificationId, String message) {
        // 알림 클릭 시 실행할 Intent 생성
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // 알림 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // 알림 아이콘 (res/drawable에 추가 필요)
                .setContentTitle("새 알림") // 알림 제목
                .setContentText(message) // 알림 내용
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 알림 중요도
                .setContentIntent(pendingIntent) // 알림 클릭 시 실행할 Intent
                .setGroup(GROUP_KEY) // 알림 그룹 지정
                .setAutoCancel(true); // 알림 클릭 시 자동으로 삭제

        // 권한이 부여되지 않은 경우에는 알림을 표시하지 않음
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            // 알림 표시
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(notificationId, builder.build());

            // 그룹 요약 알림 생성 (스택 알림을 묶기 위해 사용)
            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("알림 요약")
                    .setContentText("여러 알림이 있습니다.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setGroup(GROUP_KEY)
                    .setGroupSummary(true); // 요약 알림으로 설정

            notificationManager.notify(0, summaryBuilder.build());
        } else {
            // 권한이 없는 경우 알림을 표시할 수 없음
            Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
