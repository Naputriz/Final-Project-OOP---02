package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.entities.*;
import com.kelompok2.frontend.managers.AssetManager;

public class CharacterListScreen extends ScreenAdapter {
    private final Main game;
    private Stage stage;
    private Skin skin;

    // UI Components
    private List<String> characterList;
    private Label nameLabel;
    private Label hpLabel, atkLabel, artsLabel, defLabel, speedLabel;
    private Label descriptionLabel;
    private Label skillNameLabel, skillDescLabel;
    private Table statsTable;
    private Table headerTable; // Held as field to update actor

    // Character Actor
    private CharacterActor characterActor;

    // Data Source: Temporary instances
    private Array<GameCharacter> characters;

    // Resolution
    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public CharacterListScreen(Main game) {
        this.game = game;
        initializeCharacters();
    }

    private void initializeCharacters() {
        characters = new Array<>();
        // Instantiate characters to get their data
        characters.add(new Ryze(0, 0));
        characters.add(new Whisperwind(0, 0));
        characters.add(new Aelita(0, 0));
        characters.add(new Aegis(0, 0));
        characters.add(new Lumi(0, 0));
        characters.add(new Alice(0, 0));
        characters.add(new Kei(0, 0));
        characters.add(new Isolde(0, 0));
        characters.add(new Insania(0, 0));
        characters.add(new Blaze(0, 0));
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top().left().pad(20);

        // --- TITLE ---
        Label titleLabel = new Label("Daftar Karakter", skin);
        titleLabel.setFontScale(0.8f);
        mainTable.add(titleLabel).colspan(2).center().padBottom(30).row();

        // --- LEFT PANEL: LIST ---
        Table leftPanel = new Table();

        characterList = new List<>(skin);
        Array<String> names = new Array<>();
        for (GameCharacter c : characters) {
            names.add(c.getClass().getSimpleName());
        }
        characterList.setItems(names);

        ScrollPane listScroll = new ScrollPane(characterList, skin);
        listScroll.setFadeScrollBars(false);
        leftPanel.add(listScroll).width(300).height(600);

        mainTable.add(leftPanel).width(350).fillY().padRight(50);

        // --- RIGHT PANEL: DETAILS ---
        Table rightPanel = new Table();
        rightPanel.top().left();

        // 1. Header (Name + Sprite)
        headerTable = new Table();
        nameLabel = new Label("Character Name", skin);
        nameLabel.setFontScale(1.2f);

        // Initial Character Actor (placeholder)
        characterActor = new CharacterActor(null);
        headerTable.add(characterActor).size(200, 200).padRight(20);
        headerTable.add(nameLabel).left();
        rightPanel.add(headerTable).left().padBottom(30).row();

        // 2. Stats
        statsTable = new Table();
        statsTable.defaults().left().padBottom(10).padRight(20);

        hpLabel = new Label("HP: -", skin);
        atkLabel = new Label("ATK: -", skin);
        artsLabel = new Label("ARTS: -", skin);
        defLabel = new Label("DEF: -", skin);
        speedLabel = new Label("SPD: -", skin);

        statsTable.add(hpLabel);
        statsTable.add(atkLabel).row();
        statsTable.add(artsLabel);
        statsTable.add(defLabel).row();
        statsTable.add(speedLabel).row();

        rightPanel.add(statsTable).left().padBottom(30).row();

        // 3. Description / Lore
        Label descTitle = new Label("Latar Belakang:", skin);
        descTitle.setColor(Color.LIGHT_GRAY);
        rightPanel.add(descTitle).left().padBottom(5).row();

        descriptionLabel = new Label("...", skin);
        descriptionLabel.setWrap(true);
        descriptionLabel.setAlignment(Align.topLeft);

        ScrollPane descScroll = new ScrollPane(descriptionLabel, skin);
        descScroll.setFadeScrollBars(false);

        rightPanel.add(descScroll).width(800).height(100).left().padBottom(15).row();

        // 4. Skill Info
        Label skillTitle = new Label("Skill:", skin);
        skillTitle.setColor(Color.CYAN);
        rightPanel.add(skillTitle).left().padBottom(5).row();

        skillNameLabel = new Label("Skill Name", skin);
        skillNameLabel.setFontScale(1.1f);
        rightPanel.add(skillNameLabel).left().padBottom(5).row();

        skillDescLabel = new Label("...", skin);
        skillDescLabel.setWrap(true);
        skillDescLabel.setAlignment(Align.topLeft);

        ScrollPane skillScroll = new ScrollPane(skillDescLabel, skin);
        skillScroll.setFadeScrollBars(false);
        rightPanel.add(skillScroll).width(800).height(100).left().row();

        mainTable.add(rightPanel).expand().fill();
        mainTable.row();

        // --- BACK BUTTON ---
        TextButton backButton = new TextButton("KEMBALI KE MENU", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        // Add at bottom left or center
        stage.addActor(backButton);
        backButton.setPosition(50, 50);
        backButton.setSize(250, 60);

        stage.addActor(mainTable);

        // --- CONNECTION LOGIC ---
        characterList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateDetails();
            }
        });

        // Select first item by default
        if (characters.size > 0) {
            characterList.setSelectedIndex(0);
            updateDetails();
        }
    }

    private void updateDetails() {
        int index = characterList.getSelectedIndex();
        if (index < 0 || index >= characters.size)
            return;

        GameCharacter charData = characters.get(index);

        // Update Name and Title
        String displayName = charData.getClass().getSimpleName();
        if (charData.getTitle() != null && !charData.getTitle().isEmpty() && !charData.getTitle().equals("Unknown")) {
            displayName += " - " + charData.getTitle();
        }
        nameLabel.setText(displayName);

        // Update Stats
        hpLabel.setText(String.format("MAX HP: %.0f", charData.getMaxHp()));
        atkLabel.setText(String.format("ATK: %.0f", charData.getAtk()));
        artsLabel.setText(String.format("ARTS: %.0f", charData.getArts()));
        defLabel.setText(String.format("DEF: %.0f", charData.getDef()));
        speedLabel.setText(String.format("SPEED: %.0f", charData.getSpeed()));

        // Update Description
        descriptionLabel
                .setText(charData.getDescription() != null ? charData.getDescription() : "No information available.");

        // Update Skill Info
        skillNameLabel.setText(charData.getSkillName() != null ? charData.getSkillName() : "Unknown Skill");
        skillDescLabel
                .setText(charData.getSkillDescription() != null ? charData.getSkillDescription() : "No skill info.");

        // Update Character Actor
        if (characterActor != null) {
            characterActor.setCharacter(charData);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        // Dispose characters
        for (GameCharacter c : characters) {
            c.dispose();
        }
    }

    // Custom Actor to render GameCharacter animations
    private class CharacterActor extends Actor {
        private GameCharacter character;

        public CharacterActor(GameCharacter character) {
            setSize(200, 200);
            setCharacter(character);
        }

        public void setCharacter(GameCharacter character) {
            this.character = character;
            if (this.character != null) {
                // Use public setters we added to GameCharacter
                this.character.setRenderWidth(200);
                this.character.setRenderHeight(200);
            }
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (character != null) {
                character.update(delta);
            }
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            if (character == null)
                return;

            // Sync Position to Actor's stage coordinates
            // We use localToStageCoordinates to find where this actor is on the screen
            com.badlogic.gdx.math.Vector2 start = localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0, 0));

            // Position the character so it's centered in the actor's box
            // Since we set renderWidth/Height to 200, and actor is 200x200, it matches
            // perfectly.
            character.setPosition(start.x, start.y);

            // Ensure character is facing right (standard for menu)
            character.setFacingRight(true);

            // Render
            if (batch instanceof SpriteBatch) {
                character.render((SpriteBatch) batch);
            }
        }
    }
}
