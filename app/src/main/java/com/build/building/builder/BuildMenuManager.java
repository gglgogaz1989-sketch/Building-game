package com.build.building.builder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.build.building.R;

public class BuildMenuManager {
    
    private Context context;
    private FrameLayout parentContainer;
    private LinearLayout menuPanel;
    private boolean isOpen = false;
    
    public interface BuildMenuListener {
        void onBlockSelected(String blockId, String material);
        void onMenuOpened();
        void onMenuClosed();
    }
    
    private BuildMenuListener listener;
    
    // Доступные блоки
    private static class BlockItem {
        String id;
        String name;
        String material;
        int iconRes;
        String modelPath;
        
        BlockItem(String id, String name, String material, int iconRes, String modelPath) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.iconRes = iconRes;
            this.modelPath = modelPath;
        }
    }
    
    private BlockItem[] availableBlocks = {
        new BlockItem("brick", "Кирпич", "brick", 0, "models/brick.obj"),
        // новые блоки добавятся позже
    };
    
    public BuildMenuManager(Context context, FrameLayout parent) {
        this.context = context;
        this.parentContainer = parent;
        createMenu();
    }
    
    private void createMenu() {
        menuPanel = new LinearLayout(context);
        menuPanel.setOrientation(LinearLayout.VERTICAL);
        menuPanel.setBackgroundColor(0xCC000000);
        
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.3f);
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
        title.setTextSize(18);
        title.setPadding(16, 16, 16, 16);
        title.setBackgroundColor(0x88000000);
        menuPanel.addView(title);
        
        // Список блоков
        for (BlockItem block : availableBlocks) {
            LinearLayout item = createBlockItem(block);
            menuPanel.addView(item);
        }
        
        parentContainer.addView(menuPanel);
    }
    
    private LinearLayout createBlockItem(BlockItem block) {
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(16, 12, 16, 12);
        item.setBackgroundColor(0x33000000);
        
        // Иконка
        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(48, 48);
        iconParams.rightMargin = 16;
        icon.setLayoutParams(iconParams);
        if (block.iconRes != 0) {
            icon.setImageResource(block.iconRes);
        } else {
            icon.setBackgroundColor(0xFF888888);
        }
        item.addView(icon);
        
        // Название
        TextView name = new TextView(context);
        name.setText(block.name);
        name.setTextColor(0xFFFFFFFF);
        name.setTextSize(16);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        item.addView(name);
        
        // Материал
        TextView material = new TextView(context);
        material.setText(block.material);
        material.setTextColor(0xFFAAAAAA);
        material.setTextSize(12);
        material.setPadding(8, 0, 0, 0);
        item.addView(material);
        
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
              }
