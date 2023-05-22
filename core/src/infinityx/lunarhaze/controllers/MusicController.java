package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.audio.Music;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.screens.GameSetting;

public class MusicController {

    // Singleton instance
    private static MusicController instance;

    /** Holds music settings */
    private GameSetting musicSettings;

    /// Music tracks
    private Music stealth_background;
    private Music battle_background;
    private Music lobby_background;
    private Music editor_background;

    private Music lastPlayedTrack;

    public static MusicController getInstance() {
        if (instance == null) {
            instance = new MusicController();
        }
        return instance;
    }

    /**
     * Gather the required assets.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory          Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory, GameSetting setting_preference) {
        stealth_background = directory.getEntry("stealthBackground", Music.class);
        battle_background = directory.getEntry("battleBackground", Music.class);
        lobby_background = directory.getEntry("lobbyBackground", Music.class);
        editor_background = directory.getEntry("editorBackground", Music.class);
        stealth_background.setLooping(true);
        battle_background.setLooping(true);
        lobby_background.setLooping(true);
        editor_background.setLooping(true);
        musicSettings = setting_preference;
        setVolume(setting_preference.getMusicVolume());
        setMusicEnabled(setting_preference.isMusicEnabled());
    }

    /** Sets the volume for all music tracks */
    public void setVolume(float volume) {
        stealth_background.setVolume(volume);
        battle_background.setVolume(volume);
        lobby_background.setVolume(volume);
        editor_background.setVolume(volume);
    }

    public void setMusicEnabled(boolean state) {
        if (lastPlayedTrack == null) return;
        if (state) {
            lastPlayedTrack.play();
        } else {
            lastPlayedTrack.stop();
        }
    }

    public void playStealth() {
        play(stealth_background);
    }

    public void playBattle() {
        play(battle_background);
    }

    public void playLobby() {
        play(lobby_background);
    }

    public void playEditor() {
        play(editor_background);
    }

    private void play(Music music) {
        if (!musicSettings.isMusicEnabled()) {
            lastPlayedTrack = music;
            return;
        }

        if (lastPlayedTrack != null && lastPlayedTrack != music)
            lastPlayedTrack.stop();
        music.play();
        lastPlayedTrack = music;
    }
}
