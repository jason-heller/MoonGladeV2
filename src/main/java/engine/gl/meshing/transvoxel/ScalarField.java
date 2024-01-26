package engine.gl.meshing.transvoxel;

public interface ScalarField {
    float getDensity(int x, int y, int z);
}
