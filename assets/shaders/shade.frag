#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

const float[9] kernel = float[9](
1.0/16.0, 2.0/16.0, 1.0/16.0,
2.0/16.0, 4.0/16.0, 2.0/16.0,
1.0/16.0, 2.0/16.0, 1.0/16.0
);

void main()
{
    float step_w = 0.01f;
    float step_h = 0.01f;

    // Apply blur effect
    vec2 offset[9];
    offset[0] = v_texCoords + vec2(-step_w, -step_h);
    offset[1] = v_texCoords + vec2(0.0, -step_h);
    offset[2] = v_texCoords + vec2(step_w, -step_h);
    offset[3] = v_texCoords + vec2(-step_w, 0.0);
    offset[4] = v_texCoords + vec2(0.0, 0.0);
    offset[5] = v_texCoords + vec2(step_w, 0.0);
    offset[6] = v_texCoords + vec2(-step_w, step_h);
    offset[7] = v_texCoords + vec2(0.0, step_h);
    offset[8] = v_texCoords + vec2(step_w, step_h);

    vec3 sum = vec3(0.0);
    for(int i=0; i<9; i++) {
    vec3 temp = vec3(dot(texture2D(u_texture, offset[i]).rgb, vec3(0.2125, 0.7154, 0.0721)));
    sum += temp * kernel[i];
    }
    vec3 color = sum;

    // Apply alpha
    gl_FragColor = vec4(color, 0.3);
}
