package io.benic.doublejump.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Arrays;


public class Shop extends Table {
    private static final String LOG_TAG = "Shop";

    private static final int[] UNLOCKS = new int[] { 0, 5, 10, 20, 40, 50, 60, 70, 80, 100, 150, 200 };

    public static final int FACES = 12;
    private static final int ROWS = 3;
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

    final private NinePatchDrawable buttonBg;
    final NinePatchDrawable buttonDownBg;

    private ShopListener listener;


    public Shop(TextureAtlas atlas, BitmapFont font, I18NBundle bundle, int[] unlocked, ShopListener shopListener, boolean videoAvailable) {
//        setDebug(true);

        bg = new Image(atlas.createPatch("bg_opaque"));
        bg.setSize(SIZE * FACES / ROWS, SIZE * ROWS);
        bg.setPosition(-bg.getWidth() / 2, -bg.getHeight() / 2);
        addActor(bg);

        buttonBg = new NinePatchDrawable(atlas.createPatch("background"));
        buttonDownBg = new NinePatchDrawable(atlas.createPatch("background_down"));

        for (int i = 0; i < FACES; i++) {
            final Image image = new Image(atlas.findRegion("player", i));

            buttons[i] = new ImageButton(new ImageButton.ImageButtonStyle(buttonBg, null, buttonDownBg, image.getDrawable(),
                                                                          null, null));
            if (Arrays.binarySearch(unlocked, i) < 0) {
                buttons[i].setDisabled(true);

                tint[i] = new Image(atlas.findRegion("tint"));
                tint[i].setSize(SIZE, SIZE);
                buttons[i].addActor(tint[i]);

//                costs[i] = new Label(Integer.toString(UNLOCKS[i]), new Label.LabelStyle(font, DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK));
//                costs[i].setAlignment(Align.center);
//                costs[i].setWidth(SIZE);
//                costs[i].setY(5);
                costs[i] = new Money(font, atlas.findRegion("box"), UNLOCKS[i]);
                costs[i].setPosition(SIZE / 2 - costs[i].getWidth() / 2, 5);
                buttons[i].addActor(costs[i]);
            }

            add(buttons[i]).size(SIZE).fill();
//            if (i == FACES / 2 - 1) row();
            if ((i + 1) % (FACES / ROWS) == 0) row();
        }

        videoString = bundle.get("video");
        noVideoString = bundle.get("no_video");

        video = new TextButton(videoString, new TextButton.TextButtonStyle(buttonBg, buttonDownBg, null, font));
//        video.getLabel().setAlignment(Align.left);
        video.getLabel().moveBy(10, 0);
        videoValue = new Money(font, atlas.findRegion("box"), MathUtils.random(10, 20));
        videoValue.setPosition(SIZE * FACES / ROWS - 10 - videoValue.getWidth(), SIZE / 4 - videoValue.getHeight() / 2);
        video.addActor(videoValue);

        add(video).colspan(4).height(SIZE / 2).fill();

        bg.setSize(SIZE * FACES / ROWS, ROWS * SIZE + (SIZE / 2));
        bg.setPosition(-bg.getWidth() / 2, -bg.getHeight() / 2);

        setVideoAvailable(videoAvailable);

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
            videoValue.setValue(MathUtils.random(10, 20));
        } else {
            video.setText(noVideoString);
        }
    }

    public interface ShopListener {
        void selected(int index);
        boolean bought(int index, int cost);
        void video(int value);
    }
}
