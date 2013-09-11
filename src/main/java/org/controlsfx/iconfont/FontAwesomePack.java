/**
 * Copyright (c) 2013, ControlsFX
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
package org.controlsfx.iconfont;

import java.util.HashMap;
import java.util.Map;

public class FontAwesomePack implements FontIconPack {

	private String fontName = "FontAwesome";
	private IconFont fontAwesome = new IconFont( fontName,
	 	 "http://fontawesome.io/assets/font-awesome/font/fontawesome-webfont.ttf");
	
	private Map<String, Character> glyphs = new HashMap<>();
	
	public FontAwesomePack() {
		glyphs.put("GLASS",'\uf000');
		glyphs.put("MUSIC",'\uf001');
		glyphs.put("SEARCH",'\uf002');
		glyphs.put("ENVELOPE_ALT",'\uf003');
		glyphs.put("HEART",'\uf004');
		glyphs.put("STAR",'\uf005');
		glyphs.put("STAR_EMPTY",'\uf006');
		glyphs.put("USER",'\uf007');
		glyphs.put("FILM",'\uf008');
		glyphs.put("TH_LARGE",'\uf009');
		glyphs.put("TH",'\uf00A');
		glyphs.put("TH_LIST",'\uf00B');
		glyphs.put("OK",'\uf00C');
		glyphs.put("REMOVE",'\uf00D');
		glyphs.put("ZOOM_IN",'\uf00E');
		glyphs.put("ZOOM_OUT",'\uf010');
		glyphs.put("POWER_OFF",'\uf011');
		glyphs.put("OFF",'\uf011');
		glyphs.put("SIGNAL",'\uf012');
		glyphs.put("GEAR",'\uf013');
		glyphs.put("COG",'\uf013');
		glyphs.put("TRASH",'\uf014');
		glyphs.put("HOME",'\uf015');
		glyphs.put("FILE_ALT",'\uf016');
		glyphs.put("TIME",'\uf017');
		glyphs.put("ROAD",'\uf018');
		glyphs.put("DOWNLOAD_ALT",'\uf019');
		glyphs.put("DOWNLOAD",'\uf01A');
		glyphs.put("UPLOAD",'\uf01B');
		glyphs.put("INBOX",'\uf01C');
		glyphs.put("PLAY_CIRCLE",'\uf01D');
		glyphs.put("ROTATE_RIGHT",'\uf01E');
		glyphs.put("REPEAT",'\uf01E');
		glyphs.put("REFRESH",'\uf021');
		glyphs.put("LIST_ALT",'\uf022');
		glyphs.put("LOCK",'\uf023');
		glyphs.put("FLAG",'\uf024');
		glyphs.put("HEADPHONES",'\uf025');
		glyphs.put("VOLUME_OFF",'\uf026');
		glyphs.put("VOLUME_DOWN",'\uf027');
		glyphs.put("VOLUME_UP",'\uf028');
		glyphs.put("QRCODE",'\uf029');
		glyphs.put("BARCODE",'\uf02A');
		glyphs.put("TAG",'\uf02B');
		glyphs.put("TAGS",'\uf02C');
		glyphs.put("BOOK",'\uf02D');
		glyphs.put("BOOKMARK",'\uf02E');
		glyphs.put("PRINT",'\uf02F');
		glyphs.put("CAMERA",'\uf030');
		glyphs.put("FONT",'\uf031');
		glyphs.put("BOLD",'\uf032');
		glyphs.put("ITALIC",'\uf033');
		glyphs.put("TEXT_HEIGHT",'\uf034');
		glyphs.put("TEXT_WIDTH",'\uf035');
		glyphs.put("ALIGN_LEFT",'\uf036');
		glyphs.put("ALIGN_CENTER",'\uf037');
		glyphs.put("ALIGN_RIGHT",'\uf038');
		glyphs.put("ALIGN_JUSTIFY",'\uf039');
		glyphs.put("LIST",'\uf03A');
		glyphs.put("INDENT_LEFT",'\uf03B');
		glyphs.put("INDENT_RIGHT",'\uf03C');
		glyphs.put("FACETIME_VIDEO",'\uf03D');
		glyphs.put("PICTURE",'\uf03E');
		glyphs.put("PENCIL",'\uf040');
		glyphs.put("MAP_MARKER",'\uf041');
		glyphs.put("ADJUST",'\uf042');
		glyphs.put("TINT",'\uf043');
		glyphs.put("EDIT",'\uf044');
		glyphs.put("SHARE",'\uf045');
		glyphs.put("CHECK",'\uf046');
		glyphs.put("MOVE",'\uf047');
		glyphs.put("STEP_BACKWARD",'\uf048');
		glyphs.put("FAST_BACKWARD",'\uf049');
		glyphs.put("BACKWARD",'\uf04A');
		glyphs.put("PLAY",'\uf04B');
		glyphs.put("PAUSE",'\uf04C');
		glyphs.put("STOP",'\uf04D');
		glyphs.put("FORWARD",'\uf04E');
		glyphs.put("FAST_FORWARD",'\uf050');
		glyphs.put("STEP_FORWARD",'\uf051');
		glyphs.put("EJECT",'\uf052');
		glyphs.put("CHEVRON_LEFT",'\uf053');
		glyphs.put("CHEVRON_RIGHT",'\uf054');
		glyphs.put("PLUS_SIGN",'\uf055');
		glyphs.put("MINUS_SIGN",'\uf056');
		glyphs.put("REMOVE_SIGN",'\uf057');
		glyphs.put("OK_SIGN",'\uf058');
		glyphs.put("QUESTION_SIGN",'\uf059');
		glyphs.put("INFO_SIGN",'\uf05A');
		glyphs.put("SCREENSHOT",'\uf05B');
		glyphs.put("REMOVE_CIRCLE",'\uf05C');
		glyphs.put("OK_CIRCLE",'\uf05D');
		glyphs.put("BAN_CIRCLE",'\uf05E');
		glyphs.put("ARROW_LEFT",'\uf060');
		glyphs.put("ARROW_RIGHT",'\uf061');
		glyphs.put("ARROW_UP",'\uf062');
		glyphs.put("ARROW_DOWN",'\uf063');
		glyphs.put("MAIL_FORWARD",'\uf064');
		glyphs.put("SHARE_ALT",'\uf064');
		glyphs.put("RESIZE_FULL",'\uf065');
		glyphs.put("RESIZE_SMALL",'\uf066');
		glyphs.put("PLUS",'\uf067');
		glyphs.put("MINUS",'\uf068');
		glyphs.put("ASTERISK",'\uf069');
		glyphs.put("EXCLAMATION_SIGN",'\uf06A');
		glyphs.put("GIFT",'\uf06B');
		glyphs.put("LEAF",'\uf06C');
		glyphs.put("FIRE",'\uf06D');
		glyphs.put("EYE_OPEN",'\uf06E');
		glyphs.put("EYE_CLOSE",'\uf070');
		glyphs.put("WARNING_SIGN",'\uf071');
		glyphs.put("PLANE",'\uf072');
		glyphs.put("CALENDAR",'\uf073');
		glyphs.put("RANDOM",'\uf074');
		glyphs.put("COMMENT",'\uf075');
		glyphs.put("MAGNET",'\uf076');
		glyphs.put("CHEVRON_UP",'\uf077');
		glyphs.put("CHEVRON_DOWN",'\uf078');
		glyphs.put("RETWEET",'\uf079');
		glyphs.put("SHOPPING_CART",'\uf07A');
		glyphs.put("FOLDER_CLOSE",'\uf07B');
		glyphs.put("FOLDER_OPEN",'\uf07C');
		glyphs.put("RESIZE_VERTICAL",'\uf07D');
		glyphs.put("RESIZE_HORIZONTAL",'\uf07E');
		glyphs.put("BAR_CHART",'\uf080');
		glyphs.put("TWITTER_SIGN",'\uf081');
		glyphs.put("FACEBOOK_SIGN",'\uf082');
		glyphs.put("CAMERA_RETRO",'\uf083');
		glyphs.put("KEY",'\uf084');
		glyphs.put("GEARS",'\uf085');
		glyphs.put("COGS",'\uf085');
		glyphs.put("COMMENTS",'\uf086');
		glyphs.put("THUMBS_UP_ALT",'\uf087');
		glyphs.put("THUMBS_DOWN_ALT",'\uf088');
		glyphs.put("STAR_HALF",'\uf089');
		glyphs.put("HEART_EMPTY",'\uf08A');
		glyphs.put("SIGNOUT",'\uf08B');
		glyphs.put("LINKEDIN_SIGN",'\uf08C');
		glyphs.put("PUSHPIN",'\uf08D');
		glyphs.put("EXTERNAL_LINK",'\uf08E');
		glyphs.put("SIGNIN",'\uf090');
		glyphs.put("TROPHY",'\uf091');
		glyphs.put("GITHUB_SIGN",'\uf092');
		glyphs.put("UPLOAD_ALT",'\uf093');
		glyphs.put("LEMON",'\uf094');
		glyphs.put("PHONE",'\uf095');
		glyphs.put("UNCHECKED",'\uf096');
		glyphs.put("CHECK_EMPTY",'\uf096');
		glyphs.put("BOOKMARK_EMPTY",'\uf097');
		glyphs.put("PHONE_SIGN",'\uf098');
		glyphs.put("TWITTER",'\uf099');
		glyphs.put("FACEBOOK",'\uf09A');
		glyphs.put("GITHUB",'\uf09B');
		glyphs.put("UNLOCK",'\uf09C');
		glyphs.put("CREDIT_CARD",'\uf09D');
		glyphs.put("RSS",'\uf09E');
		glyphs.put("HDD",'\uf0A0');
		glyphs.put("BULLHORN",'\uf0A1');
		glyphs.put("BELL",'\uf0A2');
		glyphs.put("CERTIFICATE",'\uf0A3');
		glyphs.put("HAND_RIGHT",'\uf0A4');
		glyphs.put("HAND_LEFT",'\uf0A5');
		glyphs.put("HAND_UP",'\uf0A6');
		glyphs.put("HAND_DOWN",'\uf0A7');
		glyphs.put("CIRCLE_ARROW_LEFT",'\uf0A8');
		glyphs.put("CIRCLE_ARROW_RIGHT",'\uf0A9');
		glyphs.put("CIRCLE_ARROW_UP",'\uf0AA');
		glyphs.put("CIRCLE_ARROW_DOWN",'\uf0AB');
		glyphs.put("GLOBE",'\uf0AC');
		glyphs.put("WRENCH",'\uf0AD');
		glyphs.put("TASKS",'\uf0AE');
		glyphs.put("FILTER",'\uf0B0');
		glyphs.put("BRIEFCASE",'\uf0B1');
		glyphs.put("FULLSCREEN",'\uf0B2');
		glyphs.put("GROUP",'\uf0C0');
		glyphs.put("LINK",'\uf0C1');
		glyphs.put("CLOUD",'\uf0C2');
		glyphs.put("BEAKER",'\uf0C3');
		glyphs.put("CUT",'\uf0C4');
		glyphs.put("COPY",'\uf0C5');
		glyphs.put("PAPERCLIP",'\uf0C6');
		glyphs.put("PAPER_CLIP",'\uf0C6');
		glyphs.put("SAVE",'\uf0C7');
		glyphs.put("SIGN_BLANK",'\uf0C8');
		glyphs.put("REORDER",'\uf0C9');
		glyphs.put("LIST_UL",'\uf0CA');
		glyphs.put("LIST_OL",'\uf0CB');
		glyphs.put("STRIKETHROUGH",'\uf0CC');
		glyphs.put("UNDERLINE",'\uf0CD');
		glyphs.put("TABLE",'\uf0CE');
		glyphs.put("MAGIC",'\uf0D0');
		glyphs.put("TRUCK",'\uf0D1');
		glyphs.put("PINTEREST",'\uf0D2');
		glyphs.put("PINTEREST_SIGN",'\uf0D3');
		glyphs.put("GOOGLE_PLUS_SIGN",'\uf0D4');
		glyphs.put("GOOGLE_PLUS",'\uf0D5');
		glyphs.put("MONEY",'\uf0D6');
		glyphs.put("CARET_DOWN",'\uf0D7');
		glyphs.put("CARET_UP",'\uf0D8');
		glyphs.put("CARET_LEFT",'\uf0D9');
		glyphs.put("CARET_RIGHT",'\uf0DA');
		glyphs.put("COLUMNS",'\uf0DB');
		glyphs.put("SORT",'\uf0DC');
		glyphs.put("SORT_DOWN",'\uf0DD');
		glyphs.put("SORT_UP",'\uf0DE');
		glyphs.put("ENVELOPE",'\uf0E0');
		glyphs.put("LINKEDIN",'\uf0E1');
		glyphs.put("ROTATE_LEFT",'\uf0E2');
		glyphs.put("UNDO",'\uf0E2');
		glyphs.put("LEGAL",'\uf0E3');
		glyphs.put("DASHBOARD",'\uf0E4');
		glyphs.put("COMMENT_ALT",'\uf0E5');
		glyphs.put("COMMENTS_ALT",'\uf0E6');
		glyphs.put("BOLT",'\uf0E7');
		glyphs.put("SITEMAP",'\uf0E8');
		glyphs.put("UMBRELLA",'\uf0E9');
		glyphs.put("PASTE",'\uf0EA');
		glyphs.put("LIGHTBULB",'\uf0EB');
		glyphs.put("EXCHANGE",'\uf0EC');
		glyphs.put("CLOUD_DOWNLOAD",'\uf0ED');
		glyphs.put("CLOUD_UPLOAD",'\uf0EE');
		glyphs.put("USER_MD",'\uf0F0');
		glyphs.put("STETHOSCOPE",'\uf0F1');
		glyphs.put("SUITCASE",'\uf0F2');
		glyphs.put("BELL_ALT",'\uf0F3');
		glyphs.put("COFFEE",'\uf0F4');
		glyphs.put("FOOD",'\uf0F5');
		glyphs.put("FILE_TEXT_ALT",'\uf0F6');
		glyphs.put("BUILDING",'\uf0F7');
		glyphs.put("HOSPITAL",'\uf0F8');
		glyphs.put("AMBULANCE",'\uf0F9');
		glyphs.put("MEDKIT",'\uf0FA');
		glyphs.put("FIGHTER_JET",'\uf0FB');
		glyphs.put("BEER",'\uf0FC');
		glyphs.put("H_SIGN",'\uf0FD');
		glyphs.put("PLUS_SIGN_ALT",'\uf0FE');
		glyphs.put("DOUBLE_ANGLE_LEFT",'\uf100');
		glyphs.put("DOUBLE_ANGLE_RIGHT",'\uf101');
		glyphs.put("DOUBLE_ANGLE_UP",'\uf102');
		glyphs.put("DOUBLE_ANGLE_DOWN",'\uf103');
		glyphs.put("ANGLE_LEFT",'\uf104');
		glyphs.put("ANGLE_RIGHT",'\uf105');
		glyphs.put("ANGLE_UP",'\uf106');
		glyphs.put("ANGLE_DOWN",'\uf107');
		glyphs.put("DESKTOP",'\uf108');
		glyphs.put("LAPTOP",'\uf109');
		glyphs.put("TABLET",'\uf10A');
		glyphs.put("MOBILE_PHONE",'\uf10B');
		glyphs.put("CIRCLE_BLANK",'\uf10C');
		glyphs.put("QUOTE_LEFT",'\uf10D');
		glyphs.put("QUOTE_RIGHT",'\uf10E');
		glyphs.put("SPINNER",'\uf110');
		glyphs.put("CIRCLE",'\uf111');
		glyphs.put("MAIL_REPLY",'\uf112');
		glyphs.put("REPLY",'\uf112');
		glyphs.put("GITHUB_ALT",'\uf113');
		glyphs.put("FOLDER_CLOSE_ALT",'\uf114');
		glyphs.put("FOLDER_OPEN_ALT",'\uf115');
		glyphs.put("EXPAND_ALT",'\uf116');
		glyphs.put("COLLAPSE_ALT",'\uf117');
		glyphs.put("SMILE",'\uf118');
		glyphs.put("FROWN",'\uf119');
		glyphs.put("MEH",'\uf11A');
		glyphs.put("GAMEPAD",'\uf11B');
		glyphs.put("KEYBOARD",'\uf11C');
		glyphs.put("FLAG_ALT",'\uf11D');
		glyphs.put("FLAG_CHECKERED",'\uf11E');
		glyphs.put("TERMINAL",'\uf120');
		glyphs.put("CODE",'\uf121');
		glyphs.put("REPLY_ALL",'\uf122');
		glyphs.put("MAIL_REPLY_ALL",'\uf122');
		glyphs.put("STAR_HALF_FULL",'\uf123');
		glyphs.put("STAR_HALF_EMPTY",'\uf123');
		glyphs.put("LOCATION_ARROW",'\uf124');
		glyphs.put("CROP",'\uf125');
		glyphs.put("CODE_FORK",'\uf126');
		glyphs.put("UNLINK",'\uf127');
		glyphs.put("QUESTION",'\uf128');
		glyphs.put("INFO",'\uf129');
		glyphs.put("EXCLAMATION",'\uf12A');
		glyphs.put("SUPERSCRIPT",'\uf12B');
		glyphs.put("SUBSCRIPT",'\uf12C');
		glyphs.put("ERASER",'\uf12D');
		glyphs.put("PUZZLE_PIECE",'\uf12E');
		glyphs.put("MICROPHONE",'\uf130');
		glyphs.put("MICROPHONE_OFF",'\uf131');
		glyphs.put("SHIELD",'\uf132');
		glyphs.put("CALENDAR_EMPTY",'\uf133');
		glyphs.put("FIRE_EXTINGUISHER",'\uf134');
		glyphs.put("ROCKET",'\uf135');
		glyphs.put("MAXCDN",'\uf136');
		glyphs.put("CHEVRON_SIGN_LEFT",'\uf137');
		glyphs.put("CHEVRON_SIGN_RIGHT",'\uf138');
		glyphs.put("CHEVRON_SIGN_UP",'\uf139');
		glyphs.put("CHEVRON_SIGN_DOWN",'\uf13A');
		glyphs.put("HTML5",'\uf13B');
		glyphs.put("CSS3",'\uf13C');
		glyphs.put("ANCHOR",'\uf13D');
		glyphs.put("UNLOCK_ALT",'\uf13E');
		glyphs.put("BULLSEYE",'\uf140');
		glyphs.put("ELLIPSIS_HORIZONTAL",'\uf141');
		glyphs.put("ELLIPSIS_VERTICAL",'\uf142');
		glyphs.put("RSS_SIGN",'\uf143');
		glyphs.put("PLAY_SIGN",'\uf144');
		glyphs.put("TICKET",'\uf145');
		glyphs.put("MINUS_SIGN_ALT",'\uf146');
		glyphs.put("CHECK_MINUS",'\uf147');
		glyphs.put("LEVEL_UP",'\uf148');
		glyphs.put("LEVEL_DOWN",'\uf149');
		glyphs.put("CHECK_SIGN",'\uf14A');
		glyphs.put("EDIT_SIGN",'\uf14B');
		glyphs.put("EXTERNAL_LINK_SIGN",'\uf14C');
		glyphs.put("SHARE_SIGN",'\uf14D');
		glyphs.put("COMPASS",'\uf14E');
		glyphs.put("COLLAPSE",'\uf150');
		glyphs.put("COLLAPSE_TOP",'\uf151');
		glyphs.put("EXPAND",'\uf152');
		glyphs.put("EURO",'\uf153');
		glyphs.put("EUR",'\uf153');
		glyphs.put("GBP",'\uf154');
		glyphs.put("DOLLAR",'\uf155');
		glyphs.put("USD",'\uf155');
		glyphs.put("RUPEE",'\uf156');
		glyphs.put("INR",'\uf156');
		glyphs.put("YEN",'\uf157');
		glyphs.put("JPY",'\uf157');
		glyphs.put("RENMINBI",'\uf158');
		glyphs.put("CNY",'\uf158');
		glyphs.put("WON",'\uf159');
		glyphs.put("KRW",'\uf159');
		glyphs.put("BITCOIN",'\uf15A');
		glyphs.put("BTC",'\uf15A');
		glyphs.put("FILE",'\uf15B');
		glyphs.put("FILE_TEXT",'\uf15C');
		glyphs.put("SORT_BY_ALPHABET",'\uf15D');
		glyphs.put("SORT_BY_ALPHABET_ALT",'\uf15E');
		glyphs.put("SORT_BY_ATTRIBUTES",'\uf160');
		glyphs.put("SORT_BY_ATTRIBUTES_ALT",'\uf161');
		glyphs.put("SORT_BY_ORDER",'\uf162');
		glyphs.put("SORT_BY_ORDER_ALT",'\uf163');
		glyphs.put("THUMBS_UP",'\uf164');
		glyphs.put("THUMBS_DOWN",'\uf165');
		glyphs.put("YOUTUBE_SIGN",'\uf166');
		glyphs.put("YOUTUBE",'\uf167');
		glyphs.put("XING",'\uf168');
		glyphs.put("XING_SIGN",'\uf169');
		glyphs.put("YOUTUBE_PLAY",'\uf16A');
		glyphs.put("DROPBOX",'\uf16B');
		glyphs.put("STACKEXCHANGE",'\uf16C');
		glyphs.put("INSTAGRAM",'\uf16D');
		glyphs.put("FLICKR",'\uf16E');
		glyphs.put("ADN",'\uf170');
		glyphs.put("BITBUCKET",'\uf171');
		glyphs.put("BITBUCKET_SIGN",'\uf172');
		glyphs.put("TUMBLR",'\uf173');
		glyphs.put("TUMBLR_SIGN",'\uf174');
		glyphs.put("LONG_ARROW_DOWN",'\uf175');
		glyphs.put("LONG_ARROW_UP",'\uf176');
		glyphs.put("LONG_ARROW_LEFT",'\uf177');
		glyphs.put("LONG_ARROW_RIGHT",'\uf178');
		glyphs.put("APPLE",'\uf179');
		glyphs.put("WINDOWS",'\uf17A');
		glyphs.put("ANDROID",'\uf17B');
		glyphs.put("LINUX",'\uf17C');
		glyphs.put("DRIBBBLE",'\uf17D');
		glyphs.put("SKYPE",'\uf17E');
		glyphs.put("FOURSQUARE",'\uf180');
		glyphs.put("TRELLO",'\uf181');
		glyphs.put("FEMALE",'\uf182');
		glyphs.put("MALE",'\uf183');
		glyphs.put("GITTIP",'\uf184');
		glyphs.put("SUN",'\uf185');
		glyphs.put("MOON",'\uf186');
		glyphs.put("ARCHIVE",'\uf187');
		glyphs.put("BUG",'\uf188');
		glyphs.put("VK",'\uf189');
		glyphs.put("WEIBO",'\uf18A');
		glyphs.put("RENREN",'\uf18B');
		
		
	}
	
	@Override
	public String getFontName() {
		return fontName;
	}

	@Override
	public IconFont getFont() {
		return fontAwesome;
	}

	@Override
	public Map<String, Character> getGlyphs() {
		return glyphs;
	}

}
