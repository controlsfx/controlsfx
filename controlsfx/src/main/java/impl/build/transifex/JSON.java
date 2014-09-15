/**
 * Copyright (c) 2014, ControlsFX
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
package impl.build.transifex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
class JSON {
    private static final Pattern PAT_INTEGER = Pattern.compile("[-+]?[0-9]+|0[Xx][0-9]+"); //$NON-NLS-1$
    private static final Pattern PAT_DOUBLE = Pattern.compile("[+-]?[0-9]+([Ee][+-]?[0-9]+)?|[+-]?[0-9]*\\.[0-9]*([Ee][+-]?[0-9]+)?"); //$NON-NLS-1$
    private static final Pattern PAT_STRING = Pattern.compile("\"([^\\\\]+\\\\[\"'\\\\])*[^\"]*\"|'([^\\\\]+\\\\[\"'\\\\])*[^']*'"); //$NON-NLS-1$
    private static final Pattern PAT_BOOL = Pattern.compile("(true)|(false)"); //$NON-NLS-1$

    private static Object parse(String s, int[] start, Matcher integerMatcher, Matcher doubleMatcher, Matcher stringMatcher, Matcher booleanMatcher) {
        char[] c = s.toCharArray();
        skipSpace(s, start);
        if (c[start[0]] == '[') {
            start[0]++;
            ArrayList<Object> a = new ArrayList<>();
            if (c[start[0]] == ']') {
                start[0]++;
                return a;
            }
            while (true) {
                a.add(parse(s, start, integerMatcher, doubleMatcher, stringMatcher, booleanMatcher));
                boolean crlf = skipSpace(s, start);
                char p = c[start[0]];
                if (p == ']') {
                    start[0]++;
                    return a;
                }
                if (p == ',')
                    start[0]++;
                else if (!crlf)
                    throw new IllegalStateException(", or ] expected"); //$NON-NLS-1$
            }
        } else if (c[start[0]] == '{') {
            start[0]++;
            HashMap<String, Object> a = new HashMap<>();
            while (true) {
                String field = (String) parse(s, start, integerMatcher, doubleMatcher, stringMatcher, booleanMatcher);
                boolean crlf = skipSpace(s, start);
                if (c[start[0]] == ':') {
                    start[0]++;
                    a.put(field, parse(s, start, integerMatcher, doubleMatcher, stringMatcher, booleanMatcher));
                    crlf = skipSpace(s, start);
                } else
                    a.put(field, ""); //$NON-NLS-1$
                char p = c[start[0]];
                if (p == '}') {
                    start[0]++;
                    return a;
                }
                if (p == ',')
                    start[0]++;
                else if (!crlf) {
                    start[0]++;
//                    throw new IllegalStateException(", or } expected at " + start[0]); //$NON-NLS-1$
                }
            }
        }
        if (integerMatcher.find(start[0])) {
            String substring = match(start, s, integerMatcher);
            if (substring != null) return Integer.valueOf(substring);
        }
        if (doubleMatcher.find(start[0])) {
            String substring = match(start, s, doubleMatcher);
            if (substring != null) return Double.valueOf(substring);
        }
        if (stringMatcher.find(start[0])) {
            String substring = match(start, s, stringMatcher);
            if (substring != null) return substring.substring(1, substring.length() - 1);
        }
        if (booleanMatcher.find(start[0])) {
            String substring = match(start, s, booleanMatcher);
            if (substring != null) return Boolean.valueOf(substring);
        }
//        throw new IllegalStateException("unexpected end of data"); //$NON-NLS-1$
        return null;
    }

    private static String match(int[] start, String s, Matcher matcher) {
        int ms = matcher.start();
        int me = matcher.end();
        if (start[0] == ms) {
            start[0] = me;
            return s.substring(ms, me);
        }
        return null;
    }

    public static boolean skipSpace(String s, int[] start) {
        boolean ret = false;
        while (true) {
            char c = s.charAt(start[0]);
            boolean crlf = (c == '\r') || (c == '\n');
            if ((c != ' ') && !crlf)
                break;
            if (crlf)
                ret = true;
            start[0]++;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
	public static <T> T parse(String json) {
        Matcher integerMatcher = PAT_INTEGER.matcher(json);
        Matcher doubleMatcher = PAT_DOUBLE.matcher(json);
        Matcher stringMatcher = PAT_STRING.matcher(json);
        Matcher booleanMatcher = PAT_BOOL.matcher(json);
        return (T) parse(json, new int[]{0}, integerMatcher, doubleMatcher, stringMatcher, booleanMatcher);
    }
}