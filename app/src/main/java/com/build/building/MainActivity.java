package com.build.building;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.build.building.hud.DynamicJoystick;
import com.build.building.hud.HudButton;
import com.build.building.render.GameRenderer;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    // 3D компоненты
    private GLSurfaceView glSurfaceView;
    private GameRenderer renderer;
    
    // HUD контейнеры
    private RelativeLayout hudContainer;
    private TextView txtBuildMode;
    private TextView txtCoinCount;
    private ImageView crosshair;
    
    // HUD элементы
    private DynamicJoystick joystick;
    private HudButton btnBuild;
    private HudButton btnHand;
    private HudButton btnShop;
    private HudButton btnMods;
    private HudButton btnMenu;
    
    // Режимы
    private boolean buildMode = false;
    private boolean deleteMode = false;
    private boolean isDraggingBlock = false;
    
    // Валюта (временная заглушка)
    private int coinCount = 100;
    
    // Обработчик для долгих нажатий
    private Handler longPressHandler = new Handler();
    private Runnable longPressRunnable;
    
    // Размеры экрана
    private int screenWidth, screenHeight;
    
    // Джойстик значения
    private float joystickX = 0f;
    private float joystickY = 0f;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Принудительная альбомная ориентация (перевёрнутая)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        
        // Полноэкранный режим
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_main);
        
        // Получаем размеры экрана
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        // Инициализация 3D
        init3DRenderer();
        
        // Инициализация HUD контейнеров
        initHUDContainers();
        
        // Загрузка HUD элементов
        loadHudElements();
        
        // Загрузка поинта (прицела)
        loadCrosshair();
        
        // Скрываем системную навигацию
        hideSystemUI();
        
        // Временный Toast о запуске
        Toast.makeText(this, "Игра загружена. Режим: обзор", Toast.LENGTH_SHORT).show();
    }
    
    private void init3DRenderer() {
        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(3);
        renderer = new GameRenderer(this);
        renderer.setBuildModeListener(this::onBuildModeChanged);
        renderer.setCoinListener(this::onCoinEarned);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
    
    private void initHUDContainers() {
        hudContainer = findViewById(R.id.hudContainer);
        txtBuildMode = findViewById(R.id.txtBuildMode);
        txtCoinCount = findViewById(R.id.txtCoinCount);
        crosshair = findViewById(R.id.crosshair);
        
        // Настройка текста монет
        updateCoinDisplay();
    }
    
    private void loadHudElements() {
        // Загружаем джойстик
        loadJoystick();
        
        // Загружаем кнопки
        loadBuildButton();
        loadHandButton();
        loadShopButton();
        loadModsButton();
        loadMenuButton();
    }
    
    private void loadJoystick() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(120),
            dpToPx(120)
        );
        
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.leftMargin = dpToPx(16);
        params.bottomMargin = dpToPx(24);
        
        joystick = new DynamicJoystick(this, "hud/joystick_base.png", "hud/joystick_thumb.png");
        joystick.setLayoutParams(params);
        joystick.setListener(new DynamicJoystick.OnJoystickMoveListener() {
            @Override
            public void onMove(float x, float y) {
                joystickX = x;
                joystickY = y;
                if (renderer != null) {
                    renderer.movePlayer(x, y);
                }
            }
            
            @Override
            public void onStop() {
                joystickX = 0f;
                joystickY = 0f;
                if (renderer != null) {
                    renderer.stopPlayer();
                }
            }
        });
        
        hudContainer.addView(joystick);
    }
    
    private void loadBuildButton() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(72),
            dpToPx(72)
        );
        
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.rightMargin = dpToPx(24);
        params.bottomMargin = dpToPx(160);
        
        btnBuild = new HudButton(this, "hud/build_button.png", "build");
        btnBuild.setLayoutParams(params);
        btnBuild.setListener(new HudButton.OnHudButtonListener() {
            @Override
            public void onTap(String id) {
                toggleBuildMode();
            }
            
            @Override
            public void onLongPress(String id) {
                // Долгое нажатие — включить режим удаления
                if (buildMode) {
                    deleteMode = true;
                    showToast("Режим: удаление блоков");
                    btnBuild.setAlpha(0.5f);
                }
            }
            
            @Override
            public void onRelease(String id) {
                deleteMode = false;
                if (buildMode) {
                    btnBuild.setAlpha(1f);
                }
            }
        });
        
        hudContainer.addView(btnBuild);
    }
    
    private void loadHandButton() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(72),
            dpToPx(72)
        );
        
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.rightMargin = dpToPx(24);
        params.bottomMargin = dpToPx(24);
        
        btnHand = new HudButton(this, "hud/hand_button.png", "hand");
        btnHand.setLayoutParams(params);
        btnHand.setListener(new HudButton.OnHudButtonListener() {
            @Override
            public void onTap(String id) {
                // Короткое нажатие — взаимодействие
                if (renderer != null) {
                    renderer.onInteract();
                    showToast("Взаимодействие");
                }
            }
            
            @Override
            public void onLongPress(String id) {
                // Долгое нажатие — начать перемещение блока
                isDraggingBlock = true;
                if (renderer != null) {
                    renderer.onStartDrag();
                }
                showToast("Режим: перемещение блока");
                btnHand.setAlpha(0.5f);
            }
            
            @Override
            public void onRelease(String id) {
                if (isDraggingBlock) {
                    isDraggingBlock = false;
                    if (renderer != null) {
                        renderer.onEndDrag();
                    }
                    btnHand.setAlpha(1f);
                }
            }
        });
        
        hudContainer.addView(btnHand);
    }
    
    private void loadShopButton() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(56),
            dpToPx(56)
        );
        
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.topMargin = dpToPx(16);
        params.rightMargin = dpToPx(16);
        
        btnShop = new HudButton(this, "hud/empty_button.png", "shop");
        btnShop.setIconImage(this, "hud/coin_icon.png");
        btnShop.setLayoutParams(params);
        btnShop.setListener(new HudButton.OnHudButtonListener() {
            @Override
            public void onTap(String id) {
                showToast("Магазин (скоро)");
                // TODO: Открыть магазин
            }
            
            @Override
            public void onLongPress(String id) {}
            
            @Override
            public void onRelease(String id) {}
        });
        
        hudContainer.addView(btnShop);
    }
    
    private void loadModsButton() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(56),
            dpToPx(56)
        );
        
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.LEFT_OF, R.id.btnShop);
        params.topMargin = dpToPx(16);
        params.rightMargin = dpToPx(8);
        
        btnMods = new HudButton(this, "hud/empty_button.png", "mods");
        btnMods.setOverlayText("MOD");
        btnMods.setTextStyle(16f, 0xFFFFFFFF);
        btnMods.setLayoutParams(params);
        btnMods.setListener(new HudButton.OnHudButtonListener() {
            @Override
            public void onTap(String id) {
                showToast("Моды (скоро)");
                // TODO: Открыть меню модов
            }
            
            @Override
            public void onLongPress(String id) {}
            
            @Override
            public void onRelease(String id) {}
        });
        
        hudContainer.addView(btnMods);
    }
    
    private void loadMenuButton() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(56),
            dpToPx(56)
        );
        
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.topMargin = dpToPx(16);
        params.leftMargin = dpToPx(16);
        
        btnMenu = new HudButton(this, "hud/empty_button.png", "menu");
        btnMenu.setOverlayText("≡");
        btnMenu.setTextStyle(28f, 0xFFFFFFFF);
        btnMenu.setLayoutParams(params);
        btnMenu.setListener(new HudButton.OnHudButtonListener() {
            @Override
            public void onTap(String id) {
                showToast("Меню миров (скоро)");
                // TODO: Открыть меню выбора миров
            }
            
            @Override
            public void onLongPress(String id) {}
            
            @Override
            public void onRelease(String id) {}
        });
        
        hudContainer.addView(btnMenu);
    }
    
    private void loadCrosshair() {
        try {
            InputStream is = getAssets().open("hud/crosshair_point.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            crosshair.setImageBitmap(bitmap);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Если нет картинки, создаём простой крестик через код
            crosshair.setBackgroundColor(0xFFFFFFFF);
            crosshair.setAlpha(0.8f);
        }
    }
    
    private void toggleBuildMode() {
        buildMode = !buildMode;
        renderer.setBuildMode(buildMode);
        
        if (buildMode) {
            txtBuildMode.setText("Режим: строительство");
            txtBuildMode.setVisibility(View.VISIBLE);
            btnBuild.setAlpha(1f);
            showToast("Режим строительства: нажмите на блок для установки");
        } else {
            txtBuildMode.setText("Режим: обзор");
            txtBuildMode.setVisibility(View.GONE);
            deleteMode = false;
            showToast("Режим обзора");
        }
    }
    
    private void onBuildModeChanged(boolean isBuildMode) {
        buildMode = isBuildMode;
        runOnUiThread(() -> {
            if (buildMode) {
                txtBuildMode.setText("Режим: строительство");
                txtBuildMode.setVisibility(View.VISIBLE);
            } else {
                txtBuildMode.setVisibility(View.GONE);
            }
        });
    }
    
    private void onCoinEarned(int amount) {
        coinCount += amount;
        runOnUiThread(this::updateCoinDisplay);
        showToast("+" + amount + " монет");
    }
    
    private void updateCoinDisplay() {
        if (txtCoinCount != null) {
            txtCoinCount.setText(String.valueOf(coinCount));
        }
    }
    
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Принудительно возвращаем ориентацию
        if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
        hideSystemUI();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Передаём касания в рендер (камера)
        if (renderer != null && !isDraggingBlock && !deleteMode) {
            renderer.handleTouch(event);
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }
        hideSystemUI();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (renderer != null) {
            renderer.cleanup();
        }
    }
}
