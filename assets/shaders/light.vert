attribute vec4 vertex_positions;
attribute vec4 quad_colors;
attribute float s;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec2 v_pos;
void main()
{
    v_color = s * quad_colors;
    v_pos = vertex_positions.xy;
    gl_Position =  u_projTrans * vertex_positions;
}

