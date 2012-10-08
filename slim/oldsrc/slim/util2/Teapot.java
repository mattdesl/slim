package slim.util2;


import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;

/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

public class Teapot {

    /**
       * Renders the teapot as a solid shape of the specified size. The teapot is
       * created in a way that replicates the C GLUT implementation.
       *
       * @param scale
       *        the factor by which to scale the teapot
       */
      public static void glutSolidTeapot(double scale) {
        glutSolidTeapot(scale, true);
      }

      /**
       * Renders the teapot as a solid shape of the specified size. The teapot can
       * either be created in a way that is backward-compatible with the standard
       * C glut library (i.e. broken), or in a more pleasing way (i.e. with
       * surfaces whose front-faces point outwards and standing on the z=0 plane,
       * instead of the y=-1 plane). Both surface normals and texture coordinates
       * for the teapot are generated. The teapot is generated with OpenGL
       * evaluators.
       *
       * @param scale
       *        the factor by which to scale the teapot
       * @param cStyle
       *        whether to create the teapot in exactly the same way as in the C
       *        implementation of GLUT
       */
      public static void glutSolidTeapot(double scale, boolean cStyle) {
        teapot(14, scale, GL_FILL, cStyle);
      }

      /**
       * Renders the teapot as a wireframe shape of the specified size. The teapot
       * is created in a way that replicates the C GLUT implementation.
       *
       * @param scale
       *        the factor by which to scale the teapot
       */
      public static void glutWireTeapot(double scale) {
        glutWireTeapot(scale, true);
      }

      /**
       * Renders the teapot as a wireframe shape of the specified size. The teapot
       * can either be created in a way that is backward-compatible with the
       * standard C glut library (i.e. broken), or in a more pleasing way (i.e.
       * with surfaces whose front-faces point outwards and standing on the z=0
       * plane, instead of the y=-1 plane). Both surface normals and texture
       * coordinates for the teapot are generated. The teapot is generated with
       * OpenGL evaluators.
       *
       * @param scale
       *        the factor by which to scale the teapot
       * @param cStyle
       *        whether to create the teapot in exactly the same way as in the C
       *        implementation of GLUT
       */
      public static void glutWireTeapot(double scale, boolean cStyle) {
        teapot(10, scale, GL_LINE, cStyle);
      }


