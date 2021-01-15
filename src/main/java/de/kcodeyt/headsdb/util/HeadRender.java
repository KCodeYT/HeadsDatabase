package de.kcodeyt.headsdb.util;

import de.kcodeyt.heads.util.api.Mojang;
import de.kcodeyt.heads.util.api.SkinAPI;

public class HeadRender {

    private static final String URL = "https://mc-heads.net/head/";
    private static final int SIZE = 256;

    public static String createUrl(String texture) {
        return URL + SkinAPI.fromBase64(texture).substring(Mojang.TEXTURES.length()) + "/" + SIZE;
    }

}
