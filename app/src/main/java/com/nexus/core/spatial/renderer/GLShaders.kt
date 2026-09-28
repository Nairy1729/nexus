package com.nexus.core.spatial.renderer

/**
 * GLSL Shaders for NEXUS 3D Personal Communication Universe.
 */
object GLShaders {

    // ---- PLANET SHADER ----------------------------------------------------
    const val PLANET_VERTEX = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        attribute vec3 aPosition;
        attribute vec3 aNormal;
        
        varying vec3 vWorldPos;
        varying vec3 vNormal;
        varying vec3 vModelPos;

        void main() {
            vModelPos = aPosition;
            vec4 worldPos = uModelMatrix * vec4(aPosition, 1.0);
            vWorldPos = worldPos.xyz;
            // Approximate normal transformation assuming uniform scale
            vNormal = normalize((uModelMatrix * vec4(aNormal, 0.0)).xyz);
            gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
        }
    """

    const val PLANET_FRAGMENT = """
        precision mediump float;

        uniform vec3 uCameraPos;
        uniform vec3 uSunPos;
        uniform vec4 uPrimaryColor;
        uniform vec4 uAtmosphereColor;
        uniform float uGlow;
        uniform float uIsEclipse;
        uniform float uIsSelected;
        uniform float uTime;

        varying vec3 vWorldPos;
        varying vec3 vNormal;
        varying vec3 vModelPos;

        void main() {
            vec3 N = normalize(vNormal);
            vec3 V = normalize(uCameraPos - vWorldPos);
            vec3 L = normalize(uSunPos - vWorldPos);

            // Light from central star / user core
            float NdotL = max(dot(N, L), 0.18);
            
            // Fresnel rim lighting for authentic planetary atmosphere
            float fresnel = 1.0 - max(dot(N, V), 0.0);
            float rim = pow(fresnel, 2.5);

            // Subtle surface latitude banding for planetary identity
            float band = sin(vModelPos.y * 14.0 + vModelPos.x * 2.0) * 0.06;
            vec3 surfaceColor = uPrimaryColor.rgb * (1.0 + band);

            // Shading
            vec3 litColor = surfaceColor * NdotL;
            
            // Atmosphere rim
            vec3 atmoColor = uAtmosphereColor.rgb * rim * (1.2 + uGlow);

            // Eclipse (dimmed body with faint red-tinted corona)
            if (uIsEclipse > 0.5) {
                litColor *= 0.25;
                atmoColor = vec3(1.0, 0.35, 0.30) * rim * 1.5;
            }

            // Selection pulse
            if (uIsSelected > 0.5) {
                float pulse = 0.5 + 0.5 * sin(uTime * 4.0);
                atmoColor += vec3(0.8, 1.0, 0.3) * (rim * 1.8 + pulse * 0.3);
            }

            vec3 finalColor = litColor + atmoColor;
            gl_FragColor = vec4(finalColor, 1.0);
        }
    """

    // ---- USER CORE / LUMINOUS SUN SHADER ---------------------------------
    const val CORE_VERTEX = """
        uniform mat4 uMVPMatrix;
        attribute vec3 aPosition;
        attribute vec3 aNormal;
        varying vec3 vNormal;

        void main() {
            vNormal = aNormal;
            gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
        }
    """

    const val CORE_FRAGMENT = """
        precision mediump float;
        uniform vec4 uCoreColor;
        uniform float uTime;
        varying vec3 vNormal;

        void main() {
            float pulse = 0.5 + 0.5 * sin(uTime * 2.5);
            // Core rim
            float rim = pow(1.0 - abs(vNormal.z), 1.6);
            vec3 color = uCoreColor.rgb + vec3(0.2, 0.2, 0.1) * pulse + vec3(0.3) * rim;
            gl_FragColor = vec4(color, 1.0);
        }
    """

    // ---- STARFIELD SHADER -------------------------------------------------
    const val STAR_VERTEX = """
        uniform mat4 uMVPMatrix;
        uniform float uTime;
        attribute vec3 aPosition;
        attribute float aBaseSize;
        attribute float aTwinklePhase;
        attribute float aTwinkleSpeed;
        varying float vAlpha;

        void main() {
            vec4 pos = uMVPMatrix * vec4(aPosition, 1.0);
            gl_Position = pos;

            float twinkle = 0.5 + 0.5 * sin(uTime * aTwinkleSpeed + aTwinklePhase);
            // Depth attenuation
            float dist = max(pos.w, 1.0);
            gl_PointSize = (aBaseSize * (0.8 + 0.4 * twinkle)) * (20.0 / dist);
            gl_PointSize = clamp(gl_PointSize, 1.5, 9.0);

            vAlpha = 0.4 + 0.6 * twinkle;
        }
    """

    const val STAR_FRAGMENT = """
        precision mediump float;
        varying float vAlpha;

        void main() {
            // Soft circular star point sprite
            vec2 coord = gl_PointCoord - vec2(0.5);
            float distSq = dot(coord, coord);
            if (distSq > 0.25) discard;
            float falloff = 1.0 - (distSq * 4.0);
            gl_FragColor = vec4(1.0, 1.0, 1.0, vAlpha * falloff);
        }
    """

    // ---- LINE / ORBIT / CONSTELLATION SHADER ------------------------------
    const val LINE_VERTEX = """
        uniform mat4 uMVPMatrix;
        attribute vec3 aPosition;

        void main() {
            gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
        }
    """

    const val LINE_FRAGMENT = """
        precision mediump float;
        uniform vec4 uColor;

        void main() {
            gl_FragColor = uColor;
        }
    """
}
