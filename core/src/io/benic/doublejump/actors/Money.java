package io.benic.doublejump.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;


public class Money extends Table {
    private int value = 0;
    
    private Label label;
    private Image box;
    
    public Money(BitmapFont font, TextureRegion boxTexture, int value) {
//        setDebug(true);
        
        this.value = value;
        
        label = new Label(Integer.toString(value), new Label.LabelStyle(font, Color.WHITE));
        label.setAlignment(Align.center);
        add(label).height(18).padRight(5);
        
        box = new Image(boxTexture);
        box.setSize(box.getWidth() / 2, box.getHeight() / 2);
        box.setOrigin(Align.center);
        box.setRotation(45);
        box.setColor(Color.WHITE);
        add(box).size(12);
    }
    
    @Override
    public float getWidth() {
        return label.getWidth() + 5 + 12;
    }
    
    @Override
    public float getHeight() {
        return label.getHeight();
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
        
        label.setText(Integer.toString(value));
    }
    
    @Override
    public void setColor(Color color) {
        super.setColor(color);
        
        label.setColor(color);
        box.setColor(color);
    }
}
