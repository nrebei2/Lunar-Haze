package infinityx.lunarhaze;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.assets.AssetDirectory;

public class GameplayController {
    //TODO!!!!!!
    /** Texture for werewolf */
    private Texture werewolfTexture;
    /** Texture for all villagers, as they look the same */
    private Texture villagerTexture;

    /** Reference to player (need to change to allow multiple players) */
    private Werewolf player;

    /** The currently active object */
    private Array<GameObject> objects;

    public GameplayController() {
        player = null;
    }


    /**
     * Populates this mode from the given the directory.
     *
     * The asset directory is a dictionary that maps string keys to assets.
     * Assets can include images, sounds, and fonts (and more). This
     * method delegates to the gameplay controller
     *
     * @param directory 	Reference to the asset directory.
     */
    public void populate(AssetDirectory directory) {
        werewolfTexture = directory.getEntry("werewolf", Texture.class);
        villagerTexture = directory.getEntry("villager", Texture.class);
    }


    /**
     * Returns the list of the currently active (not destroyed) game objects
     *
     * As this method returns a reference and Lists are mutable, other classes can
     * technical modify this list.  That is a very bad idea.  Other classes should
     * only mark objects as destroyed and leave list management to this class.
     *
     * @return the list of the currently active (not destroyed) game objects
     */
    public Array<GameObject> getObjects() {
        return objects;
    }


    /**
     * Returns a reference to the currently active player.
     *
     * This property needs to be modified if you want multiple players.
     *
     * @return a reference to the currently active player.
     */
    public Werewolf getPlayer() { return player;}


    /**
     * Returns true if the currently active player is alive.
     *
     * This property needs to be modified if you want multiple players.
     *
     * @return true if the currently active player is alive.
     */
    public boolean isAlive() {
        return player != null;
    }


    /**
     * Starts a new game.
     *
     * This method creates a single player, but does nothing else.
     *
     * @param x Starting x-position for the player
     * @param y Starting y-position for the player
     */
    public void start(float x, float y) {
        // Create the player's ship
        player = new Werewolf(x, y);
        player.setTexture(werewolfTexture);

        // Player must be in object list.
        objects.add(player);
    }

    /**
     * Resets the game, deleting all objects.
     */
    public void reset() {
        player = null;
        objects.clear();
    }

    /**
     * Resolve the actions of all game objects (player and shells)
     *
     * You will probably want to modify this heavily in Part 2.
     *
     * @param input  Reference to the input controller
     * @param delta  Number of seconds since last animation frame
     */
    public void resolveActions(InputController input, float delta) {
        // Process the player
        if (player != null ) {
            resolvePlayer(input,delta);
        }
        // Process the other (non-ship) objects.
        for (GameObject o : objects) {
            o.update(delta);
        }
    }

    /**
     * Process the player's actions.
     *
     * Notice that firing bullets allocates memory to the heap.  If we were REALLY
     * worried about performance, we would use a memory pool here.
     *
     * @param input  Reference to the input controller
     * @param delta  Number of seconds since last animation frame
     */
    public void resolvePlayer(InputController input, float delta) {
        player.setMovementH(input.getHorizontal());
        player.setMovementV(input.getVertical());
        player.update(delta);
    }

}