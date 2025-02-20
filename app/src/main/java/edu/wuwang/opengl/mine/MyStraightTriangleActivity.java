package edu.wuwang.opengl.mine;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.wuwang.opengl.R;

public class MyStraightTriangleActivity extends AppCompatActivity {


    private GLSurfaceView gl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_triangle);

        initView();
//        initGL();
    }


    @Override
    protected void onResume() {
        super.onResume();
        gl.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        gl.onPause();
    }

    private void initView() {
        gl = (GLSurfaceView) findViewById(R.id.gl);
        MyRenderer myRenderer = new MyRenderer();
        gl.setEGLContextClientVersion(2);
        gl.setRenderer(myRenderer);
        gl.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    private void initGL() {

        ByteBuffer bb = ByteBuffer.allocateDirect(
                triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);
    }


    public int loadShader(int type, String shaderCode) {
        //根据type创建顶点着色器或者片元着色器
        int shader = GLES20.glCreateShader(type);
        //将资源加入到着色器中，并编译
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }


    private FloatBuffer vertexBuffer;
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mProgram;

    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {
            0.5f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    };

    private int mPositionHandle;
    private int mColorHandle;

    //顶点个数
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    //设置颜色，依次为红绿蓝和透明通道
    float color[] = {1.0f, 1.0f, 1.0f, 1.0f};


    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mMatrixHandler;

    class MyRenderer implements GLSurfaceView.Renderer {

//        MyRenderer(){
//            initGL();
//        }

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

            initGL();
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {

            //计算宽高比
            float ratio = (float) width / height;
            //设置透视投影
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
            //设置相机位置
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            //计算变换矩阵
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl10) {

            //将程序加入到OpenGLES2.0环境
            GLES20.glUseProgram(mProgram);
            //获取变换矩阵vMatrix成员句柄
            mMatrixHandler= GLES20.glGetUniformLocation(mProgram,"vMatrix");
            //指定vMatrix的值
            GLES20.glUniformMatrix4fv(mMatrixHandler,1,false,mMVPMatrix,0);
            //获取顶点着色器的vPosition成员句柄
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            //启用三角形顶点的句柄
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            //准备三角形的坐标数据
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);
            //获取片元着色器的vColor成员的句柄
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            //设置绘制三角形的颜色
            GLES20.glUniform4fv(mColorHandle, 1, color, 0);
            //绘制三角形
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
            //禁止顶点数组的句柄
            GLES20.glDisableVertexAttribArray(mPositionHandle);

        }


    }


}
