/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.isolines.conrec;

import java.util.Objects;

/**
 * Conrec a straightforward method of contouring some surface represented a regular
 * triangular mesh.
 *
 * Ported from the C++ code by Nicholas Yue (see above copyright notice).
 * See <a href="http://paulbourke.net/papers/conrec">http://paulbourke.net/papers/conrec</a> for full description
 * of code and original C++ source.
 *
 * author  Bradley White
 * @version 1.0
 */
public class Conrec {

    private static final int ARR_SIZE = 5;
    private static final int CASE_1 = 1;
    private static final int CASE_2 = 2;
    private static final int CASE_3 = 3;
    private static final int CASE_4 = 4;
    private static final int CASE_5 = 5;
    private static final int CASE_6 = 6;
    private static final int CASE_7 = 7;
    private static final int CASE_8 = 8;
    private static final int CASE_9 = 9;
    private static final int[][][] CASTAB =
            {
                    {
                            {0, 0, 8}, {0, 2, 5}, {7, 6, 9}
                    },
                    {
                            {0, 3, 4}, {1, 3, 1}, {4, 3, 0}
                    },
                    {
                            {9, 6, 7}, {5, 2, 0}, {8, 0, 0}
                    }
            };
    private static final double H_MUL = 0.25;


    private final double  []  h   =  new double [ARR_SIZE];
    private final int     []  sh  =  new int    [ARR_SIZE];
    private final double  []  xh  =  new double [ARR_SIZE];
    private final double  []  yh  =  new double [ARR_SIZE];

    // Object that knows how to draw the contour
    private final Render render;
 
    /**
     * Creates new Conrec.
     *
     * @param render
     *              the render object used to plot contour lines
     **/
    public  Conrec(final Render render) {
        Objects.requireNonNull(render);
        this.render = render;
    }

