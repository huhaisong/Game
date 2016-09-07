package com.example.a111.game.video.video2d;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.a111.game.util.MemUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.EventObject;

/**
 * Created by 111 on 2016/8/18.
 */
public class VRBaseView extends EventObject {
    private int mWidth;
    private int mHeight;
    private int mL;
    private int mT;
    public int id = -1;
    public Bitmap mBitmap = null;
    private Bitmap mBitmapOn = null;
    private Bitmap mBitmapOff = null;
    private boolean mOff = false;
    private boolean mSof = false;
    private int mTextureId = 0;

    private FloatBuffer textureBuffer;
    private FloatBuffer textureBufferLeft;
    private FloatBuffer textureBufferRight;
    private FloatBuffer vertexBuffer;
    private ShortBuffer indicesBuffer;
    public float t;
    public float b;
    public float l;
    public float r;
    public boolean Checked = false;
    private boolean IsOn = false;
    float[] mMatrix = null;
    long startTime = 0;
    long animationtimes = 500;
    float animationvalue = 0.1f;
    private Context mContext;

    private void init(int l, int t, int w, int h, int xParallels, int yParallels, float z) {
        mL = l;
        mT = t;
        mWidth = w;
        mHeight = h;
        mTextureId = newTexture();
        esGenSphere(xParallels, yParallels, z);
    }

    public VRBaseView(Object source, int mL, int mT, int mWidth, int mHeight, int xParallels, int yParallels, float z, Context context, int id) {
        super(source);
        init(mL, mT, mWidth, mHeight, xParallels, yParallels, z);
        mMatrix = new float[16];

        this.id = id;
        this.mContext = context;
        updateimg();
    }

    private boolean upimage = false;

