/**
 * Copyright (c) 2013, 2015, ControlsFX
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
package org.controlsfx.control.action;

import javafx.event.ActionEvent;
import org.controlsfx.glyphfont.Glyph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to allow conversion of class methods to {@link Action} instances.
 * 
 * <p>The following steps are required to use {@link ActionProxy} annotations:
 * 
 * <ol>
 * <li>Annotate your methods with the {@link ActionProxy} annotation. For example:
 * <pre>{@code @ActionProxy(text="Action 1.1", graphic=imagePath, accelerator="ctrl+shift+T")
 * private void action11() {
 *     System.out.println("Action 1.1 is executed");
 * }}</pre>
 * 
 * <p>The ActionProxy annotation is designed to work with three types of methods: 
 * <ol>
 *     <li>Methods with no parameters, 
 *     <li>Methods with one parameter of type {@link ActionEvent}.
 *     <li>Methods that take both an {@link ActionEvent} and an {@link Action}.
 * </ol> 
 * 
 * <p>The ActionProxy annotation {@link #graphic()} property supports different node types:
 * <ol>
 *     <li>Images,
 *     <li>Glyph fonts.
 * </ol>
 * 
 * <p>The ability for ActionProxy to support glyph fonts is part of the ControlsFX
 * {@link Glyph} API. For more information on how to specify
 * images and glyph fonts, refer to the {@link ActionProxy#graphic()} method.
 * <br><br></li>
 * 
 * <li>Register your class in the global {@link ActionMap}, preferably in the 
 * class constructor:  
 * <pre>{@code ActionMap.register(this); }</pre> 
 * 
 * Immediately after that actions will be created according to the provided 
 * annotations and are accessible from {@link ActionMap}, which provides several 
 * convenience methods to access actions by id. Refer to the {@link ActionMap}
 * class for more details on how to use it.</li>
 *
 * <p>{@link ActionCheck} annotation is supported on the same method where @ActionProxy is applied}</p>
 * </ol>
 * 
 * @see Action
 * @see ActionMap
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionProxy {
    
    /**
     * By default the method name that this annotation is applied to, but if not
     * null then this ID is what you use when requesting the {@link Action} out
     * of the {@link ActionMap} when using the {@link ActionMap#action(String)}
     * method.
     */
	String id() default "";
	
	/**
	 * The text that should be set in {@link Action#textProperty()}.
	 */
    String text();
    
    /**
     * The graphic that should be set in {@link Action#graphicProperty()}.
     * 
     * <p>The graphic can be either image (local path or url) or font glyph. 
     * 
     * <p>Because a graphic can come from multiple sources, a simple protocol 
     * prefix is used to designate the type. Currently supported prefixes are 
     * '<code>image&gt;</code>' and '<code>font&gt;</code>'. Default protocol is 
     * '<code>image&gt;</code>'. 
     * 
     * <p>The following are the examples of different graphic nodes:
	 * <pre>
	 * &#64;ActionProxy(text="Teacher", graphic="http://icons.iconarchive.com/icons/custom-icon-design/mini-3/16/teacher-male-icon.png")
	 * &#64;ActionProxy(text="Security", graphic="/org/controlsfx/samples/security-low.png")
	 * &#64;ActionProxy(text="Security", graphic="image&gt;/org/controlsfx/samples/security-low.png")
	 * &#64;ActionProxy(text="Star", graphic="font&gt;FontAwesome|STAR")
	 * </pre>     
	 * 
	 */
    String graphic() default "";
    
    /**
     * The text that should be set in {@link Action#longTextProperty()}.
     */
    String longText() default "";
    
    /**
     * Accepts string values such as "ctrl+shift+T" to represent the keyboard
     * shortcut for this action. By default this is empty if there is no keyboard
     * shortcut desired for this action.
     */
    String accelerator() default "";
    
    /**
     * The full class-name of a class that implements {@link AnnotatedActionFactory}. {@link ActionMap} will
     * use this class to instantiate the {@link AnnotatedAction} associated with this method, rather than
     * using its own action factory.
     */
    String factory() default "";
}
