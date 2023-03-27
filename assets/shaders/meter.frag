in vec2 uv;
uniform float amount;

#define PI 3.14159265

// 1 if in, 0 oth.
float circle(float r, float o, vec2 p) {
    return 1. - step(o, abs(length(p) - r));
}

void main() {
    uv = uv*2. - 1.;

    float inside = circle(0.9, 0.1, uv);

    if (inside == 0.) {
        discard;
    }

    vec3 outline = vec3(0.535,0.535,0.535);
    vec3 filled = vec3(1);

    float ang = (atan(-uv.x, -uv.y) + PI) / (2.*PI);

    if (ang <= amount) {
    	gl_FragColor = vec4(filled,1.0);
    } else {
        gl_FragColor = vec4(outline,1.0);
    }
}