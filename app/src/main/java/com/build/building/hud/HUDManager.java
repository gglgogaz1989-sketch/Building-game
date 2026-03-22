package com.build.building.hud;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import java.util.HashMap;

public class HUDManager {
    
    private Context context;
    private RelativeLayout hudContainer;
    private HashMap<String, HudButton> buttons = new HashMap<>();
    private DynamicJoystick joystick;
    private BuildMenuView buildMenuView;
    private CoinDisplay coinDisplay;
    
    public HUDManager(Context context, RelativeLayout container) {
        this.context = context;
        this.hudContainer = container;
    }
    
    /**
     * Создание кнопки
     */
    public HudButton createButton(String id, String backgroundPath, int x, int y, int width, int height) {
        return createButton(id, backgroundPath, null, x, y, width, height);
    }
    
    /**
     * Создание кнопки с иконкой
     */
    public HudButton createButton(String id, String backgroundPath, String iconPath, 
                                   int x, int y, int width, int height) {
        HudButton button = new HudButton(context, backgroundPath, iconPath, id);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(width),
            dpToPx(height)
        );
        params.leftMargin = dpToPx(x);
        params.topMargin = dpToPx(y);
        button.setLayoutParams(params);
        
        hudContainer.addView(button);
        buttons.put(id, button);
        
        return button;
    }
    
    /**
     * Создание кнопки с привязкой к краю экрана
     */
    public HudButton createButtonAligned(String id, String backgroundPath, String iconPath,
                                          int alignRule, int marginLeft, int marginTop,
                                          int marginRight, int marginBottom, int width, int height) {
        HudButton button = new HudButton(context, backgroundPath, iconPath, id);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(width),
            dpToPx(height)
        );
        
        if (alignRule != 0) {
            params.addRule(alignRule);
        }
        
        params.leftMargin = dpToPx(marginLeft);
        params.topMargin = dpToPx(marginTop);
        params.rightMargin = dpToPx(marginRight);
        params.bottomMargin = dpToPx(marginBottom);
        
        button.setLayoutParams(params);
        hudContainer.addView(button);
        buttons.put(id, button);
        
        return button;
    }
    
    /**
     * Создание джойстика
     */
    public DynamicJoystick createJoystick(String basePath, String thumbPath, 
                                           int x, int y, int width, int height) {
        joystick = new DynamicJoystick(context, basePath, thumbPath);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            dpToPx(width),
            dpToPx(height)
        );
        params.leftMargin = dpToPx(x);
        params.bottomMargin = dpToPx(y);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        
        joystick.setLayoutParams(params);
        hudContainer.addView(joystick);
        
        return joystick;
    }
    
    /**
     * Создание отображения монет
     */
    public CoinDisplay createCoinDisplay(int x, int y) {
        coinDisplay = new CoinDisplay(context);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dpToPx(y);
        params.rightMargin = dpToPx(x);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        
        coinDisplay.setLayoutParams(params);
        hudContainer.addView(coinDisplay);
        
        return coinDisplay;
    }
    
    /**
     * Создание меню строительства
     */
    public BuildMenuView createBuildMenu(FrameLayout parent) {
        buildMenuView = new BuildMenuView(context, parent);
        return buildMenuView;
    }
    
    /**
     * Получение кнопки по ID
     */
    public HudButton getButton(String id) {
        return buttons.get(id);
    }
    
    /**
     * Удаление кнопки
     */
    public void removeButton(String id) {
        HudButton button = buttons.remove(id);
        if (button != null) {
            hudContainer.removeView(button);
        }
    }
    
    /**
     * Показать/скрыть все кнопки
     */
    public void setAllButtonsVisible(boolean visible) {
        for (HudButton button : buttons.values()) {
            button.setVisible(visible);
        }
    }
    
    /**
     * Очистка всех HUD элементов
     */
    public void clear() {
        for (HudButton button : buttons.values()) {
            hudContainer.removeView(button);
        }
        buttons.clear();
        
        if (joystick != null) {
            hudContainer.removeView(joystick);
        }
        
        if (coinDisplay != null) {
            hudContainer.removeView(coinDisplay);
        }
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
    
    public DynamicJoystick getJoystick() {
        return joystick;
    }
    
    public CoinDisplay getCoinDisplay() {
        return coinDisplay;
    }
          }
