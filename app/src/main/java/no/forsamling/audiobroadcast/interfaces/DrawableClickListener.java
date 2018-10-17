package no.royalone.audiobroadcast.interfaces;

/**
 * Created by royalone on 2017-01-06.
 */

public interface DrawableClickListener {

    public static enum DrawablePosition { TOP, BOTTOM, LEFT, RIGHT };
    public void onClick(DrawablePosition target);
}
