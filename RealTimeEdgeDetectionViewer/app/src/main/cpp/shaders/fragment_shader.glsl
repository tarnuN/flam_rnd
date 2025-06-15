#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES sTexture;
varying vec2 vTexCoord;

void main() {
    float dx = 1.0 / 512.0;  // Adjust to texture width
    float dy = 1.0 / 512.0;  // Adjust to texture height

    vec3 color[9];
    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            color[(i+1)*3 + (j+1)] = texture2D(sTexture, vTexCoord + vec2(j*dx, i*dy)).rgb;
        }
    }

    vec3 sobelX = color[2] + 2.0 * color[5] + color[8] - color[0] - 2.0 * color[3] - color[6];
    vec3 sobelY = color[0] + 2.0 * color[1] + color[2] - color[6] - 2.0 * color[7] - color[8];

    float edge = length(sobelX + sobelY);

    gl_FragColor = vec4(vec3(edge), 1.0);
}