    protected void updateimg() {
        if (mSof && (mBitmapOff != null)) {
            Checked = false;
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmapOff, 0);
        } else if (mBitmap != null) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            if (mBitmapOn != null && IsOn) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmapOn, 0);
            } else {
                Checked = false;
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            }
        }
    }

    protected void updatecheng(float[] EulerAngles) {
        if (upimage) {
            updateimg();
            upimage = false;
        }
        if ((mSof != mOff) && (mBitmapOff != null)) {
            mSof = mOff;
            updateimg();
        }
        if (mSof) {
        } else {
            boolean ison = false;
            if (EulerAngles[0] > b && EulerAngles[0] < t && EulerAngles[1] > r && EulerAngles[1] < l) {
                ison = true;
            }
            if (IsOn != ison) {
                IsOn = ison;
                startTime = System.currentTimeMillis();
                updateimg();
            }
        }
    }

    protected void Draw(int attribPosition, int attribTexCoord, float[] EulerAngles, float[] matrix, int mMVPMatrixHandle) {
        updatecheng(EulerAngles);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glVertexAttribPointer(attribPosition, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
        GLES20.glVertexAttribPointer(attribTexCoord, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);
        GLES20.glEnableVertexAttribArray(attribPosition);
        GLES20.glEnableVertexAttribArray(attribTexCoord);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        if (IsOn) {
            updateMartix(matrix, mMVPMatrixHandle);
        }
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, indicesBuffer);
        if (IsOn) {
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);
        }
    }

    protected void Draw(int attribPosition, int attribTexCoord, float[] EulerAngles, float[] matrix, int mMVPMatrixHandle, int textureId) {
        int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
        updatecheng(EulerAngles);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glVertexAttribPointer(attribPosition, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
        GLES20.glVertexAttribPointer(attribTexCoord, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);
        GLES20.glEnableVertexAttribArray(attribPosition);
        GLES20.glEnableVertexAttribArray(attribTexCoord);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, indicesBuffer);

    }

    protected void DrawLeft(int attribPosition, int attribTexCoord, float[] EulerAngles, float[] matrix, int mMVPMatrixHandle, int textureId) {
        int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
        updatecheng(EulerAngles);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glVertexAttribPointer(attribPosition, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
        GLES20.glVertexAttribPointer(attribTexCoord, 2, GLES20.GL_FLOAT, false, 8, textureBufferLeft);
        GLES20.glEnableVertexAttribArray(attribPosition);
        GLES20.glEnableVertexAttribArray(attribTexCoord);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, indicesBuffer);

    }

    protected void DrawRight(int attribPosition, int attribTexCoord, float[] EulerAngles, float[] matrix, int mMVPMatrixHandle, int textureId) {
        int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
        updatecheng(EulerAngles);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glVertexAttribPointer(attribPosition, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
        GLES20.glVertexAttribPointer(attribTexCoord, 2, GLES20.GL_FLOAT, false, 8, textureBufferRight);
        GLES20.glEnableVertexAttribArray(attribPosition);
        GLES20.glEnableVertexAttribArray(attribTexCoord);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, indicesBuffer);

    }

    private void updateMartix(float[] matrix, int mMVPMatrixHandle) {

        if (mMatrix != null) {
            long t = System.currentTimeMillis() - startTime;
            float z = 0.0f;
            if (t > animationtimes) {
                t = animationtimes;
                Checked = true;
                //notifyEvent(this);
            }
            z = t * animationvalue / animationtimes;
            for (int i = 0; i < 15; i++) {
                mMatrix[i] = matrix[i];
            }
            mMatrix[15] = matrix[15] - z;
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMatrix, 0);
        }
    }

    private int newTexture() {
        int[] textureId = new int[1];
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return textureId[0];
    }

    int numVertices = 0;
    int numIndices = 0;

    private int esGenSphere(int numSlices, int numParallels, float d) {
        int i;
        int j;
        int iidex = 0;
        numVertices = (numParallels + 1) * (numSlices + 1);
        numIndices = numParallels * numSlices * 6;
        float angleFistx = (float) ((float) mL / (Base.MaxWidth) * (2.0f * Math.PI));
        float angleStepX = (float) ((float) mWidth / (Base.MaxWidth) * (2.0f * Math.PI) / ((float) numSlices));
        float angleStepY = (float) ((float) mHeight / (Base.MaxWidth) * (2.0f * Math.PI) / ((float) numParallels));
        float angleFisty = (float) ((float) mT / (Base.MaxWidth) * (2.0f * Math.PI));

        t = (float) (Math.PI / 2.0f - angleFisty);
        b = (float) (t - ((float) mHeight / (Base.MaxWidth) * (2.0f * Math.PI)));
        l = (float) (Math.PI - angleFistx);
        r = (float) ((float) Math.PI - (angleFistx + (float) mWidth / (Base.MaxWidth) * (2.0f * Math.PI)));
        float vertices[] = new float[numVertices * 3];
        float texCoords[] = new float[numVertices * 2];
        float texCoordRights[] = new float[numVertices * 2];
        float texCoordLefts[] = new float[numVertices * 2];
        short indices[] = new short[numIndices];

        for (i = 0; i < numParallels + 1; i++) {
            for (j = 0; j < numSlices + 1; j++) {
                int vertex = (i * (numSlices + 1) + j) * 3;
                vertices[vertex + 0] = 0.0f - (float) (d * Math.sin(angleFisty + angleStepY * (float) i) * Math.sin(angleFistx + angleStepX * (float) j));
                vertices[vertex + 1] = (float) (d * Math.cos(angleFisty + angleStepY * (float) i));
                vertices[vertex + 2] = (float) (d * Math.sin(angleFisty + angleStepY * (float) i) * Math.cos(angleFistx + angleStepX * (float) j));

                int texIndex = (i * (numSlices + 1) + j) * 2;
                texCoordRights[texIndex + 0] = (float) j / (float) numSlices/2+0.5f;
                texCoordRights[texIndex + 1] = ((float) i / (float) numParallels);

                texCoordLefts[texIndex + 0] = (float) j / (float) numSlices/2;
                texCoordLefts[texIndex + 1] = ((float) i / (float) numParallels);

                texCoords[texIndex + 0] = (float) j / (float) numSlices;
                texCoords[texIndex + 1] = ((float) i / (float) numParallels);
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


        vertexBuffer = MemUtil.makeFloatBuffer(vertices);
        textureBufferLeft = MemUtil.makeFloatBuffer(texCoordLefts);
        textureBufferRight = MemUtil.makeFloatBuffer(texCoordRights);
        textureBuffer = MemUtil.makeFloatBuffer(texCoords);
        indicesBuffer = MemUtil.makeShortBuffer(indices);
        return numIndices;
    }

    public void setOff(boolean c) {
        mOff = c;
        if (c) {
            IsOn = false;
            Checked = false;
        }
    }

    public void setBmp(Bitmap Img, Bitmap OnImg) {
        mBitmap = Img;
        mBitmapOn = OnImg;
        upimage = true;
    }


/*    private Vector repository = new Vector();
    private VRWindowListener vrWindowListener;

    public void addListener(VRWindowListener vrWindowListener) {
        repository.addElement(vrWindowListener);
    }

    public void notifyEvent(VRBaseView vrBaseView) {
        Enumeration aenum = repository.elements();
        while (aenum.hasMoreElements()) {
            vrWindowListener = (VRWindowListener) aenum.nextElement();
            vrWindowListener.handleEvent(vrBaseView);
        }
    }*/
}
