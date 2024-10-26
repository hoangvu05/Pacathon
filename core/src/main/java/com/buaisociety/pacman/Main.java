package com.buaisociety.pacman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cjcrafter.neat.Neat;
import com.cjcrafter.neat.NeatImpl;
import com.cjcrafter.neat.NeatPrinter;
import com.cjcrafter.neat.NeatSaver;
import com.cjcrafter.neat.Parameters;
import com.buaisociety.pacman.entity.EntityType;
import com.buaisociety.pacman.entity.PacmanEntity;
import com.buaisociety.pacman.entity.behavior.NeatPacmanBehavior;
import com.buaisociety.pacman.event.CreateMazeEvent;
import com.buaisociety.pacman.event.EntityPreSpawnEvent;
import com.buaisociety.pacman.event.GameEndEvent;
import com.buaisociety.pacman.util.EventSystem;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Welcome welcome! This is the main class for the training of pacman. This
 * class is responsible for creating the NEAT algorithm, creating the pacman
 * clients, and updating the game. The game is updated in a separate thread
 * pool to allow for multiple games to be updated at once. The NEAT algorithm
 * is evolved when all games are complete. The NEAT algorithm is saved to a
 * file after each evolution.
 */
public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private OrthographicCamera camera;

    private final @NotNull EventSystem events = new EventSystem();
    private final @NotNull Vector2i visibleGames = new Vector2i(4, 2);
    private final @NotNull List<PacmanNeatClient> managers = new ArrayList<>();
    private final int totalGames = 250;
    private GameLoop secondLoop;  // 1 update per second
    private boolean paused;
    private boolean showNetworks;
    private int frames;
    private int fps;

    // deep learning
    private Neat neat;
    private NeatPrinter neatPrinter;
    private NeatSaver neatSaver;
    private ExecutorService threadPool;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        batch = new SpriteBatch();
        camera.setToOrtho(false, 8 * 28 * visibleGames.x, 8 * 36 * visibleGames.y);
        neat = createNeat();
        neatPrinter = new NeatPrinter(neat);
        neatSaver = new NeatSaver(neat, getSaveFolder());
        secondLoop = new GameLoop(1);

        int processors = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(processors);
        System.out.println("Using " + processors + " threads");

        // When all games have ended, reset
        events.registerListener(GameEndEvent.class, event -> {
            int id = event.getGameManager().getConfig().id;
            managers.get(id).getGameCompleteFuture().complete(null);
        });

        // When a new pacman is created, set the behavior
        events.registerListener(EntityPreSpawnEvent.class, event -> {
            if (event.getEntityType() != EntityType.PACMAN)
                return;

            PacmanEntity.Config config = (PacmanEntity.Config) event.getConfig();
            int id = event.getMaze().getLevelManager().getConfig().id;
            config.behavior = new NeatPacmanBehavior(neat.getClients().get(id));
        });

        events.registerListener(CreateMazeEvent.class, SpecialTrainingConditions.onCreateMaze());
        events.registerListener(EntityPreSpawnEvent.class, SpecialTrainingConditions.onEntityPreSpawn());

        reset();
        //Gdx.graphics.setWindowedMode(8 * 28 * 8, 8 * 36 * 8);
    }

    public @NotNull Neat createNeat() {
        // Change this to true/false as needed, if you want to load from file
        if (true) {
            // TODO: Change this to the exact file you wan q t to load
            File exactFile = new File("saves" + File.separator + "oct26-24" + File.separator + "generation-91.json");
            // load exactFile contents to string
            String json;
            try {
                json = new String(Files.readAllBytes(Paths.get(exactFile.getPath())));
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid file: " + exactFile.getPath());
            }
            NeatImpl impl = NeatImpl.fromJson(json);
            // modify this as needed
            impl.updateNodeCounts(12, 4);  // Add 4 new inputs
            //impl.updateClients(200);  // have 200 pacman games at once
            return impl;
        } else {
            Parameters neatParameters = new Parameters();
            neatParameters.setMutateWeightChance(0.75f);
            neatParameters.setWeightCoefficient(1.0f);  // speciate on weight more often
            neatParameters.setTargetClientsPerSpecies(12);  // targeting ~12 clients per species
            neatParameters.setStagnationLimit(10);  // lower stagnation limit
            neatParameters.setUseBiasNode(true);  // use bias node
            // CHANGE THE PARAMETER TO # OF INPUTS
            return new NeatImpl(12,4, totalGames, neatParameters);
        }
    }

    public @NotNull File getSaveFolder() {
        // Create the "saves" directory if it doesn't exist
        File saveFolder = new File("saves");
        saveFolder.mkdirs();

        // Get the current date formatted as "oct26"
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("MMMdd")).toLowerCase();

        // Initialize the maximum number found for the current date
        int maxNumber = 0;

        // List all files in the "saves" directory
        File[] files = saveFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                // Check if the file name starts with the date pattern
                if (name.startsWith(datePart + "-")) {
                    // Extract the number part after the date
                    String numberPart = name.substring((datePart + "-").length());
                    try {
                        int num = Integer.parseInt(numberPart);
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore files that don't have a valid number suffix
                    }
                }
            }
        }

        // The next available number is maxNumber + 1
        String newFolderName = datePart + "-" + (maxNumber + 1);
        File newFolder = new File(saveFolder, newFolderName);
        newFolder.mkdirs();

        System.out.println("Created folder: " + newFolder.getPath());
        return newFolder;
    }

    public void reset() {
        for (PacmanNeatClient manager : managers) {
            manager.getGameManager().dispose();
        }
        managers.clear();

        for (int i = 0; i < totalGames; i++) {
            GameManager.Config config = new GameManager.Config();
            config.id = i;
            GameManager gameManager = new GameManager(events, config);
            gameManager.nextLevel();
            gameManager.setExtraLives(0);

            PacmanNeatClient neatClient = new PacmanNeatClient(neat, neat.getClients().get(i));
            neatClient.setGameManager(gameManager);
            this.managers.add(neatClient);
        }
    }

    @Override
    public void render() {

        paused ^= Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        showNetworks ^= Gdx.input.isKeyJustPressed(Input.Keys.TAB);

        frames++;
        fps++;

        if (secondLoop.update()) {
            System.out.println("FPS: " + fps + ", Frames: " + frames);
            fps = 0;
        }

        // If all games are complete, reset
        if (managers.stream().map(PacmanNeatClient::getGameCompleteFuture).allMatch(CompletableFuture::isDone)) {
            reset();
            System.out.println(neatPrinter.render());
            neatSaver.save();
            neat.evolve();
        }

        // Update games
        List<Future<?>> futures = new ArrayList<>();
        List<PacmanNeatClient> updatedManagers = new ArrayList<>();
        for (PacmanNeatClient manager : managers) {
            manager.setRenderNetwork(showNetworks);
            if (manager.getGameCompleteFuture().isDone())
                continue;

            if (!paused) {
                // Submit the update task and add to updatedManagers
                Future<?> future = threadPool.submit(() -> {
                    manager.getGameManager().update();
                });
                futures.add(future);
                updatedManagers.add(manager);
            }
        }

        // Wait for all games to be updated
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Call postUpdate() on the main thread for games that were updated
        for (PacmanNeatClient manager : updatedManagers) {
            manager.getGameManager().postUpdate();
        }

        // Render everything
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();

        // Get a copy of the managers list and sort by score so the best are rendered first
        List<PacmanNeatClient> sortedManagers = new ArrayList<>(managers);
        sortedManagers.sort(Comparator.comparingInt(manager -> -manager.getGameManager().getScore()));

        int renderCount = 0;
        for (PacmanNeatClient manager : managers) {
            if (manager.getGameCompleteFuture().isDone())
                continue;
            if (renderCount >= visibleGames.x * visibleGames.y)
                break;

            int gameX = renderCount % visibleGames.x;
            int gameY = renderCount / visibleGames.x;
            renderCount++;

            batch.setProjectionMatrix(camera.combined.cpy().translate(gameX * 8 * 28, gameY * 8 * 36, 0));
            manager.render(batch);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
