package com.buaisociety.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.buaisociety.pacman.entity.EntityType;
import com.buaisociety.pacman.entity.PacmanEntity;
import com.buaisociety.pacman.entity.behavior.Behavior;
import com.buaisociety.pacman.entity.behavior.TournamentBehavior;
import com.buaisociety.pacman.event.EntityPreSpawnEvent;
import com.buaisociety.pacman.util.EventListener;
import com.buaisociety.pacman.util.EventSystem;
import com.cjcrafter.neat.compute.SimpleCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hey there! You can use this file to test your best client against the tests
 * we will do live during the tournament!
 */
public class Tournament extends ApplicationAdapter {

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private final EventSystem events = new EventSystem();
    private GameManager gameManager;
    private GameLoop gameLoop;
    private GameLoop secondLoop;  // 1 update per second
    private int frames;
    private int fps;

    /**
     * This is where you can instantiate your behavior you have been working
     * on. This is the spot where you can setup your neural network.
     *
     * @return The behavior to submit to the tournament
     */
    public Behavior setupBehavior() {
        // TODO: Choose your best client here
        File file = new File("saves" + File.separator + "oct26-24" + File.separator + "best-calculator-91.json");
        if (!file.exists()) {
            System.err.println("Could not find the file: " + file.getAbsolutePath());
            return null;
        }

        String json;
        try {
            json = new String(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        SimpleCalculator calculator = SimpleCalculator.fromJson(json);
        return new TournamentBehavior(calculator);
    }

    @Override
    public void create() {
        // Whenever a new pacman is created, make sure it uses the neural network
        events.registerListener(EntityPreSpawnEvent.class, new EventListener<>() {
            Behavior neuralNetworkBehavior = setupBehavior();

            @Override
            public void onEvent(@NotNull EntityPreSpawnEvent event) {
                if (neuralNetworkBehavior == null) {
                    System.err.println("Could not find any behavior");
                    return;
                }

                if (event.getEntityType() == EntityType.PACMAN) {
                    PacmanEntity.Config config = (PacmanEntity.Config) event.getConfig();
                    config.behavior = neuralNetworkBehavior;
                }
            }
        });

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 8 * 28, 8 * 36);

        viewport = new FitViewport(8 * 28, 8 * 36, camera);  // Initialize viewport
        viewport.apply(true);  // Center the camera

        batch = new SpriteBatch();

        GameManager.Config config = new GameManager.Config();
        config.handicap = 8;
        config.levelsPreset = "tournament_levels.json";
        gameManager = new GameManager(events, config);
        gameManager.nextLevel();

        gameLoop = new GameLoop(60);
        secondLoop = new GameLoop(1);

        // Maximize window
        Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height);
    }

    @Override
    public void render() {
        if (!gameLoop.update())
            return;

        frames++;
        fps++;

        if (secondLoop.update()) {
            System.out.println("FPS: " + fps + ", Frames: " + frames);
            fps = 0;
        }

        gameManager.update();
        gameManager.postUpdate();
        ScreenUtils.clear(0, 0, 0, 1);

        // Apply the viewport and update the camera
        viewport.apply();
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        gameManager.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);  // Center the camera during resize
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
