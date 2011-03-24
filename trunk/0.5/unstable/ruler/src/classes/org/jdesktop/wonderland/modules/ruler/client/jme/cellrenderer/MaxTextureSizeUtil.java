package org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer;

import java.lang.reflect.Method;
import java.nio.IntBuffer;

/**
 * Utility class used to try and find the max texture size supported by the underlying platform
 * or return a default if it could not be found.
 *
 * @author Carl Jokl
 */
public abstract class MaxTextureSizeUtil {

    private static final int DEFAULT_MAX_SIZE = 512;

    private static final int GL_MAX_TEXTURE_SIZE = 3379;

    public static int getMaxTextureSize() {
        MaxTextureSizeRetriever retriever = null;
        try {
            retriever = new LWJGLTextureSizeRetriever();
            return retriever.getMaxTextureSize();
        } //I know this is hacky.
        catch (Exception lwjglException) {
            try {
                retriever = new JOGLTextureSizeRetriever();
                return retriever.getMaxTextureSize();
            }
            catch (Exception joglException) {}
        }
        return DEFAULT_MAX_SIZE;
    }

    /**
     * Private interface implemented by the private classes of the different APIs to
     * try and get the max texture size.
     */
    private interface MaxTextureSizeRetriever {

        /**
         * The implementing class will get the maximum texture size.
         *
         * @return The maximum texture size.
         * @throws Exception if any error occurred retrieving this value.
         */
        public int getMaxTextureSize() throws Exception;
    }

    private static class LWJGLTextureSizeRetriever implements MaxTextureSizeRetriever {

        @Override
        public int getMaxTextureSize() throws Exception {
            Class<?> lwjglGL11Class = Class.forName("org.lwjgl.opengl.GL11");
            int[] valueArray = new int[1];
            IntBuffer value = IntBuffer.wrap(valueArray);
            Method getIntegerMethod = lwjglGL11Class.getMethod("glGetInteger", int.class, IntBuffer.class);
            getIntegerMethod.invoke(null, GL_MAX_TEXTURE_SIZE, value);
            return valueArray[0];
        }
    }

    private static class JOGLTextureSizeRetriever implements MaxTextureSizeRetriever {

        @Override
        public int getMaxTextureSize() throws Exception {
            Class<?> joglGLClass = Class.forName("javax.media.opengl.GL");
            Class<?> joglGLContextClass = Class.forName("javax.media.opengl.GLContext");
            int[] valueArray = new int[1];
            IntBuffer value = IntBuffer.wrap(valueArray);
            Method getCurrentContextMethod = joglGLContextClass.getMethod("getCurrent");
            Object glContext = getCurrentContextMethod.invoke(null);
            if (glContext != null) {
                Method getGLMethod = joglGLContextClass.getMethod("getGL");
                Object gl = getGLMethod.invoke(glContext);
                if (gl != null) {
                    Method getIntegerMethod = joglGLClass.getMethod("glGetIntegerv", int.class, IntBuffer.class);
                    getIntegerMethod.invoke(gl, GL_MAX_TEXTURE_SIZE, value);
                    return valueArray[0];
                }
                else {
                    throw new UnsupportedOperationException("Failed to get access to GL api from the GL context!");
                }
            }
            else {
                throw new IllegalStateException("No GL Context is available!");
            }
        }

    }
}
