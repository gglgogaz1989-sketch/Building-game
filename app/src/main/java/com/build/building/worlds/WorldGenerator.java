package com.build.building.worlds;

public class WorldGenerator {
    
    /**
     * Создание пустого мира с ровной картой-основой
     */
    public static WorldSaveData createEmptyWorld(String worldName) {
        WorldSaveData world = new WorldSaveData(worldName);
        
        // Добавляем только одну ровную основу (пол)
        // Вся остальная стройка — руками игрока
        
        WorldSaveData.WorldObjectData ground = new WorldSaveData.WorldObjectData("ground", 0, -0.5f, 0);
        ground.scaleX = 100f;      // большой пол
        ground.scaleZ = 100f;
        ground.scaleY = 0.2f;
        ground.material = "dirt";
        world.objects.add(ground);
        
        return world;
    }
    
    /**
     * Создание мира с заданным размером карты
     */
    public static WorldSaveData createEmptyWorld(String worldName, int mapSize) {
        WorldSaveData world = new WorldSaveData(worldName);
        
        // Ровная основа
        WorldSaveData.WorldObjectData ground = new WorldSaveData.WorldObjectData("ground", 0, -0.5f, 0);
        ground.scaleX = mapSize;
        ground.scaleZ = mapSize;
        ground.scaleY = 0.2f;
        ground.material = "dirt";
        world.objects.add(ground);
        
        return world;
    }
}