    // Teapot implementation (a modified port of glut_teapot.c)
    //
    // Rim, body, lid, and bottom data must be reflected in x and
    // y; handle and spout data across the y axis only.
    private static final int[][] teapotPatchData = {
            /* rim */
            {102, 103, 104, 105, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            /* body */
            {12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27},
            {24, 25, 26, 27, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40},
            /* lid */
            {96, 96, 96, 96, 97, 98, 99, 100, 101, 101, 101, 101, 0, 1, 2, 3,},
            {0, 1, 2, 3, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117},
            /* bottom */
            {118, 118, 118, 118, 124, 122, 119, 121, 123, 126, 125, 120, 40, 39, 38, 37},
            /* handle */
            {41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56},
            {53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 28, 65, 66, 67},
            /* spout */
            {68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83},
            {80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95}
    };
    private static final float[][] teapotCPData = {
            {0.2f, 0f, 2.7f},
            {0.2f, -0.112f, 2.7f},
            {0.112f, -0.2f, 2.7f},
            {0f, -0.2f, 2.7f},
            {1.3375f, 0f, 2.53125f},
            {1.3375f, -0.749f, 2.53125f},
            {0.749f, -1.3375f, 2.53125f},
            {0f, -1.3375f, 2.53125f},
            {1.4375f, 0f, 2.53125f},
            {1.4375f, -0.805f, 2.53125f},
            {0.805f, -1.4375f, 2.53125f},
            {0f, -1.4375f, 2.53125f},
            {1.5f, 0f, 2.4f},
            {1.5f, -0.84f, 2.4f},
            {0.84f, -1.5f, 2.4f},
            {0f, -1.5f, 2.4f},
            {1.75f, 0f, 1.875f},
            {1.75f, -0.98f, 1.875f},
            {0.98f, -1.75f, 1.875f},
            {0f, -1.75f, 1.875f},
            {2f, 0f, 1.35f},
            {2f, -1.12f, 1.35f},
            {1.12f, -2f, 1.35f},
            {0f, -2f, 1.35f},
            {2f, 0f, 0.9f},
            {2f, -1.12f, 0.9f},
            {1.12f, -2f, 0.9f},
            {0f, -2f, 0.9f},
            {-2f, 0f, 0.9f},
            {2f, 0f, 0.45f},
            {2f, -1.12f, 0.45f},
            {1.12f, -2f, 0.45f},
            {0f, -2f, 0.45f},
            {1.5f, 0f, 0.225f},
            {1.5f, -0.84f, 0.225f},
            {0.84f, -1.5f, 0.225f},
            {0f, -1.5f, 0.225f},
            {1.5f, 0f, 0.15f},
            {1.5f, -0.84f, 0.15f},
            {0.84f, -1.5f, 0.15f},
            {0f, -1.5f, 0.15f},
            {-1.6f, 0f, 2.025f},
            {-1.6f, -0.3f, 2.025f},
            {-1.5f, -0.3f, 2.25f},
            {-1.5f, 0f, 2.25f},
            {-2.3f, 0f, 2.025f},
            {-2.3f, -0.3f, 2.025f},
            {-2.5f, -0.3f, 2.25f},
            {-2.5f, 0f, 2.25f},
            {-2.7f, 0f, 2.025f},
            {-2.7f, -0.3f, 2.025f},
            {-3f, -0.3f, 2.25f},
            {-3f, 0f, 2.25f},
            {-2.7f, 0f, 1.8f},
            {-2.7f, -0.3f, 1.8f},
            {-3f, -0.3f, 1.8f},
            {-3f, 0f, 1.8f},
            {-2.7f, 0f, 1.575f},
            {-2.7f, -0.3f, 1.575f},
            {-3f, -0.3f, 1.35f},
            {-3f, 0f, 1.35f},
            {-2.5f, 0f, 1.125f},
            {-2.5f, -0.3f, 1.125f},
            {-2.65f, -0.3f, 0.9375f},
            {-2.65f, 0f, 0.9375f},
            {-2f, -0.3f, 0.9f},
            {-1.9f, -0.3f, 0.6f},
            {-1.9f, 0f, 0.6f},
            {1.7f, 0f, 1.425f},
            {1.7f, -0.66f, 1.425f},
            {1.7f, -0.66f, 0.6f},
            {1.7f, 0f, 0.6f},
            {2.6f, 0f, 1.425f},
            {2.6f, -0.66f, 1.425f},
            {3.1f, -0.66f, 0.825f},
            {3.1f, 0f, 0.825f},
            {2.3f, 0f, 2.1f},
            {2.3f, -0.25f, 2.1f},
            {2.4f, -0.25f, 2.025f},
            {2.4f, 0f, 2.025f},
            {2.7f, 0f, 2.4f},
            {2.7f, -0.25f, 2.4f},
            {3.3f, -0.25f, 2.4f},
            {3.3f, 0f, 2.4f},
            {2.8f, 0f, 2.475f},
            {2.8f, -0.25f, 2.475f},
            {3.525f, -0.25f, 2.49375f},
            {3.525f, 0f, 2.49375f},
            {2.9f, 0f, 2.475f},
            {2.9f, -0.15f, 2.475f},
            {3.45f, -0.15f, 2.5125f},
            {3.45f, 0f, 2.5125f},
            {2.8f, 0f, 2.4f},
            {2.8f, -0.15f, 2.4f},
            {3.2f, -0.15f, 2.4f},
            {3.2f, 0f, 2.4f},
            {0f, 0f, 3.15f},
            {0.8f, 0f, 3.15f},
            {0.8f, -0.45f, 3.15f},
            {0.45f, -0.8f, 3.15f},
            {0f, -0.8f, 3.15f},
            {0f, 0f, 2.85f},
            {1.4f, 0f, 2.4f},
            {1.4f, -0.784f, 2.4f},
            {0.784f, -1.4f, 2.4f},
            {0f, -1.4f, 2.4f},
            {0.4f, 0f, 2.55f},
            {0.4f, -0.224f, 2.55f},
            {0.224f, -0.4f, 2.55f},
            {0f, -0.4f, 2.55f},
            {1.3f, 0f, 2.55f},
            {1.3f, -0.728f, 2.55f},
            {0.728f, -1.3f, 2.55f},
            {0f, -1.3f, 2.55f},
            {1.3f, 0f, 2.4f},
            {1.3f, -0.728f, 2.4f},
            {0.728f, -1.3f, 2.4f},
            {0f, -1.3f, 2.4f},
            {0f, 0f, 0f},
            {1.425f, -0.798f, 0f},
            {1.5f, 0f, 0.075f},
            {1.425f, 0f, 0f},
            {0.798f, -1.425f, 0f},
            {0f, -1.5f, 0.075f},
            {0f, -1.425f, 0f},
            {1.5f, -0.84f, 0.075f},
            {0.84f, -1.5f, 0.075f}
    };
    // Since glMap2f expects a packed array of floats, we must convert
    // from a 3-dimensional array to a 1-dimensional array
    private static final float[] teapotTex = {
            0, 0, 1, 0, 0, 1, 1, 1
    };

    public static FloatBuffer toBuffer(float[] src) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(src.length);
        buffer.put(src);
        buffer.rewind();
        return buffer;
    }

