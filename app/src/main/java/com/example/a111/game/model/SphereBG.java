package com.example.a111.game.model;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.a111.game.util.MemUtil;
import com.example.a111.game.util.ShaderUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.Matrix.perspectiveM;

public class SphereBG {

    private String mFragmentShader = "\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main()                         \n" +
            "{           \n" +
            "   gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}              ";
    private String mVertexShader = "uniform mat4 uMVPMatrix;\n" +
            "attribute vec3 aPosition;\n" +
            "attribute vec2 aTexCoor;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main()     \n" +
            "{\n" +
            "   gl_Position = uMVPMatrix * vec4(aPosition,1);\n" +
            "   vTextureCoord = aTexCoor;\n" +
            "}";


    private int maPositionHandle, maTexCoorHandle;
    private int mProgram;
    private int mMVPMatrixHandle;

    int count;

    private FloatBuffer vertexBuffer, textureBuffer;
    private ShortBuffer IndicesBuffer;

    private float[] mProjMatrix = new float[16];//4x4矩阵 投影用
    private float[] mMVPMatrix = new float[16];

    public SphereBG() {

        initData();
        initShader();
    }

    private void initShader() {

        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    private void initData() {
        count = esGenSphere(100, 100);
    }

    /**
     * @param numSlices 切分次数
     * @param d         半径
     */
    private int esGenSphere(int numSlices, float d) {
        int i;
        int j;
        int iidex = 0;
        int numParallels = numSlices / 2;
        int numVertices = (numParallels + 1) * (numSlices + 1);
        int numIndices = numParallels * numSlices * 6;
        float angleStep = (float) ((2.0f * Math.PI) / ((float) numSlices));
        float vertices[] = new float[numVertices * 3];
        float texCoords[] = new float[numVertices * 2];
        // float texRightCoords[] = new float[numVertices * 2];

        short indices[] = new short[numIndices];
        for (i = 0; i < numParallels + 1; i++) {
            for (j = 0; j < numSlices + 1; j++) {
                int vertex = (i * (numSlices + 1) + j) * 3;
                vertices[vertex] = (float) (d * Math.sin(angleStep * (float) i) * Math.sin(angleStep * (float) j));
                vertices[vertex + 1] = (float) (d * Math.cos(angleStep * (float) i));
                vertices[vertex + 2] = (float) (d * Math.sin(angleStep * (float) i) * Math.cos(angleStep * (float) j));

                int texIndex = (i * (numSlices + 1) + j) * 2;
                texCoords[texIndex] = 1.0f - (float) j / (float) numSlices;
                texCoords[texIndex + 1] = ((float) i / (float) numParallels);//((float)i/(float)numParallels);//

                // texRightCoords[texIndex] = 1.0f - (float) j / (float) numSlices;
                // texRightCoords[texIndex + 1] = ((float) i / (float) numParallels) / 2 + 0.5f;
            }
        }

        for (i = 0; i < numParallels; i++) {
            for (j = 0; j < numSlices; j++) {
                indices[iidex++] = (short) (i * (numSlices + 1) + j);
                indices[iidex++] = (short) ((i + 1) * (numSlices + 1) + j);
                indices[iidex++] = (short) ((i + 1) * (numSlices + 1) + (j + 1));

                indices[iidex++] = (short) (i * (numSlices + 1) + j);
                indices[iidex++] = (short) ((i + 1) * (numSlices + 1) + (j + 1));
                indices[iidex++] = (short) (i * (numSlices + 1) + (j + 1));
            }
        }

        //texRighttureBuffer = MemUtil.makeFloatBuffer(texRightCoords);
        vertexBuffer = MemUtil.makeFloatBuffer(vertices);
        textureBuffer = MemUtil.makeFloatBuffer(texCoords);
        IndicesBuffer = MemUtil.makeShortBuffer(indices);
        return numIndices;
    }

    //设置透视投影参数
    public void setProjectFrustum(int mWidth,int mHeight) {
        perspectiveM(mProjMatrix, 0, 75.0f, mWidth / mHeight / 2.0f, 0.1f, 500.0f);
    }
    //设置透视投影参数
    public void setProjectFrustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public void drawSelf(float[] mHeadView, int textureId) {

        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mHeadView, 0);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
        GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, textureBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, count, GLES20.GL_UNSIGNED_SHORT, IndicesBuffer);
    }
}
