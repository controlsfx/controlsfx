/**
 * Copyright (c) 2013,2014 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.glyphfont;

import java.io.InputStream;
import java.util.Arrays;


/**
 * Defines a {@link GlyphFont} for the FontAwesome font set (see
 * <a href="http://fortawesome.github.io/Font-Awesome/">the FontAwesome website</a>
 * for more details). Note that at present the FontAwesome font is not distributed
 * with ControlsFX, and is, by default, instead loaded from a CDN at runtime.
 *
 * <p>To use FontAwesome (or indeed any glyph font) in your JavaFX application,
 * you firstly have to get access to the FontAwesome glyph font. You do this by
 * doing the following:
 *
 * <pre>GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");</pre>
 *
 * <p>This code works because all glyph fonts are found dynamically at runtime
 * by the {@link GlyphFontRegistry} class, so you can simply request the font
 * set you want from there.
 *
 * <p>Once the font set has been loaded, you can simply start creating
 * {@link Glyph} nodes and place them in your user interface. For example:
 *
 * <pre>new Button("", fontAwesome.create(&#92;uf013).fontColor(Color.RED));</pre>
 *
 * <p>Of course, this requires you to know that <code>&#92;uf013</code> maps to
 * a 'gear' icon, which is not always intuitive (especially when you re-read the
 * code in the future). A simpler approach is to do the following:
 *
 * <pre>new Button("", fontAwesome.create(FontAwesome.Glyph.GEAR));</pre>
 * or
 * <pre>new Button("", fontAwesome.create("GEAR"));</pre>
 *
 * It is possible to achieve the same result without creating a reference to icon font by simply using
 * {@link org.controlsfx.glyphfont.Glyph} constructor
 *
 * <pre>new Button("", new Glyph("FontAwesome","GEAR");</pre>
 *
 * You can use the above Glyph class also in FXML and set the
 * fontFamily and icon property there.
 *
 * @see GlyphFont
 * @see GlyphFontRegistry
 * @see Glyph
 */
public class FontAwesome extends GlyphFont {

    private static String fontName = "FontAwesome"; //$NON-NLS-1$


    /**
     * The individual glyphs offered by the FontAwesome font.
     */
    public static enum Glyph implements INamedCharacter {

