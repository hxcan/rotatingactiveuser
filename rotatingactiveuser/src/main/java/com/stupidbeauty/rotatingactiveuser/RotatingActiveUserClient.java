package com.stupidbeauty.rotatingactiveuser;

import com.upokecenter.cbor.CBORObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
// import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.AsyncHttpClient;
import java.net.InetSocketAddress;
import com.koushikdutta.async.callback.ConnectCallback;
import android.os.Handler;
// import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
// import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import org.apache.commons.io.FileUtils;
import com.koushikdutta.async.callback.ConnectCallback;
import java.net.InetSocketAddress;
import android.text.format.Formatter;
import android.net.wifi.WifiManager;
import java.util.Random;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RotatingActiveUserClient
{
    private static final String TAG ="RotatingActiveUserClient"; //!<  输出调试信息时使用的标记。
    private Context context; //!< 执行时使用的上下文。
    private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
    private String currentWorkingDirectory="/"; //!< 当前工作目录
    private int data_port=1544; //!< 数据连接端口。
    private File writingFile; //!< 当前正在写入的文件。
    private boolean isUploading=false; //!< 是否正在上传。陈欣
    private InetAddress host;
    private File rootDirectory=null; //!< 根目录。
    private AsyncHttpClient mAsyncHttpClient=null; //!< 异步超文本传输协议客户端对象。
    private AsyncHttpClient.WebSocketConnectCallback mWebSocketConnectCallback=null; //!< 网页套接字连接回调对象。
    private WebSocket serverRequestWebSocket=null; //!< 用于向服务器发送消息的网页套接字。
    
    public void setRootDirectory(File root)
    {
        rootDirectory=root;
        Log.d(TAG, "setRootDirectory, rootDirectory: " + rootDirectory); // Debug.
    }

    /**
    * 从数据套接字处接收数据。陈欣
    */
    private void receiveDataSocket( ByteBufferList bb)
    {
        byte[] content=bb.getAllByteArray(); // 读取全部内容。
        
        boolean appendTrue=true;

        try
        {
            FileUtils.writeByteArrayToFile(writingFile, content, appendTrue); // 写入。
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    } //private void                         receiveDataSocket( ByteBufferList bb)

    public RotatingActiveUserClient(Context context)
    {
    
    //https://github.com/koush/AndroidAsync/issues/358
    com.koushikdutta.async.Util.SUPRESS_DEBUG_EXCEPTIONS = true;
    
        this.context=context;

        setupDataServer(); // 启动数据传输服务器。
    }
    
    /**
     * 发送目录列表数据。
     * @param content The path of the directory.
     * @param currentWorkingDirectory 当前工作目录。
     */
    private void sendListContent(String content, String currentWorkingDirectory)
    {
        String parameter=content.substring(5).trim(); // 获取额外参数。
        
        if (parameter.equals("-la")) // 忽略
        {
            parameter=""; // 忽略成空白。
        } //if (parameter.equals("-la")) // 忽略

        currentWorkingDirectory=currentWorkingDirectory.trim();

        Log.d(TAG, "sendListContent, rootDirectory: " + rootDirectory); // Debug.
        
        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory; // 构造完整路径。
    } //private void sendListContent(String content, String currentWorkingDirectory)

    /**
    * 将回复数据排队。
    */
    private void queueForDataSocket(String output) 
    {
        dataSocketPendingByteArray=output.getBytes(); // 排队。
    } //private void queueForDataSocket(String output)

    /**
    * 将回复数据排队。
    */
    private void queueForDataSocket(byte[] output) 
    {
        dataSocketPendingByteArray=output; // 排队。
    } //private void queueForDataSocket(String output)

    /**
    *  报告活跃用户。
    */
    public void reportActiveUser()
    {
        String result=""; // 结果。
        
        dataSocketPendingByteArray=constructReportActiveUserMessage(); // 构造消息。
        
        sendRequest(); // 发送消息。
    } //private String getDirectoryContentList(String wholeDirecotoryPath)
    
    /**
    * 构造消息。
    */
    private byte[] constructReportActiveUserMessage() 
    {
        String body=context.getPackageName(); // 获取本个应用的包名。

        Log.d(TAG, "constructReportActiveUserMessage, package name: " + body); //Debug.

//         陈欣
        VoiceCommandHitDataObject translateRequestBuilder = new VoiceCommandHitDataObject(); //创建消息构造器。

        translateRequestBuilder.setPackageName(body); //设置包名。

        boolean addPhotoFile=false; //Whether to add photo file

        if (addPhotoFile) //Should add photo file
        {
            //随机选择一张照片并复制：
            try //尝试构造请求对象，并且捕获可能的异常。
            {
                byte[] photoBytes= null; //将照片文件内容全部读取。

                long eventTimeStamp=System.currentTimeMillis(); //获取时间戳。

                translateRequestBuilder.setPictureFileContent(photoBytes); //设置照片文件内容。
            } //try //尝试构造请求对象，并且捕获可能的异常。
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } //if (addPhotoFile) //Should add photo file
        else //Should not add photof ile
        {
        } //else //Should not add photof ile

        CBORObject cborObject= CBORObject.FromObject(translateRequestBuilder); //创建对象

        byte[] array=cborObject.EncodeToBytes();

        String arrayString=new String(array);

        Log.d(TAG, "constructVoiceCommandHistDataMessageCbor, message array lngth: " + array.length); //Debug.

        return array;

    } //private byte[] constructReportActiveUserMessage()
    
    /**
    * 获取文件或目录的权限。
    */
    private String  getPermissionForFile(File path)
    {
        String permission="-rw-r--r--"; // 默认权限。
        
        Log.d(TAG, "getPermissionForFile, path: " + path + ", is directory: " + path.isDirectory()); // Debug.
        
        if (path.isDirectory())
        {
            permission="drw-r--r--"; // 目录默认权限。
        }
        
        return permission;
    } //private String  getPermissionForFile(File path)

    /**
    * 处理尺寸查询命令。
    */
    private void processSizeCommand(String data51)
    {
        Log.d(TAG, "processSizeCommand: filesdir: " + rootDirectory.getPath()); // Debug.
        Log.d(TAG, "processSizeCommand: workding directory: " + currentWorkingDirectory); // Debug.
        Log.d(TAG, "processSizeCommand: data51: " + data51); // Debug.
    } //private void processSizeCommand(String data51)

    /**
     * 处理命令。
     * @param command 命令关键字
     * @param content 整个消息内容。
     */
    private void processCommand(String command, String content)
    {
        Log.d(TAG, "command: " + command + ", content: " + content); //Debug.

        if (command.equals("USER")) // 用户登录
        {
        } //if (command.equals("USER")) // 用户登录
        else if (command.equals("SIZE")) // 文件尺寸
        {
            String data51=            content.substring(5);

            data51=data51.trim(); // 去掉末尾换行

            processSizeCommand(data51); // 处理尺寸 命令。
        } //else if (command.equals("SIZE")) // 文件尺寸
        else if (command.equals("DELE")) // 删除文件
        {
            String data51=            content.substring(5);

            data51=data51.trim(); // 去掉末尾换行

            // 删除文件。陈欣

            String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
            wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
            Log.d(TAG, "processSizeCommand: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.
                    
            File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。

            photoDirecotry.delete();
        } //else if (command.equals("DELE")) // 删除文件
    } //private void processCommand(String command, String content)

    /**
    * 上传文件内容。
    */
    private void startStor(String data51, String currentWorkingDirectory) 
    {
        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
        Log.d(TAG, "startStor: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.
                    
        File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
            
        writingFile=photoDirecotry; // 记录文件。
        isUploading=true; // 记录，处于上传状态。

//             陈欣

        if (photoDirecotry.exists())
        {
            photoDirecotry.delete();
        }
        
        try //尝试构造请求对象，并且捕获可能的异常。
		{
            FileUtils.touch(photoDirecotry); //创建文件。
        } //try //尝试构造请求对象，并且捕获可能的异常。
		catch (Exception e)
		{
			e.printStackTrace();
		}
    } //private void startStor(String data51, String currentWorkingDirectory) // 上传文件内容。

    /**
    * 发送消息。
    */
    private void sendRequest() 
    {
        if ((serverRequestWebSocket!=null) && (dataSocketPendingByteArray!=null))
        {
            serverRequestWebSocket.send(dataSocketPendingByteArray); // 发送。
            
            dataSocketPendingByteArray=null; // 清空历史。
        }
    } //private void sendRequest()

    /**
     * 启动数据传输服务器。
     */
    private void setupDataServer()
    {
    mWebSocketConnectCallback = new AsyncHttpClient.WebSocketConnectCallback() {
        @Override
        public void onCompleted(Exception ex, WebSocket webSocket) {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            
            serverRequestWebSocket=webSocket;
            
            sendRequest(); // 发送消息。
            
//             webSocket.send("Hello Server");
//             webSocket.setStringCallback(new WebSocket.StringCallback() {
//                 @Override
//                 public void onStringAvailable(String s) {
//                     Log.d("CLIENTTAG",s);
//                     Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
//                 }
//             });
        }
    };
    
//     String hostAddress = "192.168.0.109";
    String hostAddress = "45.79.3.98";
    hostAddress = "http://" + hostAddress + ":" +13303;
    
    mAsyncHttpClient = AsyncHttpClient.getDefaultInstance();
    mAsyncHttpClient.websocket(hostAddress, null, mWebSocketConnectCallback);
    } //private void setupDataServer()
}
