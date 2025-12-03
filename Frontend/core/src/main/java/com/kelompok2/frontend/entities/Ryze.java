package com.kelompok2.frontend.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Ryze extends GameCharacter {

    public Ryze(float x, float y) {
        super(x, y, 200f, 100f); // Stats sementara, HP ama Speed dulu buat testing

        // Sementara, nanti diganti
        this.texture = new Texture(Gdx.files.internal("ryze_placeholder.png"));

        // Ukuran hitbox
        this.bounds.setSize(256, 256);
    }

    @Override
    public void performInnateSkill() {
        // Implementasi skill Spectral Body nanti di sini
        System.out.println("Ryze pake skill");
    }
}
