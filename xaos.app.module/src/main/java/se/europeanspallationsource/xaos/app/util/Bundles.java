/*
 * Copyright 2018 European Spallation Source ERIC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.europeanspallationsource.xaos.app.util;


import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;


/**
 * @author claudio.rosati@esss.se
 */
public class Bundles {

    private static final String LOCALIZED_STRINGS_BUNDLE = "bundles.localizedStringsBundle";
    private static final Logger LOGGER = Logger.getLogger(Bundles.class.getName());

    /**
     * @return The {@link ResourceBundle} containing localized strings.
     */
    public static ResourceBundle getLocalizedStrings() {
        try {
            return ResourceBundle.getBundle(LOCALIZED_STRINGS_BUNDLE);
        } catch ( MissingResourceException mrex ) {
            LOGGER.log(SEVERE, MessageFormat.format("Unable to load \"{0}\" bundle file.", LOCALIZED_STRINGS_BUNDLE), mrex);
            return null;
        }
    }

    private Bundles() {
    }

}
