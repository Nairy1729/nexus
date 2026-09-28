package com.nexus.core.spatial.renderer

/**
 * GLSL Shaders for NEXUS SPATIAL GLASS OS.
 *
 * Implements:
 * - Liquid Glass Orbs: Translucent, refractive Fresnel rims, internal caustics, specular glints
 * - Spatial Atmosphere: Calm ambient dust motes and soft light particles synchronized with active theme
 * - User Core: Luminous breathing glass orb with theme-driven corona
 * - Light Filaments: Hairline orbital guides and constellation energy threads
 */
object GLShaders {

    // ---- LIQUID GLASS ORB SHADER ------------------------------------------
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

            // Fresnel refraction boundary
            float VdotN = max(dot(N, V), 0.0);
            float fresnel = 1.0 - VdotN;
            float rim = pow(fresnel, 2.6);

            // Specular environment reflection (top-down light catch)
            vec3 H = normalize(L + V);
            float NdotH = max(dot(N, H), 0.0);
            float specular = pow(NdotH, 28.0) * 0.70;

            // Internal caustic & fluid movement
            float internalFluid = sin(vModelPos.y * 5.0 + vModelPos.x * 3.0 + uTime * 1.4) * 0.04;
            vec3 coreTint = mix(vec3(0.88, 0.91, 0.98), uPrimaryColor.rgb, 0.35);
            vec3 internalGlow = coreTint * (pow(VdotN, 2.2) * 0.22 + internalFluid);

            // Glass edge highlight
            vec3 rimColor = mix(vec3(0.94, 0.96, 1.0), uAtmosphereColor.rgb, 0.40) * rim * (1.15 + uGlow * 0.25);
            vec3 specColor = vec3(1.0, 1.0, 1.0) * specular;

            vec3 orbColor = internalGlow + rimColor + specColor;

            // Translucency: center lets environment through, edges are reflective
            float alpha = clamp(0.32 + rim * 0.58 + specular * 0.4, 0.0, 0.95);

            // Eclipse state (missed call): shadowed orb with elegant warm red crescent rim
            if (uIsEclipse > 0.5) {
                orbColor = vec3(0.10, 0.12, 0.16) * (0.25 + 0.75 * VdotN);
                float crescent = pow(max(dot(N, vec3(0.707, 0.707, 0.0)), 0.0), 3.0) * fresnel;
                orbColor += vec3(0.92, 0.35, 0.30) * crescent * 1.3;
                alpha = 0.50;
            }

            // Selected state: subtle icy violet illumination aura
            if (uIsSelected > 0.5) {
                float pulse = 0.5 + 0.5 * sin(uTime * 3.2);
                orbColor += vec3(0.55, 0.68, 1.0) * (rim * 1.3 + pulse * 0.20);
                alpha = min(0.98, alpha + 0.15);
            }

            gl_FragColor = vec4(orbColor, alpha);
        }
    """

    // ---- USER CENTRAL CORE: LUMINOUS PEARL-WHITE / ICY GLASS ORB ---------
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
            // Calm pearl-white / icy breathing core with theme-driven color
            float pulse = 0.5 + 0.5 * sin(uTime * 1.6);
            float rim = pow(1.0 - abs(vNormal.z), 2.0);
            vec3 core = uCoreColor.rgb * (0.82 + 0.18 * pulse);
            vec3 rimGlow = mix(vec3(1.0), uCoreColor.rgb, 0.4) * rim * 1.1;
            gl_FragColor = vec4(core + rimGlow, 0.85);
        }
    """

    // ---- SPATIAL ATMOSPHERE: AMBIENT DUST MOTES / SOFT PARTICLES ----------
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

            // Gentle ambient floating
            float drift = 0.5 + 0.5 * sin(uTime * (aTwinkleSpeed * 0.4) + aTwinklePhase);
            float dist = max(pos.w, 1.0);
            gl_PointSize = (aBaseSize * (0.7 + 0.3 * drift)) * (16.0 / dist);
            gl_PointSize = clamp(gl_PointSize, 1.0, 5.5);

            // Very subtle opacity for atmospheric depth (never harsh stars)
            vAlpha = 0.08 + 0.18 * drift;
        }
    """

    const val STAR_FRAGMENT = """
        precision mediump float;
        uniform vec3 uMoteColor;
        varying float vAlpha;

        void main() {
            vec2 coord = gl_PointCoord - vec2(0.5);
            float distSq = dot(coord, coord);
            if (distSq > 0.25) discard;
            // Soft gaussian-like falloff
            float falloff = 1.0 - (distSq * 4.0);
            gl_FragColor = vec4(uMoteColor, vAlpha * falloff);
        }
    """

    // ---- LIGHT FILAMENTS: ORBITS & CONSTELLATIONS -------------------------
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
