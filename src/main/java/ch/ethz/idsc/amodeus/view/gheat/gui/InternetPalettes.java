/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.StrictColorDataIndexed;

/* package */ class InternetPalettes {

    public static ColorDataIndexed createCool() {
        int[][] sum = new int[][] { { 39, 254, 253, 255 }, { 39, 254, 253, 255 }, { 39, 254, 253, 255 }, { 40, 251, 253, 255 }, { 40, 250, 253, 255 }, { 40, 250, 253, 255 },
                { 40, 250, 253, 255 }, { 41, 246, 253, 255 }, { 41, 246, 253, 255 }, { 41, 246, 253, 255 }, { 41, 247, 253, 255 }, { 41, 244, 253, 255 }, { 42, 241, 253, 255 },
                { 42, 241, 253, 255 }, { 42, 241, 253, 255 }, { 42, 239, 253, 255 }, { 43, 237, 253, 255 }, { 43, 237, 253, 255 }, { 43, 237, 253, 255 }, { 44, 235, 253, 255 },
                { 44, 233, 253, 255 }, { 44, 233, 253, 255 }, { 44, 233, 253, 255 }, { 45, 230, 253, 255 }, { 46, 229, 253, 255 }, { 46, 229, 253, 255 }, { 46, 229, 253, 255 },
                { 47, 226, 253, 255 }, { 47, 225, 253, 255 }, { 47, 225, 253, 255 }, { 47, 225, 253, 255 }, { 49, 222, 253, 255 }, { 49, 221, 253, 255 }, { 49, 221, 253, 255 },
                { 49, 221, 253, 255 }, { 50, 220, 253, 255 }, { 52, 217, 253, 255 }, { 52, 217, 253, 255 }, { 52, 217, 253, 255 }, { 53, 216, 253, 255 }, { 54, 213, 253, 255 },
                { 54, 213, 253, 255 }, { 54, 213, 253, 255 }, { 55, 211, 253, 255 }, { 57, 207, 253, 255 }, { 57, 208, 253, 255 }, { 57, 209, 253, 255 }, { 59, 205, 253, 255 },
                { 60, 203, 253, 255 }, { 60, 203, 253, 255 }, { 60, 203, 253, 255 }, { 62, 200, 252, 255 }, { 63, 199, 252, 255 }, { 63, 199, 252, 255 }, { 63, 199, 252, 255 },
                { 65, 196, 252, 255 }, { 66, 195, 252, 255 }, { 66, 195, 252, 255 }, { 66, 195, 252, 255 }, { 66, 195, 252, 255 }, { 69, 191, 252, 255 }, { 69, 191, 252, 255 },
                { 69, 191, 252, 255 }, { 70, 190, 252, 255 }, { 72, 187, 252, 255 }, { 72, 187, 252, 255 }, { 72, 187, 252, 255 }, { 73, 186, 252, 255 }, { 75, 183, 252, 255 },
                { 75, 183, 252, 255 }, { 75, 183, 252, 255 }, { 76, 181, 252, 255 }, { 78, 179, 252, 255 }, { 78, 179, 252, 255 }, { 78, 179, 252, 255 }, { 80, 177, 252, 255 },
                { 82, 175, 252, 255 }, { 82, 175, 252, 255 }, { 82, 175, 252, 255 }, { 84, 172, 252, 255 }, { 85, 171, 252, 255 }, { 85, 171, 252, 255 }, { 85, 171, 252, 255 },
                { 85, 171, 252, 255 }, { 89, 167, 252, 255 }, { 89, 167, 252, 255 }, { 89, 167, 252, 255 }, { 89, 167, 252, 255 }, { 92, 163, 252, 255 }, { 92, 163, 252, 255 },
                { 92, 163, 252, 255 }, { 93, 162, 252, 255 }, { 96, 159, 252, 255 }, { 96, 159, 252, 255 }, { 96, 159, 252, 255 }, { 97, 158, 252, 255 }, { 99, 155, 252, 255 },
                { 99, 155, 252, 255 }, { 99, 155, 252, 255 }, { 101, 153, 252, 255 }, { 103, 151, 252, 255 }, { 103, 151, 252, 255 }, { 103, 151, 252, 255 },
                { 105, 149, 252, 255 }, { 106, 147, 252, 255 }, { 106, 147, 252, 255 }, { 106, 147, 252, 255 }, { 106, 147, 252, 255 }, { 109, 144, 252, 255 },
                { 110, 143, 252, 255 }, { 110, 143, 252, 255 }, { 110, 143, 252, 255 }, { 114, 139, 252, 255 }, { 114, 139, 252, 255 }, { 114, 139, 252, 255 },
                { 114, 139, 252, 255 }, { 117, 135, 252, 255 }, { 117, 135, 252, 255 }, { 117, 135, 252, 255 }, { 118, 134, 252, 255 }, { 121, 131, 252, 255 },
                { 121, 131, 252, 255 }, { 121, 131, 252, 255 }, { 122, 130, 252, 255 }, { 125, 127, 252, 255 }, { 125, 127, 252, 255 }, { 125, 127, 252, 255 },
                { 125, 127, 252, 255 }, { 127, 125, 252, 255 }, { 129, 123, 252, 255 }, { 129, 123, 252, 255 }, { 129, 123, 252, 255 }, { 132, 120, 252, 255 },
                { 133, 119, 252, 255 }, { 133, 119, 252, 255 }, { 133, 119, 252, 255 }, { 136, 116, 252, 255 }, { 137, 115, 252, 255 }, { 137, 115, 252, 255 },
                { 137, 115, 252, 255 }, { 141, 111, 252, 255 }, { 141, 111, 252, 255 }, { 141, 111, 252, 255 }, { 141, 111, 252, 255 }, { 145, 107, 252, 255 },
                { 145, 107, 252, 255 }, { 145, 107, 252, 255 }, { 146, 106, 252, 255 }, { 149, 103, 252, 255 }, { 149, 103, 252, 255 }, { 149, 103, 252, 255 },
                { 149, 103, 252, 255 }, { 150, 101, 252, 255 }, { 152, 99, 252, 255 }, { 152, 99, 252, 255 }, { 152, 99, 252, 255 }, { 154, 97, 252, 255 }, { 156, 96, 252, 255 },
                { 156, 96, 252, 255 }, { 156, 96, 252, 255 }, { 159, 93, 252, 255 }, { 160, 92, 252, 255 }, { 160, 92, 252, 255 }, { 160, 92, 252, 255 }, { 163, 89, 252, 255 },
                { 164, 88, 252, 255 }, { 164, 88, 252, 255 }, { 164, 88, 252, 255 }, { 168, 84, 252, 255 }, { 168, 84, 252, 255 }, { 168, 84, 252, 255 }, { 168, 84, 252, 255 },
                { 172, 80, 252, 255 }, { 172, 80, 252, 255 }, { 172, 80, 252, 255 }, { 172, 80, 252, 255 }, { 173, 79, 252, 255 }, { 176, 77, 252, 255 }, { 176, 77, 252, 255 },
                { 176, 77, 252, 255 }, { 178, 75, 252, 255 }, { 180, 73, 252, 255 }, { 180, 73, 252, 255 }, { 180, 73, 252, 255 }, { 182, 71, 252, 255 }, { 184, 69, 252, 255 },
                { 184, 69, 252, 255 }, { 184, 69, 252, 255 }, { 187, 67, 252, 255 }, { 188, 66, 252, 255 }, { 188, 66, 252, 255 }, { 188, 66, 252, 255 }, { 191, 63, 252, 255 },
                { 192, 62, 252, 255 }, { 192, 62, 252, 255 }, { 192, 62, 252, 255 }, { 196, 58, 252, 255 }, { 196, 58, 252, 255 }, { 196, 58, 252, 255 }, { 196, 58, 252, 255 },
                { 197, 57, 252, 255 }, { 200, 55, 252, 255 }, { 200, 55, 252, 255 }, { 200, 55, 252, 255 }, { 201, 54, 252, 255 }, { 204, 51, 252, 255 }, { 204, 51, 252, 255 },
                { 204, 51, 252, 255 }, { 206, 50, 252, 255 }, { 208, 48, 252, 255 }, { 208, 48, 252, 255 }, { 207, 48, 252, 255 }, { 211, 46, 252, 255 }, { 213, 44, 252, 255 },
                { 213, 44, 252, 255 }, { 213, 44, 252, 255 }, { 216, 41, 252, 255 }, { 217, 40, 252, 255 }, { 217, 40, 252, 255 }, { 217, 40, 252, 255 }, { 220, 38, 252, 255 },
                { 221, 37, 252, 255 }, { 221, 37, 252, 255 }, { 221, 37, 252, 255 }, { 222, 37, 252, 255 }, { 225, 34, 252, 255 }, { 225, 34, 252, 255 }, { 225, 34, 252, 255 },
                { 226, 33, 252, 255 }, { 229, 31, 252, 255 }, { 229, 31, 252, 255 }, { 229, 31, 252, 255 }, { 230, 30, 252, 255 }, { 233, 28, 252, 255 }, { 233, 28, 252, 255 },
                { 233, 28, 252, 255 }, { 235, 27, 252, 255 }, { 237, 26, 252, 255 }, { 237, 26, 252, 255 }, { 237, 26, 252, 255 }, { 239, 25, 252, 255 }, { 241, 24, 252, 255 },
                { 241, 24, 252, 255 }, { 241, 24, 252, 255 }, { 244, 23, 252, 255 }, { 245, 22, 252, 255 }, { 245, 22, 252, 255 }, { 245, 22, 252, 255 }, { 245, 22, 252, 255 },
                { 249, 20, 253, 255 }, { 249, 20, 253, 255 }, { 249, 20, 253, 255 }, { 250, 20, 253, 255 }, { 253, 17, 253, 255 }, { 253, 17, 253, 255 }, { 253, 17, 253, 255 } };
        Tensor matrix = Tensors.reserve(256);
        for (int c = 0; c < 256; ++c)
            matrix.append(Tensors.vector(sum[c][0], sum[c][1], sum[c][2], 255 - c));
        return StrictColorDataIndexed.of(matrix);
    }

}
