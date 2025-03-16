package fr.poulpogaz.musictagger.opus;

public enum CoverType {
    OTHER("Other"),
    PNG_32x32_ICON("32x32 pixels 'file icon' (PNG only)"),
    ICON("Other file icon"),
    COVER_FRONT("Cover (front)"),
    COVER_BACK("Cover (back)"),
    LEAFLET_PAGE("Leaflet page"),
    MEDIA("Media (e.g. label side of CD)"),
    LEAD_ARTIST("Lead artist/lead performer/soloist"),
    ARTIST("Artist/performer"),
    CONDUCTOR("Conductor"),
    BAND("Band/Orchestra"),
    COMPOSER("Composer"),
    LYRICIST("Lyricist/text writer"),
    RECORDING_LOCATION("Recording Location"),
    DURING_RECORDING("During recording"),
    DURING_PERFORMANCE("During performance"),
    MOVIE_VIDEO_SCREEN_CAPTURE("Movie/video screen capture"),
    A_BRIGHT_COLORED_FISH("A bright coloured fish"),
    ILLUSTRATION("Illustration"),
    BAND_LOGOTYPE("Band/artist logotype"),
    PUBLISHER_LOGOTYPE("Publisher/Studio logotype");

    private final String fullDescription;

    CoverType(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }
}