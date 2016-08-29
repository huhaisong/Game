package com.example.a111.game.model;

import android.opengl.GLES20;

import com.example.a111.game.util.ShaderUtil;

/**
 * Created by 111 on 2016/8/23.
 */
public class BallProgram {

    private static BallProgram mBallProgram;

    public int mProgram;//自定义渲染管线着色器程序id
    public int muMVPMatrixHandle;//总变换矩阵引用
    public int maPositionHandle; //顶点位置属性引用
    public int maTexCoorHandle; //顶点纹理坐标属性引用
    public int maNormalHandle; //顶点纹理坐标属性引用
    public int muMMatrixHandle; //顶点纹理坐标属性引用


    private String mVertexShader =
            "uniform mat4 uMVPMatrix; //总变换矩阵\n" +
            "uniform mat4 uMMatrix; //变换矩阵\n" +
            "attribute vec3 aPosition;  //顶点位置\n" +
            "attribute vec3 aNormal;    //顶点法向量\n" +
            "attribute vec2 aTexCoor;    //顶点纹理坐标\n" +
            "varying vec2 vTextureCoord;  //用于传递给片元着色器的变量\n" +
            "varying vec4 vambient;\n" +
            "varying vec4 vdiffuse;\n" +
            "varying vec4 vspecular; \n" +
            "//定位光光照计算的方法\n" +
            "void pointLight(\t\t\t\t//定位光光照计算的方法\n" +
            "  in vec3 normal,\t\t\t\t//法向量\n" +
            "  inout vec4 ambient,\t\t\t//环境光最终强度\n" +
            "  inout vec4 diffuse,\t\t\t//散射光最终强度\n" +
            "  inout vec4 specular,\t\t\t//镜面光最终强度\n" +
            "  in vec3 lightLocation,\t\t//光源位置\n" +
            "  in vec4 lightAmbient,\t\t\t//环境光强度\n" +
            "  in vec4 lightDiffuse,\t\t\t//散射光强度\n" +
            "  in vec4 lightSpecular\t\t\t//镜面光强度\n" +
            "){\n" +
            "  ambient=lightAmbient;\t\t\t//直接得出环境光的最终强度  \n" +
            "  vec3 normalTarget=aPosition+normal;\t//计算变换后的法向量\n" +
            "  vec3 newNormal=(uMMatrix*vec4(normalTarget,1)).xyz-(uMMatrix*vec4(aPosition,1)).xyz;\n" +
            "  newNormal=normalize(newNormal); \t//对法向量规格化\n" +
            "  //计算从表面点到摄像机的向量\n" +
            "  vec3 eye= normalize(vec3(0.0,0.0,0.0)-(uMMatrix*vec4(aPosition,1)).xyz);  \n" +
            "  //计算从表面点到光源位置的向量vp\n" +
            "  vec3 vp= normalize(lightLocation-(uMMatrix*vec4(aPosition,1)).xyz);  \n" +
            "  vp=normalize(vp);//格式化vp\n" +
            "  vec3 halfVector=normalize(vp+eye);\t//求视线与光线的半向量    \n" +
            "  float shininess=50.0;\t\t\t\t//粗糙度，越小越光滑\n" +
            "  float nDotViewPosition=max(0.0,dot(newNormal,vp)); \t//求法向量与vp的点积与0的最大值\n" +
            "  diffuse=lightDiffuse*nDotViewPosition;\t\t\t\t//计算散射光的最终强度\n" +
            "  float nDotViewHalfVector=dot(newNormal,halfVector);\t//法线与半向量的点积 \n" +
            "  float powerFactor=max(0.0,pow(nDotViewHalfVector,shininess)); \t//镜面反射光强度因子\n" +
            "  specular=lightSpecular*powerFactor;    \t\t\t//计算镜面光的最终强度\n" +
            "}\n" +
            "\n" +
            "void main()     \n" +
            "{  \n" +
            "   gl_Position = uMVPMatrix * vec4(aPosition,1); //根据总变换矩阵计算此次绘制此顶点位置\n" +
            "   pointLight(normalize(aNormal),vambient,vdiffuse,vspecular,vec3(0.0,0.0,0.0),vec4(0.3,0.3,0.3,1.0),vec4(0.7,0.7,0.7,1.0),vec4(0.3,0.3,0.3,1.0));\n" +
            "   vTextureCoord = aTexCoor;//将接收的纹理坐标传递给片元着色器\n" +
            "} ";

    private String mFragmentShader =
            "precision mediump float;\n" +
            "uniform sampler2D sTexture;//纹理内容数据\n" +
            "varying vec2 vTextureCoord; //接收从顶点着色器过来的参数\n" +
            "varying vec4 vambient;\n" +
            "varying vec4 vdiffuse;\n" +
            "varying vec4 vspecular;\n" +
            "void main()                         \n" +
            "{\n" +
            "   //将计算出的颜色给此片元\n" +
            "   vec4 finalColor=texture2D(sTexture, vTextureCoord);\n" +
            "   //给此片元颜色值 \n" +
            "   gl_FragColor = finalColor*vambient+finalColor*vspecular+finalColor*vdiffuse;//给此片元颜色值\n" +
            "}              ";

    private BallProgram() {
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取程序中顶点位置属性引用id
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中顶点纹理坐标属性引用id
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        //获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        //获取程序中顶点法向量属性引用id
        maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        //获取位置、旋转变换矩阵引用id
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
    }

    public static BallProgram getInstance() {

        if (mBallProgram == null) {
            mBallProgram = new BallProgram();
        }
        return mBallProgram;
    }
}
