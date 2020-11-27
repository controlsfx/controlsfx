/**
 * Copyright (c) 2013, 2018 ControlsFX
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
package org.controlsfx.control.tableview2.event;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import org.controlsfx.control.tableview2.TableView2;

/**
 * Event related to {@link TableView2} filtering.
 *
 * @param <C> The event source
 */
public class FilterEvent<C> extends Event {

    /**
     * Common supertype for all sort event types.
     */
    public static final EventType<FilterEvent> ANY =
            new EventType<FilterEvent>(Event.ANY, "FILTER");

    @SuppressWarnings("unchecked")
    public static <C> EventType<FilterEvent<C>> filterEvent() {
        return (EventType<FilterEvent<C>>) FILTER_EVENT;
    }

    private static final EventType<?> FILTER_EVENT = new EventType<>(FilterEvent.ANY, "FILTER_EVENT");

    /**
     * Construct a new {@code Event} with the specified event source and target, with
     * type {@code FILTER_EVENT}. If the source or target is set to {@code null},
     * it is replaced by the {@code NULL_SOURCE_TARGET} value.
     *
     * @param source the event source which sent the event
     * @param target the event target to associate with the event
     */
    public FilterEvent(@NamedArg("source") C source, @NamedArg("target") EventTarget target) {
        super(source, target, filterEvent());

    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public C getSource() {
        return (C) super.getSource();
    }
    
}
