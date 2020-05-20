package szm.orde4c.game.base;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;
import java.util.Iterator;

public class TileMapActor extends Actor {
    public static int windowWidth = 1980;
    public static int windowHeight = 1080;

    private TiledMap tiledMap;
    private OrthographicCamera tiledCamera;
    private OrthoCachedTiledMapRenderer tiledMapRenderer;

    private int tileWidth;
    private int tileHeight;
    private int numTilesHorizontal;
    private int numTilesVertical;
    private int mapWidth;
    private int mapHeight;

    public TileMapActor(String filename) {
        tiledMap = new TmxMapLoader().load(filename);

        tileWidth = (int) tiledMap.getProperties().get("tilewidth");
        tileHeight = (int) tiledMap.getProperties().get("tileheight");
        numTilesHorizontal = (int) tiledMap.getProperties().get("width");
        numTilesVertical = (int) tiledMap.getProperties().get("height");
        mapWidth = tileWidth * numTilesHorizontal;
        mapHeight = tileHeight * numTilesVertical;
    }

    public TileMapActor(String filename, Stage stage, boolean setCamera) {
        this(filename);

        BaseActor.setWorldBounds(mapWidth, mapHeight);

        tiledMapRenderer = new OrthoCachedTiledMapRenderer(tiledMap);
        tiledMapRenderer.setBlending(true);
        tiledCamera = new OrthographicCamera();
        tiledCamera.setToOrtho(false, windowWidth, windowHeight);
        tiledCamera.update();

        stage.addActor(this);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Camera mainCamera = getStage().getCamera();
        tiledCamera.position.x = mainCamera.position.x;
        tiledCamera.position.y = mainCamera.position.y;
        tiledCamera.update();
        tiledMapRenderer.setView(tiledCamera);

        batch.end();
        tiledMapRenderer.render();
        batch.begin();
    }

    public ArrayList<MapObject> getRectangleList(String propertyName) {
        ArrayList<MapObject> list = new ArrayList<>();

        for (MapLayer layer :
                tiledMap.getLayers()) {
            for (MapObject obj :
                    layer.getObjects()) {
                if (!(obj instanceof RectangleMapObject)) {
                    continue;
                }
                MapProperties props = obj.getProperties();
                if (props.containsKey("name") && props.get("name").equals(propertyName)) {
                    list.add(obj);
                }
            }
        }
        return list;
    }

    public ArrayList<MapObject> getPolygonList(String propertyName) {
        ArrayList<MapObject> list = new ArrayList<>();

        for (MapLayer layer :
                tiledMap.getLayers()) {
            for (MapObject obj :
                    layer.getObjects()) {
                if (!(obj instanceof PolygonMapObject)) {
                    continue;
                }
                MapProperties props = obj.getProperties();
                if (props.containsKey("name") && props.get("name").equals(propertyName)) {
                    list.add(obj);
                }
            }
        }
        return list;
    }

    public ArrayList<MapObject> getTileList(String propertyName) {
        ArrayList<MapObject> list = new ArrayList<>();

        for (MapLayer layer :
                tiledMap.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if (!(obj instanceof TiledMapTileMapObject)) {
                    continue;
                }
                MapProperties props = obj.getProperties();

                TiledMapTileMapObject tmtmo = (TiledMapTileMapObject) obj;
                TiledMapTile tile = tmtmo.getTile();
                MapProperties defaultProperties = tile.getProperties();

                if (defaultProperties.containsKey("name") && defaultProperties.get("name").equals(propertyName)) {
                    list.add(obj);
                }

                Iterator<String> propertyKeys = defaultProperties.getKeys();

                while (propertyKeys.hasNext()) {
                    String key = propertyKeys.next();

                    if (props.containsKey(key)) {
                        continue;
                    } else {
                        Object value = defaultProperties.get(key);
                        props.put(key, value);
                    }
                }
            }
        }
        return list;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getNumTilesHorizontal() {
        return numTilesHorizontal;
    }

    public int getNumTilesVertical() {
        return numTilesVertical;
    }
}
