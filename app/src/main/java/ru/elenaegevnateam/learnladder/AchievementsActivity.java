package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AchievementsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String ACHIEVEMENTS_PREFS = "achievements_prefs";
    private DatabaseReference databaseReferenceUserCoins;
    String user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUserCoins = database.getReference("users").child(user_id).child("coins");

        sharedPreferences = getSharedPreferences(ACHIEVEMENTS_PREFS, MODE_PRIVATE);

        Map<String, Integer> achievementMap = new HashMap<>();
        achievementMap.put("stresses_cards", R.id.stressesCardsImageView);
        achievementMap.put("words_cards", R.id.wordsCardsImageView);
        achievementMap.put("trops_cards", R.id.tropsCardsImageView);
        achievementMap.put("stresses_test", R.id.stressesTestImageView);
        achievementMap.put("words_test", R.id.wordsTestImageView);
        achievementMap.put("trops_test", R.id.tropsTestImageView);
        achievementMap.put("roots_test", R.id.rootsTestImageView);
        achievementMap.put("prefixes_test", R.id.prefixesTestImageView);
        achievementMap.put("verbs_test", R.id.verbsTestImageView);
        achievementMap.put("all_cards", R.id.allCardsImageView);
        achievementMap.put("all_tests", R.id.allTestsImageView);

        for (Map.Entry<String, Integer> entry : achievementMap.entrySet()) {
            checkAndSetAchievement(findViewById(entry.getValue()), intent.getStringExtra(entry.getKey()), entry.getKey());
        }

        if (allCardsUnlocked(intent)) {
            checkAndSetAchievement(findViewById(R.id.allCardsImageView), "0", "all_cards");
        }

        if (allTestsUnlocked(intent)) {
            checkAndSetAchievement(findViewById(R.id.allTestsImageView), "0", "all_tests");
        }
    }

    private boolean allCardsUnlocked(Intent intent) {
        return Objects.equals(intent.getStringExtra("stresses_cards"), "0") &&
                Objects.equals(intent.getStringExtra("words_cards"), "0") &&
                Objects.equals(intent.getStringExtra("trops_cards"), "0");
    }

    private boolean allTestsUnlocked(Intent intent) {
        return Objects.equals(intent.getStringExtra("stresses_test"), "0") &&
                Objects.equals(intent.getStringExtra("words_test"), "0") &&
                Objects.equals(intent.getStringExtra("trops_test"), "0") &&
                Objects.equals(intent.getStringExtra("roots_test"), "0") &&
                Objects.equals(intent.getStringExtra("prefixes_test"), "0") &&
                Objects.equals(intent.getStringExtra("verbs_test"), "0");
    }

    private void checkAndSetAchievement(ImageView imageView, String achievementValue, String achievementKey) {
        boolean isAchievementUnlocked = sharedPreferences.getBoolean(user_id + "_" + achievementKey, false);
        boolean sample = Objects.equals(achievementKey, "stresses_cards") || Objects.equals(achievementKey, "words_cards") || Objects.equals(achievementKey, "trops_cards") || Objects.equals(achievementKey, "stresses_test") || Objects.equals(achievementKey, "words_test") || Objects.equals(achievementKey, "trops_test") || Objects.equals(achievementKey, "roots_test") || Objects.equals(achievementKey, "prefixes_test") || Objects.equals(achievementKey, "verbs_test");
        boolean cool = Objects.equals(achievementKey, "all_cards") || Objects.equals(achievementKey, "all_tests");
        if (sample) {
            if (isAchievementUnlocked || Objects.equals(achievementValue, "0")) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.sample_achieve_c));
                if (!isAchievementUnlocked) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(user_id + "_" + achievementKey, true);
                    editor.apply();
                    handleAchievementBonus(achievementKey);
                }
            }
        } else if (cool) {
            if (isAchievementUnlocked || Objects.equals(achievementValue, "0")) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.cool_achieve_c));
                if (!isAchievementUnlocked) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(user_id + "_" + achievementKey, true);
                    editor.apply();
                    handleAchievementBonus(achievementKey);
                }
            }
        }
    }

    private void handleAchievementBonus(String achievementKey) {
        Map<String, Integer> buttonMap = new HashMap<>();
        buttonMap.put("stresses_cards", R.id.stressesCardsButton);
        buttonMap.put("words_cards", R.id.wordsCardsButton);
        buttonMap.put("trops_cards", R.id.tropsCardsButton);
        buttonMap.put("stresses_test", R.id.stressesTestButton);
        buttonMap.put("words_test", R.id.wordsTestButton);
        buttonMap.put("trops_test", R.id.tropsTestButton);
        buttonMap.put("roots_test", R.id.rootsTestButton);
        buttonMap.put("prefixes_test", R.id.prefixesTestButton);
        buttonMap.put("verbs_test", R.id.verbsTestButton);
        buttonMap.put("all_cards", R.id.allCardsButton);
        buttonMap.put("all_tests", R.id.allTestsButton);

        Map<String, Integer> bonusMap = new HashMap<>();
        bonusMap.put("stresses_cards", 5);
        bonusMap.put("words_cards", 5);
        bonusMap.put("trops_cards", 5);
        bonusMap.put("stresses_test", 15);
        bonusMap.put("words_test", 15);
        bonusMap.put("trops_test", 15);
        bonusMap.put("roots_test", 15);
        bonusMap.put("prefixes_test", 15);
        bonusMap.put("verbs_test", 15);
        bonusMap.put("all_cards", 30);
        bonusMap.put("all_tests", 100);

        if (buttonMap.containsKey(achievementKey) && bonusMap.containsKey(achievementKey)) {
            int buttonId = buttonMap.get(achievementKey);
            if (sharedPreferences.getInt(user_id + "_" + achievementKey + "_isPressed", 0) == 1) {
                findViewById(buttonId).setVisibility(View.GONE);
            } else {
                findViewById(buttonId).setVisibility(View.VISIBLE);
                Toast.makeText(this, "Ты открыл новое достижение! Успей получить награду!", Toast.LENGTH_SHORT).show();
                findViewById(buttonId).setOnClickListener(v -> updateCoins(bonusMap.get(achievementKey), buttonId, achievementKey));
            }
        }
    }

    private void updateCoins(int bonus, int buttonId, String achievementKey) {
        databaseReferenceUserCoins.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String coins = String.valueOf(task.getResult().getValue());
                int coinsInt = Integer.parseInt(coins) + bonus;
                databaseReferenceUserCoins.setValue(coinsInt).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(user_id + "_" + achievementKey + "_isPressed", 1);
                        editor.apply();

                        findViewById(buttonId).setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AchievementsActivity.this, AccountActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
