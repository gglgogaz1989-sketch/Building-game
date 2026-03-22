package com.build.building.hud;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class BuildMenuView {
    
    private Context context;
    private FrameLayout parentContainer;
    private LinearLayout menuPanel;
    private ScrollView scrollView;
    private boolean isOpen = false;
    
    public interface BuildMenuListener {
        void onBlockSelected(String blockId, String material);
        void onMenuOpened();
        void onMenuClosed();
    }
    
    private BuildMenuListener listener;
    
    public static class BlockItem {
        public String id;
        public String name;
        public String material;
        public String iconPath;
        public String modelPath;
        public int price;
        
        public BlockItem(String id, String name, String material, String iconPath, String modelPath, int price) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.iconPath = iconPath;
            this.modelPath = modelPath;
            this.price = price;
        }
    }
    
    private BlockItem[] blocks = {
        new BlockItem("brick", "Кирпич", "brick", "hud/brick_icon.png", "models/brick.obj", 10),
    };
    
    public BuildMenuView(Context context, FrameLayout parent) {
        this.context = context;
        this.parentContainer = parent;
        createMenu();
    }
    
    private void createMenu() {
        menuPanel = new LinearLayout(context);
        menuPanel.setOrientation(LinearLayout.VERTICAL);
        menuPanel.setBackgroundColor(0xDD1A1A2E);
        
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.35f);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            width,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.leftMargin = 0;
        menuPanel.setLayoutParams(params);
        menuPanel.setVisibility(View.GONE);
        
        // Заголовок
        TextView title = new TextView(context);
        title.setText("СТРОИТЕЛЬСТВО");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        title.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        title.setBackgroundColor(0xFF16213E);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        menuPanel.addView(title);
        
        // Скролл для списка
        scrollView = new ScrollView(context);
        LinearLayout itemsContainer = new LinearLayout(context);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        
        for (BlockItem block : blocks) {
            View item = createBlockItem(block);
            itemsContainer.addView(item);
        }
        
        scrollView.addView(itemsContainer);
        menuPanel.addView(scrollView);
        
        parentContainer.addView(menuPanel);
    }
    
    private View createBlockItem(BlockItem block) {
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
        item.setBackgroundColor(0x33000000);
        
        // Иконка
        HudButton icon = new HudButton(context, block.iconPath, block.id + "_icon");
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48));
        iconParams.rightMargin = dpToPx(16);
        icon.setLayoutParams(iconParams);
        item.addView(icon);
        
        // Информация
        LinearLayout infoLayout = new LinearLayout(context);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        
        TextView name = new TextView(context);
        name.setText(block.name);
        name.setTextColor(0xFFFFFFFF);
        name.setTextSize(16);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        infoLayout.addView(name);
        
        TextView material = new TextView(context);
        material.setText("Материал: " + block.material);
        material.setTextColor(0xFFAAAAAA);
        material.setTextSize(12);
        infoLayout.addView(material);
        
        TextView price = new TextView(context);
        price.setText("💰 " + block.price);
        price.setTextColor(0xFFFFD700);
        price.setTextSize(12);
        infoLayout.addView(price);
        
        item.addView(infoLayout);
        
        item.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBlockSelected(block.id, block.material);
            }
            closeMenu();
        });
        
        return item;
    }
    
    public void toggleMenu() {
        if (isOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }
    
    public void openMenu() {
        isOpen = true;
        menuPanel.setVisibility(View.VISIBLE);
        if (listener != null) {
            listener.onMenuOpened();
        }
    }
    
    public void closeMenu() {
        isOpen = false;
        menuPanel.setVisibility(View.GONE);
        if (listener != null) {
            listener.onMenuClosed();
        }
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public void setListener(BuildMenuListener listener) {
        this.listener = listener;
    }
    
    public void setBlocks(BlockItem[] newBlocks) {
        this.blocks = newBlocks;
        recreateMenu();
    }
    
    private void recreateMenu() {
        parentContainer.removeView(menuPanel);
        createMenu();
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
          }
