/*
 * Copyright 2022 KCodeYT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kcodeyt.headsdb.util;

import cn.nukkit.form.element.ElementButtonImageData;
import de.kcodeyt.heads.util.api.Mojang;
import de.kcodeyt.heads.util.api.SkinAPI;

public class HeadRender {

    private static final String URL = "https://mc-heads.net/head/";

    private static String createUrl(String texture) {
        return URL + SkinAPI.fromBase64(texture).substring(Mojang.TEXTURES.length()) + ".png";
    }

    private static String createUrlById(String textureId) {
        return URL + textureId + ".png";
    }

    public static ElementButtonImageData createButtonImage(String texture) {
        return new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_URL, HeadRender.createUrl(texture));
    }

    public static ElementButtonImageData createButtonImageById(String textureId) {
        return new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_URL, HeadRender.createUrlById(textureId));
    }

}
