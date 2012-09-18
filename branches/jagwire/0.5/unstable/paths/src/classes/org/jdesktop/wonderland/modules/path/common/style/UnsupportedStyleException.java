package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;

/**
 * This is an Exception which is thrown when a style factory fails to create a style
 * or when a NodeStyle is attempted to be used with a PathStyle for which is is
 * not supported.
 *
 * @author Carl Jokl
 */
public class UnsupportedStyleException extends Exception implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private Object style;
    
    /**
     * Create an instance of UnsupportedStyleException.
     * 
     * @param style The style or style type which caused the exception.
     * @param message The message conveying the nature of the exception.
     */
    public UnsupportedStyleException(Object style, String message) {
        super(message);
        this.style = style;
    }

    /**
     * Create a new instance of UnsupportedStyleException.
     *
     * @param style The style or style type which caused the exception.
     * @param message The message explaining the nature of the exception.
     * @param cause An exception which was the underlying cause of this exception.
     */
    public UnsupportedStyleException(Object style, String message, Throwable cause) {
        super(message, cause);
        this.style = style;
    }

    /**
     * Get the style or style type which
     * was not supported causing this exception.
     *
     * @return The style or style type which was not supported.
     */
    public Object getStyle() {
        return style;
    }
}
