attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

void main()
{
    v_color = a_color;
    v_color.a = v_color.a * (255.0/254.0);
    v_texCoords = a_texCoord0;
    vec2 pos = (u_projTrans * vec4(a_position.xy, 0, 1)).xy;
    gl_Position = vec4(pos, a_position.z, 1);
}
