/*
 * Copyright (c) 2018, ControlsFX
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
package impl.org.controlsfx.collections;

import java.util.Arrays;
import java.util.List;

class ChangeHelper {
    
    static String addRemoveChangeToString(int from, int to, List<?> list, List<?> removed) {
        
        StringBuilder b = new StringBuilder();

        if (removed.isEmpty()) {
            b.append(list.subList(from, to));
            b.append(" addition at ").append(from);
        } else {
            b.append(removed);
            if (from == to) {
                b.append(" removal at ").append(from);
            } else {
                b.append(" replaced by ");
                b.append(list.subList(from, to));
                b.append(" at ").append(from);
            }
        }
        return b.toString();
    }

    static String permChangeToString(int[] permutation) {
        return "permutation by " + Arrays.toString(permutation);
    }

    static String updateChangeToString(int from, int to) {
        return "update at range [" + from + ", " + to + ")";
    }
}

