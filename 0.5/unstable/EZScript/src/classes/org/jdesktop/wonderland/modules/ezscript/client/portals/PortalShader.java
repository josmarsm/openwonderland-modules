/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.portals;

import com.jme.scene.Geometry;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.shader.Shader;

/**
 *
 * @author JagWire
 */
public class PortalShader extends Shader {

        /**
         * The vertex and fragment shader
         */
        private static final String vShader =
                //        "attribute vec3 tangent;" +
                //        "attribute vec3 binormal;" +
                "varying vec3 EyeDir;" +
                "varying vec3 LightDir;" +
                "void main(void)" +
                "{" +
                //"        vec3 n = normalize(gl_NormalMatrix * gl_Normal);" +
                //"        vec3 t = normalize(gl_NormalMatrix * gl_SecondaryColor.xyz);" +
                "        vec3 n = normalize(gl_Normal);" +
                "        vec3 t = normalize(gl_SecondaryColor.xyz);" +
                "        vec3 b = normalize(cross(n, t));" +
                "        gl_Position = ftransform();" +
                "        vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);" +
                "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
                "" +
                "        vec3 v;" +
                "        vec4 lloc = vec4(0.0, 75.0, 0.0, 1.0);" +
                "        vec3 mlloc = vec3(lloc); " +
                //"        vec3 tmpVec = normalize(mlloc - vVertex);" +
                "        vec3 tmpVec = normalize(mlloc - gl_Vertex.xyz);" +
                //"        vec3 tmpVec = normalize(lloc);" +
                //"        mat3 tMat = mat3(t, b, n);" +
                "        v.x = dot(tmpVec, t);" +
                "        v.y = dot(tmpVec, b);" +
                "        v.z = dot(tmpVec, n);" +
                //"        LightDir = normalize(tMat*lloc);" +
                "        LightDir = normalize(v);" +
                "" +
                //"        tmpVec = vVertex;" +
                //"        v.x = dot(tmpVec, t);" +
                //"        v.y = dot(tmpVec, b);" +
                //"        v.z = dot(tmpVec, n);" +
                //"        EyeDir = normalize(v);" +
                "}";
        private static final String fShader =
                "varying vec3 EyeDir;" +
                "varying vec3 LightDir;" +
                "uniform sampler2D DiffuseMapIndex;" +
                "uniform sampler2D NormalMapIndex;" +
                "vec3 FragLocalNormal;" +
                "vec3 finalColor;" +
                "vec3 diffuseColor;" +
                "vec3 specularColor;" +
                "float NdotL;" +
                "float spec;" +
                "vec3 reflectDir;" +
                "void main(void) { " +
                // Do some setup
                "        diffuseColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st).rgb;" +
                "        FragLocalNormal = normalize(texture2D(NormalMapIndex, gl_TexCoord[0].st).xyz * 2.0 - 1.0);" +
                //"        FragLocalNormal.y = -FragLocalNormal.y;" +
                //"        specularColor = vec3(1.0, 1.0, 1.0);" +
                //"        finalColor = gl_FrontMaterial.ambient.rgb * gl_LightSource[0].ambient.rgb;" +
                // Compute diffuse for light0
                //"        vec3 mlloc = gl_NormalMatrix * lloc; " +
                "        vec3 ld = vec3(0, 0, 1);" +
                "        NdotL = dot(FragLocalNormal, normalize(LightDir));" +
                //"        NdotL = dot(FragLocalNormal, ld);"  +
                "        finalColor = (diffuseColor*NdotL);" +
                //"        finalColor = vec3(NdotL);" +
                // Compte specular for light0
                //"        reflectDir = reflect(LightDir, FragLocalNormal);" +
                //"        spec = max(dot(EyeDir, reflectDir), 0.0);" +
                //"        spec = pow(spec, 32.0);" +
                //"        finalColor += (spec * specularColor * gl_LightSource[0].specular.rgb);" +
                // Final assignment
                "        gl_FragColor = vec4(finalColor, 1.0);" +
                "}";

        public PortalShader(WorldManager worldManager) {
            super(vShader, fShader);
            init(worldManager);
        }

        /**
         * This applies this shader to the given geometry
         */
        public void applyToGeometry(Geometry geo) {
//        shaderState.setAttributePointer("binormal", 3, false, 0, geo.getBinormalBuffer());
//        shaderState.setAttributePointer("tangent", 3, false, 0, geo.getTangentBuffer());
            shaderState.setUniform("DiffuseMapIndex", 0);
            shaderState.setUniform("NormalMapIndex", 1);
            geo.setRenderState(shaderState);
        }
    }