    private static void teapot(int grid,
                               double scale,
                               int type,
                               boolean backCompatible) {
        // As mentioned above, glMap2f expects a packed array of floats
        float[] p = new float[4 * 4 * 3];
        float[] q = new float[4 * 4 * 3];
        float[] r = new float[4 * 4 * 3];
        float[] s = new float[4 * 4 * 3];
        int i, j, k, l;

        glPushAttrib(GL_ENABLE_BIT | GL_EVAL_BIT | GL_POLYGON_BIT);
        glEnable(GL_AUTO_NORMAL);
        glEnable(GL_MAP2_VERTEX_3);
        glEnable(GL_MAP2_TEXTURE_COORD_2);
        glPushMatrix();
        if (!backCompatible) {
            // The time has come to have the teapot no longer be inside out
            glFrontFace(GL_CW);
            glScaled(0.5 * scale, 0.5 * scale, 0.5 * scale);
        } else {
            // We want the teapot in it's backward compatible position and
            // orientation
            glRotatef(270.0f, 1, 0, 0);
            glScalef((float) (0.5 * scale),
                    (float) (0.5 * scale),
                    (float) (0.5 * scale));
            glTranslatef(0.0f, 0.0f, -1.5f);
        }
        for (i = 0; i < 10; i++) {
            for (j = 0; j < 4; j++) {
                for (k = 0; k < 4; k++) {
                    for (l = 0; l < 3; l++) {
                        p[(j * 4 + k) * 3 + l] = teapotCPData[teapotPatchData[i][j * 4 + k]][l];
                        q[(j * 4 + k) * 3 + l] =
                                teapotCPData[teapotPatchData[i][j * 4 + (3 - k)]][l];
                        if (l == 1)
                            q[(j * 4 + k) * 3 + l] *= -1.0;
                        if (i < 6) {
                            r[(j * 4 + k) * 3 + l] =
                                    teapotCPData[teapotPatchData[i][j * 4 + (3 - k)]][l];
                            if (l == 0)
                                r[(j * 4 + k) * 3 + l] *= -1.0;
                            s[(j * 4 + k) * 3 + l] = teapotCPData[teapotPatchData[i][j * 4 + k]][l];
                            if (l == 0)
                                s[(j * 4 + k) * 3 + l] *= -1.0;
                            if (l == 1)
                                s[(j * 4 + k) * 3 + l] *= -1.0;
                        }
                    }
                }
            }

            // glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, java.nio.FloatBuffer points)
            // glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, const GLfloat *points)
            // glMap2f(GLenum target, GLfloat u1, GLfloat u2, GLint ustride, GLint uorder, GLfloat v1, GLfloat v2, GLint vstride, GLint vorder, const GLfloat *points)

            glMap2f(GL_MAP2_TEXTURE_COORD_2, 0.0f, 1.0f, 2, 2, 0.0f, 1.0f, 4, 2, toBuffer(teapotTex));
            glMap2f(GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, toBuffer(p));
            glMapGrid2f(grid, 0.0f, 1.0f, grid, 0.0f, 1.0f);
            evaluateTeapotMesh(grid, type, i, !backCompatible);
            glMap2f(GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, toBuffer(q));
            evaluateTeapotMesh(grid, type, i, !backCompatible);
            if (i < 6) {
                glMap2f(GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, toBuffer(r));
                evaluateTeapotMesh(grid, type, i, !backCompatible);
                glMap2f(GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, toBuffer(s));
                evaluateTeapotMesh(grid, type, i, !backCompatible);
            }
//      glMap2f(GL_MAP2_TEXTURE_COORD_2, 0, 1, 2, 2, 0, 1, 4, 2, teapotTex, 0);
//      glMap2f(GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, p, 0);
//      glMapGrid2f(grid, 0.0f, 1.0f, grid, 0.0f, 1.0f);
//      evaluateTeapotMesh(grid, type, i, !backCompatible);
//      glMap2f(GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, q, 0);
//      evaluateTeapotMesh(grid, type, i, !backCompatible);
//      if (i < 6) {
//        glMap2f(GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, r, 0);
//        evaluateTeapotMesh(grid, type, i, !backCompatible);
//        glMap2f(GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, s, 0);
//        evaluateTeapotMesh(grid, type, i, !backCompatible);
//      }
        }
        glPopMatrix();
        glPopAttrib();
    }

