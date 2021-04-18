package de.kcodeyt.headsdb.util;

import cn.nukkit.form.element.ElementButtonImageData;
import de.kcodeyt.heads.util.api.Mojang;
import de.kcodeyt.heads.util.api.SkinAPI;

public class HeadRender {

    private static final String URL = "https://mc-heads.net/head/";

    private static String createUrl(String texture) {
        return URL + SkinAPI.fromBase64(texture).substring(Mojang.TEXTURES.length()) + ".png";
    }

    public static ElementButtonImageData createButtonImage(String texture) {
        return new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_URL, HeadRender.createUrl(texture));
    }

}
