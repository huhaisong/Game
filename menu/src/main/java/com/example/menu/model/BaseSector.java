package com.example.menu.model;

import android.opengl.GLES20;

import com.example.menu.util.AABB3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by 111 on 2016/8/30.
 */
public class BaseSector extends TouchableObject {

    private SectorProgram mSectorProgram;

    public BaseSector(int l, int t, int w, int h, int xParallels, int yParallels, float diameter,int id) {
        this.id = id;
        setInitStack();
        mSectorProgram = SectorProgram.getInstance();
        esGenSector(l, t, w, h, xParallels, yParallels, diameter);
    }

    private int numIndices;
    private FloatBuffer mTexCoorBuffer;
    private FloatBuffer mVertexBuffer;
    private ShortBuffer IndicesBuffer;

    private int esGenSector(int l, int t, int w, int h, int numSlices, int numParallels, float diameter) {

        int i;
        int j;
        int iidex = 0;
        int numVertices = (numParallels + 1) * (numSlices + 1);
        numIndices = numParallels * numSlices * 6;
        float angleFistx = (float) ((float) l / (4000) * (2.0f * Math.PI));
        float angleStepX = (float) ((float) w / (4000) * (2.0f * Math.PI) / ((float) numSlices));
        float angleStepY = (float) ((float) h / (4000) * (2.0f * Math.PI) / ((float) numParallels));
        float angleFisty = (float) ((float) t / (4000) * (2.0f * Math.PI));

        float vertices[] = new float[numVertices * 3];
        float texCoords[] = new float[numVertices * 2];
        short indices[] = new short[numIndices];

        for (i = 0; i < numParallels + 1; i++) {
            for (j = 0; j < numSlices + 1; j++) {
                int vertex = (i * (numSlices + 1) + j) * 3;
                vertices[vertex + 0] = 0.0f - (float) (diameter * Math.sin(angleFisty + angleStepY * (float) i) * Math.sin(angleFistx + angleStepX * (float) j));
                vertices[vertex + 1] = (float) (diameter * Math.cos(angleFisty + angleStepY * (float) i));
                vertices[vertex + 2] = (float) (diameter * Math.sin(angleFisty + angleStepY * (float) i) * Math.cos(angleFistx + angleStepX * (float) j));

                int texIndex = (i * (numSlices + 1) + j) * 2;
                texCoords[texIndex + 0] = (float) j / (float) numSlices;
                texCoords[texIndex + 1] = ((float) i / (float) numParallels);//((float)i/(float)numParallels);//
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

        preBox = new AABB3(vertices);
        ByteBuffer bb = ByteBuffer.allocateDirect(
                vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());

        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(
                texCoords.length * 4);
        cc.order(ByteOrder.nativeOrder());

        mTexCoorBuffer = cc.asFloatBuffer();
        mTexCoorBuffer.put(texCoords);
        mTexCoorBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(
                indices.length * 2);
        dd.order(ByteOrder.nativeOrder());

        IndicesBuffer = dd.asShortBuffer();
        IndicesBuffer.put(indices);
        IndicesBuffer.position(0);
        return numIndices;
    }

    public void draw(int mTextureId) {

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        //制定使用某套shader程序
        GLES20.glUseProgram(mSectorProgram.mProgram);
        //将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(mSectorProgram.muMVPMatrixHandle, 1, false, getFinalMatrix(), 0);

        //传送顶点位置数据
        GLES20.glVertexAttribPointer(mSectorProgram.maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);
        //传送顶点纹理坐标数据
        GLES20.glVertexAttribPointer(mSectorProgram.maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTexCoorBuffer);
        //启用顶点位置数据
        GLES20.glEnableVertexAttribArray(mSectorProgram.maPositionHandle);
        //启用顶点纹理数据
        GLES20.glEnableVertexAttribArray(mSectorProgram.maTexCoorHandle);

        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        //绘制纹理矩形
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, IndicesBuffer);
    }
}