    /**
     *     contour is a contouring subroutine for rectangularily spaced data 
     *
     *     It emits calls to a line drawing subroutine supplied by the user
     *     which draws a contour map corresponding to real*4data on a randomly
     *     spaced rectangular grid. The coordinates emitted are in the same
     *     units given in the x() and y() arrays.
     *
     *     Any number of contour levels may be specified but they must be
     *     in order of increasing value.
     *
     *
     * @param d   - matrix of data to contour
     *              ilb,iub,jlb,jub contain the index bounds of data matrix
     * @param ilb - lower vertical bound (i.e. d[ilb:iub][jlb:jlb], bounds are inclusive)
     * @param iub - upper vetical bound (i.e. d[ilb:iub][jlb:jlb], bounds are inclusive)
     * @param jlb - lower horizontal bound (i.e. d[ilb:iub][jlb:jlb], bounds are inclusive)
     * @param jub - upper horizontal bound (i.e. d[ilb:iub][jlb:jlb], bounds are inclusive)
     *              The following two, one dimensional arrays (x and y) contain the horizontal and
     *              vertical coordinates of each sample points.
     * @param x   - data matrix column coordinates
     * @param y   - data matrix row coordinates
     * @param nc  - number of contour levels
     * @param z   - contour levels in increasing order.
     * 
     */
    public void contour(final double[][] d, final int ilb, final int iub, final int jlb, final int jub, final double[] x, final double [] y, final int nc, final double [] z) {
        int         m1;
        int         m2;
        int         m3;
        int         caseValue;
        double      dmin;
        double      dmax;
        double      x1 = 0.0;
        double      x2 = 0.0;
        double      y1 = 0.0;
        double      y2 = 0.0;
        int         i, j, k, m;

        // The indexing of im and jm should be noted as it has to start from zero
        // unlike the fortran counter part
        final int     [] im   = {0, 1, 1, 0};
        final int     [] jm   = {0, 0, 1, 1};

        // Note that castab is arranged differently from the FORTRAN code because
        // Fortran and C/C++ arrays are transposed of each other, in this case
        // it is more tricky as castab is in 3 dimension

        for (j = jub - 1; j >= jlb; j--) {
            for (i = ilb; i <= iub - 1; i++) {
                double temp1, temp2;
                temp1 = Math.min(d[i][j], d[i][j + 1]);
                temp2 = Math.min(d[i + 1][j], d[i + 1][j + 1]);
                dmin  = Math.min(temp1, temp2);
                temp1 = Math.max(d[i][j], d[i][j + 1]);
                temp2 = Math.max(d[i + 1][j], d[i + 1][j + 1]);
                dmax  = Math.max(temp1, temp2);

                if (dmax >= z[0] && dmin <= z[nc - 1]) {
                    for (k = 0; k < nc; k++) {
                        if (z[k] >= dmin && z[k] <= dmax) {
                            for (m = 4; m >= 0; m--) {
                                if (m > 0) {
                                    // The indexing of im and jm should be noted as it has to
                                    // start from zero
                                    h[m] = d[i + im[m - 1]][j + jm[m - 1]] - z[k];
                                    xh[m] = x[i + im[m - 1]];
                                    yh[m] = y[j + jm[m - 1]];
                                } else {
                                    h[0] = H_MUL * (h[1] + h[2] + h[3] + h[4]);
                                    xh[0] = 0.5 * (x[i] + x[i + 1]);
                                    yh[0] = 0.5 * (y[j] + y[j + 1]);
                                }
                                if (h[m] > 0.0) {
                                    sh[m] = 1;
                                } else if (h[m] < 0.0) {
                                    sh[m] = -1;
                                } else {
                                    sh[m] = 0;
                                }
                            }
                            //
                            // Note: at this stage the relative heights of the corners and the
                            // centre are in the h array, and the corresponding coordinates are
                            // in the xh and yh arrays. The centre of the box is indexed by 0
                            // and the 4 corners by 1 to 4 as shown below.
                            // Each triangle is then indexed by the parameter m, and the 3
                            // vertices of each triangle are indexed by parameters m1,m2,and
                            // m3.
                            // It is assumed that the centre of the box is always vertex 2
                            // though this isimportant only when all 3 vertices lie exactly on
                            // the same contour level, in which case only the side of the box
                            // is drawn.
                            //
                            //
                            //      vertex 4 +-------------------+ vertex 3
                            //               | \               / |
                            //               |   \    m-3    /   |
                            //               |     \       /     |
                            //               |       \   /       |
                            //               |  m=2    X   m=2   |       the centre is vertex 0
                            //               |       /   \       |
                            //               |     /       \     |
                            //               |   /    m=1    \   |
                            //               | /               \ |
                            //      vertex 1 +-------------------+ vertex 2
                            //
                            //
                            //
                            //               Scan each triangle in the box
                            //
                            for (m = 1; m <= 4; m++) {
                                m1 = m;
                                m2 = 0;
                                if (m != 4) {
                                    m3 = m + 1;
                                } else {
                                    m3 = 1;
                                }
                                caseValue = CASTAB[sh[m1] + 1][sh[m2] + 1][sh[m3] + 1];
                                if (caseValue != 0) {
                                    switch (caseValue) {
                                        case CASE_1: // Line between vertices 1 and 2
                                            x1 = xh[m1];
                                            y1 = yh[m1];
                                            x2 = xh[m2];
                                            y2 = yh[m2];
                                            break;
                                        case CASE_2: // Line between vertices 2 and 3
                                            x1 = xh[m2];
                                            y1 = yh[m2];
                                            x2 = xh[m3];
                                            y2 = yh[m3];
                                            break;
                                        case CASE_3: // Line between vertices 3 and 1
                                            x1 = xh[m3];
                                            y1 = yh[m3];
                                            x2 = xh[m1];
                                            y2 = yh[m1];
                                            break;
                                        case CASE_4: // Line between vertex 1 and side 2-3
                                            x1 = xh[m1];
                                            y1 = yh[m1];
                                            x2 = xsect(m2, m3);
                                            y2 = ysect(m2, m3);
                                            break;
                                        case CASE_5: // Line between vertex 2 and side 3-1
                                            x1 = xh[m2];
                                            y1 = yh[m2];
                                            x2 = xsect(m3, m1);
                                            y2 = ysect(m3, m1);
                                            break;
                                        case CASE_6: //  Line between vertex 3 and side 1-2
                                            x1 = xh[m3];
                                            y1 = yh[m3];
                                            x2 = xsect(m1, m2);
                                            y2 = ysect(m1, m2);
                                            break;
                                        case CASE_7: // Line between sides 1-2 and 2-3
                                            x1 = xsect(m1, m2);
                                            y1 = ysect(m1, m2);
                                            x2 = xsect(m2, m3);
                                            y2 = ysect(m2, m3);
                                            break;
                                        case CASE_8: // Line between sides 2-3 and 3-1
                                            x1 = xsect(m2, m3);
                                            y1 = ysect(m2, m3);
                                            x2 = xsect(m3, m1);
                                            y2 = ysect(m3, m1);
                                            break;
                                        case CASE_9: // Line between sides 3-1 and 1-2
                                            x1 = xsect(m3, m1);
                                            y1 = ysect(m3, m1);
                                            x2 = xsect(m1, m2);
                                            y2 = ysect(m1, m2);
                                            break;
                                        default:
                                            break;
                                    }
                                    render.drawContour(x1, y1, x2, y2, z[k]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private double xsect(final int p1, final int p2) {
        return (h[p2] * xh[p1] - h[p1] * xh[p2]) / (h[p2] - h[p1]);
    }

    private double ysect(final int p1, final int p2) {
        return (h[p2] * yh[p1] - h[p1] * yh[p2]) / (h[p2] - h[p1]);
    }

}

