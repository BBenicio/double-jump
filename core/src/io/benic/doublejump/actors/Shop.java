package io.benic.doublejump.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Arrays;


public class Shop extends Table {
    private static final String LOG_TAG = "Shop";

    private static final int[] UNLOCKS = new int[] { 0, 5, 10, 20, 40, 50, 60, 70, 80, 100, 150, 200 };

    public static final int FACES = 12;
    private static final int ROWS = 3;
    private static final int COLUMNS = FACES / ROWS;
    private static final float SIZE = 100.0f;

    private final String videoString;
    private final String noVideoString;

    private int selected = 0;
    private ImageButton[] buttons = new ImageButton[FACES];
    private Image[] tint = new Image[FACES];
    private Money[] costs = new Money[FACES];

    private TextButton video;
    private boolean videoAvailable;
    private Money videoValue;

    private Image bg;

    private TextButton[] tabButtons = new TextButton[2];
    private int tab = 0;
    private ButtonGroup<TextButton> tabs;

    private Container<Table> content;
    private Table skins;
    private Table consumables;

    private ShopListener listener;


    public Shop(TextureAtlas atlas, BitmapFont font, I18NBundle bundle, int[] unlocked, ShopListener shopListener, boolean videoAvailable) {
        // setDebug(true);

        bg = new Image(atlas.createPatch("bg_opaque"));
        bg.setSize(SIZE * COLUMNS, SIZE * ROWS + SIZE / 2);
        bg.setPosition(-bg.getWidth() / 2, -bg.getHeight() / 2);
        addActor(bg);

        final NinePatchDrawable buttonBg = new NinePatchDrawable(atlas.createPatch("background"));
        final NinePatchDrawable buttonDownBg = new NinePatchDrawable(atlas.createPatch("background_down"));

        /*tabs = new ButtonGroup<TextButton>();
        tabButtons[0] = new TextButton("Skins", new TextButton.TextButtonStyle(buttonBg, null, buttonDownBg, font));
        tabButtons[1] = new TextButton("Consumables", new TextButton.TextButtonStyle(buttonBg, null, buttonDownBg, font));
        tabs.add(tabButtons[0]);
        tabs.add(tabButtons[1]);
        add(tabButtons[0]).colspan(COLUMNS / 2).height(40).fill();
        add(tabButtons[1]).colspan(COLUMNS / 2).height(40).fill();
        row();*/

        skins = new Table();
        content = new Container<Table>(skins);
        for (int i = 0; i < FACES; i++) {
            final Image image = new Image(atlas.findRegion("player", i));

            buttons[i] = new ImageButton(new ImageButton.ImageButtonStyle(buttonBg, null, buttonDownBg, image.getDrawable(),
                                                                          null, null));
            if (Arrays.binarySearch(unlocked, i) < 0) {
                buttons[i].setDisabled(true);

                tint[i] = new Image(atlas.findRegion("tint"));
                tint[i].setSize(SIZE, SIZE);
                buttons[i].addActor(tint[i]);

                costs[i] = new Money(font, atlas.findRegion("box"), UNLOCKS[i]);
                costs[i].setPosition(SIZE / 2 - costs[i].getWidth() / 2, 5);
                buttons[i].addActor(costs[i]);
            }

            skins.add(buttons[i]).size(SIZE).fill();
            if ((i + 1) % (FACES / ROWS) == 0) skins.row();
        }


        add(content).colspan(COLUMNS);
        row();

        videoString = bundle.get("video");
        noVideoString = bundle.get("no_video");

        video = new TextButton(videoString, new TextButton.TextButtonStyle(buttonBg, buttonDownBg, null, font));
//        video.getLabel().setAlignment(Align.left);
        video.getLabel().moveBy(10, 0);
        videoValue = new Money(font, atlas.findRegion("box"), MathUtils.random(10, 15));
        videoValue.setPosition(SIZE * FACES / ROWS - 10 - videoValue.getWidth(), SIZE / 4 - videoValue.getHeight() / 2);
        video.addActor(videoValue);

        add(video).colspan(4).height(SIZE / 2).fill();

        setVideoAvailable(videoAvailable, 10);

        listener = shopListener;
    }

    public boolean isVideoAvailable() {
        return videoAvailable;
    }

    public int getSelected() {
        return selected;
    }

    public int getVideoValue() {
        return videoValue.getValue();
    }

    public void setSelected(int selected) {
        buttons[this.selected].setChecked(false);
        this.selected = selected;
        buttons[selected].setChecked(true);
        Gdx.app.log(LOG_TAG, "selected item " + selected);

        listener.selected(selected);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        /*if (tabs.getCheckedIndex() != tab) {
            Gdx.app.log(LOG_TAG, "change tab to " + tabs.getCheckedIndex());
            tab = tabs.getCheckedIndex();
        }*/

        if (!buttons[selected].isChecked()) {
            buttons[selected].setChecked(true);
        }

        for (int i = 0; i < FACES; i++) {
            if (!buttons[i].isChecked() && !buttons[i].isDisabled() && buttons[i].isPressed()) {
                setSelected(i);
                break;
            } else if (buttons[i].isDisabled() && buttons[i].isPressed()) {
                if (listener.bought(i, UNLOCKS[i])) {
                    buttons[i].setDisabled(false);
                    tint[i].setVisible(false);
                    costs[i].setVisible(false);
                    setSelected(i);
                }
            }
        }

        if (videoAvailable && video.isPressed()) {
            setVideoAvailable(false);

            listener.video(videoValue.getValue());
        }
    }

    public void setVideoAvailable(boolean videoAvailable) {
        this.videoAvailable = videoAvailable;

        video.setDisabled(!videoAvailable);
        videoValue.setVisible(videoAvailable);
        if (videoAvailable) {
            video.setText(videoString);
            videoValue.setValue(MathUtils.random(10, 15));
        } else {
            video.setText(noVideoString);
        }
    }

    public void setVideoAvailable(boolean videoAvailable, int value) {
        setVideoAvailable(videoAvailable);
        videoValue.setValue(value);
    }

    public boolean containsPoint(float x, float y) {
        return x >= getX() && x <= getRight() && y >= getY() && y <= getTop();
    }

    public interface ShopListener {
        void selected(int index);
        boolean bought(int index, int cost);
        void video(int value);
    }
}