        GLASS('\uf000'),
        MUSIC('\uf001'),
        SEARCH('\uf002'),
        ENVELOPE_ALT('\uf003'),
        HEART('\uf004'),
        STAR('\uf005'),
        STAR_ALT('\uf006'),
        USER('\uf007'),
        FILM('\uf008'),
        TH_LARGE('\uf009'),
        TH('\uf00a'),
        TH_LIST('\uf00b'),
        CHECK('\uf00c'),
        TIMES('\uf00d'),
        SEARCH_PLUS('\uf00e'),
        SEARCH_MINUS('\uf010'),
        POWER_OFF('\uf011'),
        SIGNAL('\uf012'),
        GEAR('\uf013'),
        COG('\uf013'),
        TRASH_ALT('\uf014'),
        HOME('\uf015'),
        FILE_ALT('\uf016'),
        CLOCK_ALT('\uf017'),
        ROAD('\uf018'),
        DOWNLOAD('\uf019'),
        ARROW_CIRCLE_ALT_DOWN('\uf01a'),
        ARROW_CIRCLE_ALT_UP('\uf01b'),
        INBOX('\uf01c'),
        PLAY_CIRCLE_ALT('\uf01d'),
        ROTATE_RIGHT('\uf01e'),
        REPEAT('\uf01e'),
        REFRESH('\uf021'),
        LIST_ALT('\uf022'),
        LOCK('\uf023'),
        FLAG('\uf024'),
        HEADPHONES('\uf025'),
        VOLUME_OFF('\uf026'),
        VOLUME_DOWN('\uf027'),
        VOLUME_UP('\uf028'),
        QRCODE('\uf029'),
        BARCODE('\uf02a'),
        TAG('\uf02b'),
        TAGS('\uf02c'),
        BOOK('\uf02d'),
        BOOKMARK('\uf02e'),
        PRINT('\uf02F'),
        CAMERA('\uf030'),
        FONT('\uf031'),
        BOLD('\uf032'),
        ITALIC('\uf033'),
        TEXT_HEIGHT('\uf034'),
        TEXT_WIDTH('\uf035'),
        ALIGN_LEFT('\uf036'),
        ALIGN_CENTER('\uf037'),
        ALIGN_RIGHT('\uf038'),
        ALIGN_JUSTIFY('\uf039'),
        LIST('\uf03a'),
        DEDENT('\uf03b'),
        OUTDENT('\uf03b'),
        INDENT('\uf03c'),
        VIDEO_CAMERA('\uf03d'),
        PHOTO('\uf03e'),
        IMAGE('\uf03e'),
        PICTURE_ALT('\uf03e'),
        PENCIL('\uf040'),
        MAP_MARKER('\uf041'),
        ADJUST('\uf042'),
        TINT('\uf043'),
        EDIT('\uf044'),
        PENCIL_SQUARE_ALT('\uf044'),
        SHARE_SQUARE_ALT('\uf045'),
        CHECK_SQUARE_ALT('\uf046'),
        ARROWS('\uf047'),
        STEP_BACKWARD('\uf048'),
        FAST_BACKWARD('\uf049'),
        BACKWARD('\uf04a'),
        PLAY('\uf04b'),
        PAUSE('\uf04c'),
        STOP('\uf04d'),
        FORWARD('\uf04e'),
        FAST_FORWARD('\uf050'),
        STEP_FORWARD('\uf051'),
        EJECT('\uf052'),
        CHEVRON_LEFT('\uf053'),
        CHEVRON_RIGHT('\uf054'),
        PLUS_CIRCLE('\uf055'),
        MINUS_CIRCLE('\uf056'),
        TIMES_CIRCLE('\uf057'),
        CHECK_CIRCLE('\uf058'),
        QUESTION_CIRCLE('\uf059'),
        INFO_CIRCLE('\uf05a'),
        CROSSHAIRS('\uf05b'),
        TIMES_CIRCLE_ALT('\uf05c'),
        CHECK_CIRCLE_ALT('\uf05d'),
        BAN('\uf05e'),
        ARROW_LEFT('\uf060'),
        ARROW_RIGHT('\uf061'),
        ARROW_UP('\uf062'),
        ARROW_DOWN('\uf063'),
        MAIL_FORWARD('\uf064'),
        SHARE('\uf064'),
        EXPAND('\uf065'),
        COMPRESS('\uf066'),
        PLUS('\uf067'),
        MINUS('\uf068'),
        ASTERISK('\uf069'),
        EXCLAMATION_CIRCLE('\uf06a'),
        GIFT('\uf06b'),
        LEAF('\uf06c'),
        FIRE('\uf06d'),
        EYE('\uf06e'),
        EYE_SLASH('\uf070'),
        WARNING('\uf071'),
        EXCLAMATION_TRIANGLE('\uf071'),
        PLANE('\uf072'),
        CALENDAR('\uf073'),
        RANDOM('\uf074'),
        COMMENT('\uf075'),
        MAGNET('\uf076'),
        CHEVRON_UP('\uf077'),
        CHEVRON_DOWN('\uf078'),
        RETWEET('\uf079'),
        SHOPPING_CART('\uf07a'),
        FOLDER('\uf07b'),
        FOLDER_OPEN('\uf07c'),
        ARROWS_V('\uf07d'),
        ARROWS_H('\uf07e'),
        BAR_CHART_ALT('\uf080'),
        TWITTER_SQUARE('\uf081'),
        FACEBOOK_SQUARE('\uf082'),
        CAMERA_RETRO('\uf083'),
        KEY('\uf084'),
        GEARS('\uf085'),
        COGS('\uf085'),
        COMMENTS('\uf086'),
        THUMBS_ALT_UP('\uf087'),
        THUMBS_ALT_DOWN('\uf088'),
        STAR_HALF('\uf089'),
        HEART_ALT('\uf08a'),
        SIGN_OUT('\uf08b'),
        LINKEDIN_SQUARE('\uf08c'),
        THUMB_TACK('\uf08d'),
        EXTERNAL_LINK('\uf08e'),
        SIGN_IN('\uf090'),
        TROPHY('\uf091'),
        GITHUB_SQUARE('\uf092'),
        UPLOAD('\uf093'),
        LEMON_ALT('\uf094'),
        PHONE('\uf095'),
        SQUARE_ALT('\uf096'),
        BOOKMARK_ALT('\uf097'),
        PHONE_SQUARE('\uf098'),
        TWITTER('\uf099'),
        FACEBOOK('\uf09a'),
        GITHUB('\uf09b'),
        UNLOCK('\uf09c'),
        CREDIT_CARD('\uf09d'),
        RSS('\uf09e'),
        HDD_ALT('\uf0a0'),
        BULLHORN('\uf0a1'),
        BELL('\uf0f3'),
        CERTIFICATE('\uf0a3'),
        HAND_ALT_RIGHT('\uf0a4'),
        HAND_ALT_LEFT('\uf0a5'),
        HAND_ALT_UP('\uf0a6'),
        HAND_ALT_DOWN('\uf0a7'),
        ARROW_CIRCLE_LEFT('\uf0a8'),
        ARROW_CIRCLE_RIGHT('\uf0a9'),
        ARROW_CIRCLE_UP('\uf0aa'),
        ARROW_CIRCLE_DOWN('\uf0ab'),
        GLOBE('\uf0ac'),
        WRENCH('\uf0ad'),
        TASKS('\uf0ae'),
        FILTER('\uf0b0'),
        BRIEFCASE('\uf0b1'),
        ARROWS_ALT('\uf0b2'),
        GROUP('\uf0c0'),
        USERS('\uf0c0'),
        CHAIN('\uf0c1'),
        LINK('\uf0c1'),
        CLOUD('\uf0c2'),
        FLASK('\uf0c3'),
        CUT('\uf0c4'),
        SCISSORS('\uf0c4'),
        COPY('\uf0c5'),
        FILES_ALT('\uf0c5'),
        PAPERCLIP('\uf0c6'),
        SAVE('\uf0c7'),
        FLOPPY_ALT('\uf0c7'),
        SQUARE('\uf0c8'),
        NAVICON('\uf0c9'),
        REORDER('\uf0c9'),
        BARS('\uf0c9'),
        LIST_UL('\uf0ca'),
        LIST_OL('\uf0cb'),
        STRIKETHROUGH('\uf0cc'),
        UNDERLINE('\uf0cd'),
        TABLE('\uf0ce'),
        MAGIC('\uf0d0'),
        TRUCK('\uf0d1'),
        PINTEREST('\uf0d2'),
        PINTEREST_SQUARE('\uf0d3'),
        GOOGLE_PLUS_SQUARE('\uf0d4'),
        GOOGLE_PLUS('\uf0d5'),
        MONEY('\uf0d6'),
        CARET_DOWN('\uf0d7'),
        CARET_UP('\uf0d8'),
        CARET_LEFT('\uf0d9'),
        CARET_RIGHT('\uf0da'),
        COLUMNS('\uf0db'),
        UNSORTED('\uf0dc'),
        SORT('\uf0dc'),
        SORT_DOWN('\uf0dd'),
        SORT_DESC('\uf0dd'),
        SORT_UP('\uf0de'),
        SORT_ASC('\uf0de'),
        ENVELOPE('\uf0e0'),
        LINKEDIN('\uf0e1'),
        ROTATE_LEFT('\uf0e2'),
        UNDO('\uf0e2'),
        LEGAL('\uf0e3'),
        GAVEL('\uf0e3'),
        DASHBOARD('\uf0e4'),
        TACHOMETER('\uf0e4'),
        COMMENT_ALT('\uf0e5'),
        COMMENTS_ALT('\uf0e6'),
        FLASH('\uf0e7'),
        BOLT('\uf0e7'),
        SITEMAP('\uf0e8'),
        UMBRELLA('\uf0e9'),
        PASTE('\uf0ea'),
        CLIPBOARD('\uf0ea'),
        LIGHTBULB_ALT('\uf0eb'),
        EXCHANGE('\uf0ec'),
        CLOUD_DOWNLOAD('\uf0ed'),
        CLOUD_UPLOAD('\uf0ee'),
        USER_MD('\uf0f0'),
        STETHOSCOPE('\uf0f1'),
        SUITCASE('\uf0f2'),
        BELL_ALT('\uf0a2'),
        COFFEE('\uf0f4'),
        CUTLERY('\uf0f5'),
        FILE_TEXT_ALT('\uf0f6'),
        BUILDING_ALT('\uf0f7'),
        HOSPITAL_ALT('\uf0f8'),
        AMBULANCE('\uf0f9'),
        MEDKIT('\uf0fa'),
        FIGHTER_JET('\uf0fb'),
        BEER('\uf0fc'),
        H_SQUARE('\uf0fd'),
        PLUS_SQUARE('\uf0fe'),
        ANGLE_DOUBLE_LEFT('\uf100'),
        ANGLE_DOUBLE_RIGHT('\uf101'),
        ANGLE_DOUBLE_UP('\uf102'),
        ANGLE_DOUBLE_DOWN('\uf103'),
        ANGLE_LEFT('\uf104'),
        ANGLE_RIGHT('\uf105'),
        ANGLE_UP('\uf106'),
        ANGLE_DOWN('\uf107'),
        DESKTOP('\uf108'),
        LAPTOP('\uf109'),
        TABLET('\uf10a'),
        MOBILE_PHONE('\uf10b'),
        MOBILE('\uf10b'),
        CIRCLE_ALT('\uf10c'),
        QUOTE_LEFT('\uf10d'),
        QUOTE_RIGHT('\uf10e'),
        SPINNER('\uf110'),
        CIRCLE('\uf111'),
        MAIL_REPLY('\uf112'),
        REPLY('\uf112'),
        GITHUB_ALT('\uf113'),
        FOLDER_ALT('\uf114'),
        FOLDER_OPEN_ALT('\uf115'),
        SMILE_ALT('\uf118'),
        FROWN_ALT('\uf119'),
        MEH_ALT('\uf11a'),
        GAMEPAD('\uf11b'),
        KEYBOARD_ALT('\uf11c'),
        FLAG_ALT('\uf11d'),
        FLAG_CHECKERED('\uf11e'),
        TERMINAL('\uf120'),
        CODE('\uf121'),
        MAIL_REPLY_ALL('\uf122'),
        REPLY_ALL('\uf122'),
        STAR_HALF_EMPTY('\uf123'),
        STAR_HALF_FULL('\uf123'),
        STAR_HALF_ALT('\uf123'),
        LOCATION_ARROW('\uf124'),
        CROP('\uf125'),
        CODE_FORK('\uf126'),
        UNLINK('\uf127'),
        CHAIN_BROKEN('\uf127'),
        QUESTION('\uf128'),
        INFO('\uf129'),
        EXCLAMATION('\uf12a'),
        SUPERSCRIPT('\uf12b'),
        SUBSCRIPT('\uf12c'),
        ERASER('\uf12d'),
        PUZZLE_PIECE('\uf12e'),
        MICROPHONE('\uf130'),
        MICROPHONE_SLASH('\uf131'),
        SHIELD('\uf132'),
        CALENDAR_ALT('\uf133'),
        FIRE_EXTINGUISHER('\uf134'),
        ROCKET('\uf135'),
        MAXCDN('\uf136'),
        CHEVRON_CIRCLE_LEFT('\uf137'),
        CHEVRON_CIRCLE_RIGHT('\uf138'),
        CHEVRON_CIRCLE_UP('\uf139'),
        CHEVRON_CIRCLE_DOWN('\uf13a'),
        HTML5('\uf13b'),
        CSS3('\uf13c'),
        ANCHOR('\uf13d'),
        UNLOCK_ALT('\uf13e'),
        BULLSEYE('\uf140'),
        ELLIPSIS_H('\uf141'),
        ELLIPSIS_V('\uf142'),
        RSS_SQUARE('\uf143'),
        PLAY_CIRCLE('\uf144'),
        TICKET('\uf145'),
        MINUS_SQUARE('\uf146'),
        MINUS_SQUARE_ALT('\uf147'),
        LEVEL_UP('\uf148'),
        LEVEL_DOWN('\uf149'),
        CHECK_SQUARE('\uf14a'),
        PENCIL_SQUARE('\uf14b'),
        EXTERNAL_LINK_SQUARE('\uf14c'),
        SHARE_SQUARE('\uf14d'),
        COMPASS('\uf14e'),
        TOGGLE_DOWN('\uf150'),
        CARET_SQUARE_ALT_DOWN('\uf150'),
        TOGGLE_UP('\uf151'),
        CARET_SQUARE_ALT_UP('\uf151'),
        TOGGLE_RIGHT('\uf152'),
        CARET_SQUARE_ALT_RIGHT('\uf152'),
        EURO('\uf153'),
        EUR('\uf153'),
        GBP('\uf154'),
        DOLLAR('\uf155'),
        USD('\uf155'),
        RUPEE('\uf156'),
        INR('\uf156'),
        CNY('\uf157'),
        RMB('\uf157'),
        YEN('\uf157'),
        JPY('\uf157'),
        RUBLE('\uf158'),
        ROUBLE('\uf158'),
        RUB('\uf158'),
        WON('\uf159'),
        KRW('\uf159'),
        BITCOIN('\uf15a'),
        BTC('\uf15a'),
        FILE('\uf15b'),
        FILE_TEXT('\uf15c'),
        SORT_ALPHA_ASC('\uf15d'),
        SORT_ALPHA_DESC('\uf15e'),
        SORT_AMOUNT_ASC('\uf160'),
        SORT_AMOUNT_DESC('\uf161'),
        SORT_NUMERIC_ASC('\uf162'),
        SORT_NUMERIC_DESC('\uf163'),
        THUMBS_UP('\uf164'),
        THUMBS_DOWN('\uf165'),
        YOUTUBE_SQUARE('\uf166'),
        YOUTUBE('\uf167'),
        XING('\uf168'),
        XING_SQUARE('\uf169'),
        YOUTUBE_PLAY('\uf16a'),
        DROPBOX('\uf16b'),
        STACK_OVERFLOW('\uf16c'),
        INSTAGRAM('\uf16d'),
        FLICKR('\uf16e'),
        ADN('\uf170'),
        BITBUCKET('\uf171'),
        BITBUCKET_SQUARE('\uf172'),
        TUMBLR('\uf173'),
        TUMBLR_SQUARE('\uf174'),
        LONG_ARROW_DOWN('\uf175'),
        LONG_ARROW_UP('\uf176'),
        LONG_ARROW_LEFT('\uf177'),
        LONG_ARROW_RIGHT('\uf178'),
        APPLE('\uf179'),
        WINDOWS('\uf17a'),
        ANDROID('\uf17b'),
        LINUX('\uf17c'),
        DRIBBBLE('\uf17d'),
        SKYPE('\uf17e'),
        FOURSQUARE('\uf180'),
        TRELLO('\uf181'),
        FEMALE('\uf182'),
        MALE('\uf183'),
        GITTIP('\uf184'),
        SUN_ALT('\uf185'),
        MOON_ALT('\uf186'),
        ARCHIVE('\uf187'),
        BUG('\uf188'),
        VK('\uf189'),
        WEIBO('\uf18a'),
        RENREN('\uf18b'),
        PAGELINES('\uf18c'),
        STACK_EXCHANGE('\uf18d'),
        ARROW_CIRCLE_ALT_RIGHT('\uf18e'),
        ARROW_CIRCLE_ALT_LEFT('\uf190'),
        TOGGLE_LEFT('\uf191'),
        CARET_SQUARE_ALT_LEFT('\uf191'),
        DOT_CIRCLE_ALT('\uf192'),
        WHEELCHAIR('\uf193'),
        VIMEO_SQUARE('\uf194'),
        TURKISH_LIRA('\uf195'),
        TRY('\uf195'),
        PLUS_SQUARE_ALT('\uf196'),
        SPACE_SHUTTLE('\uf197'),
        SLACK('\uf198'),
        ENVELOPE_SQUARE('\uf199'),
        WORDPRESS('\uf19a'),
        OPENID('\uf19b'),
        INSTITUTION('\uf19c'),
        BANK('\uf19c'),
        UNIVERSITY('\uf19c'),
        MORTAR_BOARD('\uf19d'),
        GRADUATION_CAP('\uf19d'),
        YAHOO('\uf19e'),
        GOOGLE('\uf1a0'),
        REDDIT('\uf1a1'),
        REDDIT_SQUARE('\uf1a2'),
        STUMBLEUPON_CIRCLE('\uf1a3'),
        STUMBLEUPON('\uf1a4'),
        DELICIOUS('\uf1a5'),
        DIGG('\uf1a6'),
        PIED_PIPER_SQUARE('\uf1a7'),
        PIED_PIPER('\uf1a7'),
        PIED_PIPER_ALT('\uf1a8'),
        DRUPAL('\uf1a9'),
        JOOMLA('\uf1aa'),
        LANGUAGE('\uf1ab'),
        FAX('\uf1ac'),
        BUILDING('\uf1ad'),
        CHILD('\uf1ae'),
        PAW('\uf1b0'),
        SPOON('\uf1b1'),
        CUBE('\uf1b2'),
        CUBES('\uf1b3'),
        BEHANCE('\uf1b4'),
        BEHANCE_SQUARE('\uf1b5'),
        STEAM('\uf1b6'),
        STEAM_SQUARE('\uf1b7'),
        RECYCLE('\uf1b8'),
        AUTOMOBILE('\uf1b9'),
        CAR('\uf1b9'),
        CAB('\uf1ba'),
        TAXI('\uf1ba'),
        TREE('\uf1bb'),
        SPOTIFY('\uf1bc'),
        DEVIANTART('\uf1bd'),
        SOUNDCLOUD('\uf1be'),
        DATABASE('\uf1c0'),
        FILE_PDF_ALT('\uf1c1'),
        FILE_WORD_ALT('\uf1c2'),
        FILE_EXCEL_ALT('\uf1c3'),
        FILE_POWERPOINT_ALT('\uf1c4'),
        FILE_PHOTO_ALT('\uf1c5'),
        FILE_PICTURE_ALT('\uf1c5'),
        FILE_IMAGE_ALT('\uf1c5'),
        FILE_ZIP_ALT('\uf1c6'),
        FILE_ARCHIVE_ALT('\uf1c6'),
        FILE_SOUND_ALT('\uf1c7'),
        FILE_AUDIO_ALT('\uf1c7'),
        FILE_MOVIE_ALT('\uf1c8'),
        FILE_VIDEO_ALT('\uf1c8'),
        FILE_CODE_ALT('\uf1c9'),
        VINE('\uf1ca'),
        CODEPEN('\uf1cb'),
        JSFIDDLE('\uf1cc'),
        LIFE_BOUY('\uf1cd'),
        LIFE_SAVER('\uf1cd'),
        SUPPORT('\uf1cd'),
        LIFE_RING('\uf1cd'),
        CIRCLE_ALT_NOTCH('\uf1ce'),
        RA('\uf1d0'),
        REBEL('\uf1d0'),
        GE('\uf1d1'),
        EMPIRE('\uf1d1'),
        GIT_SQUARE('\uf1d2'),
        GIT('\uf1d3'),
        HACKER_NEWS('\uf1d4'),
        TENCENT_WEIBO('\uf1d5'),
        QQ('\uf1d6'),
        WECHAT('\uf1d7'),
        WEIXIN('\uf1d7'),
        SEND('\uf1d8'),
        PAPER_PLANE('\uf1d8'),
        SEND_ALT('\uf1d9'),
        PAPER_PLANE_ALT('\uf1d9'),
        HISTORY('\uf1da'),
        CIRCLE_THIN('\uf1db'),
        HEADER('\uf1dc'),
        PARAGRAPH('\uf1dd'),
        SLIDERS('\uf1de'),
        SHARE_ALT('\uf1e0'),
        SHARE_ALT_SQUARE('\uf1e1'),
        BOMB('\uf1e2');

        private final char ch;

        /**
         * Creates a named Glyph mapped to the given character
         * @param ch
         */
        Glyph( char ch ) {
            this.ch = ch;
        }

        @Override
        public char getChar() {
            return ch;
        }
    };

    /**
     * Do not call this constructor directly - instead access the
     * {@link FontAwesome.Glyph} public static enumeration method to create the glyph nodes), or
     * use the {@link GlyphFontRegistry} class to get access.
     *
     * Note: Do not remove this public constructor since it is used by the service loader!
     */
    public FontAwesome() {
        this("http://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/fonts/fontawesome-webfont.ttf"); //$NON-NLS-1$
    }

    /**
     * Creates a new FontAwesome instance which uses the provided font source.
     * @param url
     */
    public FontAwesome(String url){
        super(fontName, 14, url, true);
        registerAll(Arrays.asList(Glyph.values()));
    }

    /**
     * Creates a new FontAwesome instance which uses the provided font source.
     * @param is
     */
    public FontAwesome(InputStream is){
        super(fontName, 14, is, true);
        registerAll(Arrays.asList(Glyph.values()));
    }

}
