#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main()
{
//    float step_w = 0.001;
//    float step_h = 0.001;
//
//    float kernel[9];
//    kernel[0] = 1.0/16.0;
//    kernel[1] = 2.0/16.0;
//    kernel[2] = 1.0/16.0;
//    kernel[3] = 2.0/16.0;
//    kernel[4] = 4.0/16.0;
//    kernel[5] = 2.0/16.0;
//    kernel[6] = 1.0/16.0;
//    kernel[7] = 2.0/16.0;
//    kernel[8] = 1.0/16.0;
//
//    // Apply blur effect
//    vec2 offset[9];
//    offset[0] = v_texCoords + vec2(-step_w, -step_h);
//    offset[1] = v_texCoords + vec2(0.0, -step_h);
//    offset[2] = v_texCoords + vec2(step_w, -step_h);
//    offset[3] = v_texCoords + vec2(-step_w, 0.0);
//    offset[4] = v_texCoords + vec2(0.0, 0.0);
//    offset[5] = v_texCoords + vec2(step_w, 0.0);
//    offset[6] = v_texCoords + vec2(-step_w, step_h);
//    offset[7] = v_texCoords + vec2(0.0, step_h);
//    offset[8] = v_texCoords + vec2(step_w, step_h);

    vec4 color = vec4(0.0);
    vec2 dir = vec2(0.0);
    float alpha = 0.0;
    int samples = 10;

    for (int i = 0; i <= samples; i++) {
        float pct = float(i) / float(samples);
        dir = pct * 0.005 * vec2(cos(2.0*3.14159265*pct), sin(2.0*3.14159265*pct));
        color += texture2D(u_texture, v_texCoords + dir);
    }
    color = color / float(samples + 1);

//    for(int i=0; i<9; i++) {
//    vec4 texel = texture2D(u_texture, offset[i]);
//    vec3 temp = vec3(dot(texel.rgb, vec3(0.2125, 0.7154, 0.0721)));
//    sum += temp * kernel[i];
//    alpha += texel.a * kernel[i]; // Use texture's alpha
//    }
//    vec3 color = sum;

    // Apply alpha
    gl_FragColor = color * 0.25f;
}

