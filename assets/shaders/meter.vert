attribute vec4 a_position;
attribute vec2 a_texCoord;
attribute vec2 i_offset;
attribute float i_amount;

uniform mat4 u_projTrans;
varying vec2 v_uv;

void main() {
    v_uv = a_texCoord;
    gl_Position = u_projTrans * (a_position + vec4(i_offset, 0., 0.));
}