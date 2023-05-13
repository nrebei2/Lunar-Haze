#ifdef GL_ES
precision lowp float;
#define MED mediump
#else
#define MED
#endif
varying vec4 v_color;
varying vec2 v_pos;

uniform float iTime;

float random2(vec2 p) {
    return fract(sin(dot(p, vec2(12.121213, 4.1231))) * 43256.12039);
}

float noise(vec2 p) {
    vec2 i_pos = floor(p);
    vec2 f_pos = fract(p);

    float col = 0.0;

    // value noise
    float ll = random2(i_pos);
    float lr = random2(i_pos + vec2(1.0, 0.0));
    float ul = random2(i_pos + vec2(0.0, 1.0));
    float ur = random2(i_pos + vec2(1.0, 1.0));

    // gradient noise
    //float ll = dot(random2_2(i_pos), f_pos);
    //float lr = dot(random2_2(i_pos + vec2(1.0, 0.0)), f_pos - vec2(1.0, 0.0));
    //float ul = dot(random2_2(i_pos + vec2(0.0, 1.0)), f_pos - vec2(0.0, 1.0));
    //float ur = dot(random2_2(i_pos + vec2(1.0, 1.0)), f_pos - vec2(1.0, 1.0));

    // cubic interpolation (smoothstep)
    vec2 u = f_pos*f_pos*(3.0-2.0*f_pos);

    // quintic interpolation
//    vec2 u = f_pos * f_pos * f_pos * (6.0 * f_pos * f_pos - 15. * f_pos + 10.);

    col = mix (
            mix( ll, lr, u.x),
            mix( ul, ur, u.x),
            u.y);

    return col;
}

float fbm (vec2 p ) {
    int octaves = 4;
    float lacunarity = 2.0;
    float gain = 0.5;

    float amp = 0.5;
    float freq = 1.;

    float h = 0.;

    for (int i = 0; i <= octaves; i++) {
        float b = noise(freq * p);
        h += amp * b;
        freq *= lacunarity;
        amp *= gain;
    }

    return h;
}

float pattern (in vec2 p) {
    // Domain warping
    vec2 q = vec2( fbm( p + vec2(0.0,0.0) ),
                   fbm( p + vec2(5.2,1.3) ) );

    vec2 r = vec2( fbm( p + 4.0*q + vec2(1.7,9.2) + iTime*3.5 ),
                   fbm( p + 4.0*q + vec2(8.3,2.8) + iTime*2.1 ) );

    float f = fbm( p + 4.0*r );
    return smoothstep(0., 1., f*f);
}

void main()
{
    float patternAlpha = pattern(v_pos * 23.0);
    gl_FragColor = vec4(v_color.xyz, v_color.a + 0.8 * patternAlpha);
}