    private static void evaluateTeapotMesh(int grid,
                                           int type,
                                           int partNum,
                                           boolean repairSingularities) {
        if (repairSingularities && (partNum == 5 || partNum == 3)) {
            // Instead of using evaluators that give bad results at singularities,
            // evaluate by hand
            glPolygonMode(GL_FRONT_AND_BACK, type);
            for (int nv = 0; nv < grid; nv++) {
                if (nv == 0) {
                    // Draw a small triangle-fan to fill the hole
                    glDisable(GL_AUTO_NORMAL);
                    glNormal3f(0, 0, partNum == 3 ? 1 : -1);
                    glBegin(GL_TRIANGLE_FAN);
                    {
                        glEvalCoord2f(0, 0);
                        // Note that we draw in clock-wise order to match the evaluator
                        // method
                        for (int nu = 0; nu <= grid; nu++) {
                            glEvalCoord2f(nu / (float) grid, (1f / grid) / (float) grid);
                        }
                    }
                    glEnd();
                    glEnable(GL_AUTO_NORMAL);
                }
                // Draw the rest of the piece as an evaluated quad-strip
                glBegin(GL_QUAD_STRIP);
                {
                    // Note that we draw in clock-wise order to match the evaluator method
                    for (int nu = grid; nu >= 0; nu--) {
                        glEvalCoord2f(nu / (float) grid, (nv + 1) / (float) grid);
                        glEvalCoord2f(nu / (float) grid, Math.max(nv, 1f / grid)
                                / (float) grid);
                    }
                }
                glEnd();
            }
        } else {
            glEvalMesh2(type, 0, grid, 0, grid);
        }
    }


}