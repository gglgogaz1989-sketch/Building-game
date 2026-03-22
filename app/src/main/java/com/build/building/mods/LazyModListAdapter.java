package com.build.building.mods;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.build.building.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LazyModListAdapter extends BaseAdapter {
    
    private Context context;
    private ModManager modManager;
    private List<Mod> allMods;
    private List<Mod> displayedMods = new ArrayList<>();
    
    private int pageSize = 10;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMore = true;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public LazyModListAdapter(Context context) {
        this.context = context;
        this.modManager = ModManager.getInstance(context);
        this.allMods = modManager.getAllMods();
        loadPage(0);
    }
    
    public void loadPage(int page) {
        if (isLoading) return;
        
        isLoading = true;
        currentPage = page;
        
        executor.execute(() -> {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, allMods.size());
            
            List<Mod> newMods = new ArrayList<>();
            for (int i = start; i < end; i++) {
                Mod mod = allMods.get(i);
                
                if (!mod.hasDetailsLoaded()) {
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {}
                    mod.setDetailsLoaded(true);
                }
                
                newMods.add(mod);
            }
            
            if (page == 0) {
                displayedMods.clear();
            }
            displayedMods.addAll(newMods);
            hasMore = end < allMods.size();
            isLoading = false;
            
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
            }
        });
    }
    
    public void loadNextPage() {
        if (hasMore && !isLoading) {
            loadPage(currentPage + 1);
        }
    }
    
    public void preloadNextPage() {
        if (hasMore && !isLoading) {
            executor.execute(() -> {
                int nextStart = (currentPage + 1) * pageSize;
                int nextEnd = Math.min(nextStart + pageSize, allMods.size());
                for (int i = nextStart; i < nextEnd; i++) {
                    Mod mod = allMods.get(i);
                    if (!mod.hasDetailsLoaded()) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {}
                        mod.setDetailsLoaded(true);
                    }
                }
            });
        }
    }
    
    @Override
    public int getCount() {
        return displayedMods.size();
    }
    
    @Override
    public Object getItem(int position) {
        return displayedMods.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return displayedMods.get(position).getModId().hashCode();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mod_list_item, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.mod_icon);
            holder.name = convertView.findViewById(R.id.mod_name);
            holder.version = convertView.findViewById(R.id.mod_version);
            holder.author = convertView.findViewById(R.id.mod_author);
            holder.description = convertView.findViewById(R.id.mod_description);
            holder.enabledSwitch = convertView.findViewById(R.id.mod_enabled_switch);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Mod mod = displayedMods.get(position);
        
        holder.name.setText(mod.getName());
        holder.version.setText("v" + mod.getVersion());
        holder.author.setText(mod.getAuthor());
        
        String description = mod.getDescription();
        if (description != null && !description.isEmpty()) {
            holder.description.setText(description);
        } else {
            holder.description.setText("Нет описания");
        }
        
        loadIconAsync(mod, holder.icon);
        
        holder.enabledSwitch.setChecked(mod.isEnabled());
        holder.enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            modManager.setModEnabled(mod.getModId(), isChecked);
        });
        
        if (position >= getCount() - 3) {
            preloadNextPage();
        }
        
        return convertView;
    }
    
    private void loadIconAsync(Mod mod, ImageView imageView) {
        executor.execute(() -> {
            Bitmap icon = null;
            
            try {
                if (mod.getIconPath() != null && !mod.getIconPath().isEmpty()) {
                    if (mod.isBuiltIn()) {
                        InputStream is = context.getAssets().open(mod.getIconPath());
                        icon = BitmapFactory.decodeStream(is);
                        is.close();
                    } else {
                        File iconFile = new File(mod.getModFolderPath(), mod.getIconPath());
                        if (iconFile.exists()) {
                            icon = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            final Bitmap finalIcon = icon;
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    if (finalIcon != null) {
                        imageView.setImageBitmap(finalIcon);
                    } else {
                        imageView.setImageResource(android.R.drawable.ic_menu_info_details);
                    }
                });
            }
        });
    }
    
    public void refresh() {
        allMods = modManager.getAllMods();
        displayedMods.clear();
        currentPage = 0;
        hasMore = true;
        loadPage(0);
    }
    
    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView version;
        TextView author;
        TextView description;
        Switch enabledSwitch;
    }
}
