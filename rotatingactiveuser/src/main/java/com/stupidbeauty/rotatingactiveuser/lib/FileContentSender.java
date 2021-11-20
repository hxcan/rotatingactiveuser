package com.stupidbeauty.ftpserver.lib;

import com.koushikdutta.async.AsyncSocket;
import java.net.InetSocketAddress;
import com.koushikdutta.async.callback.ConnectCallback;
import android.app.Application;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import org.apache.commons.io.FileUtils;
import com.koushikdutta.async.callback.ConnectCallback;

public class FileContentSender
{
    private byte[] dataSocketPendingByteArray=null; //!< 数据套接字数据内容 排队。
    private ControlConnectHandler controlConnectHandler=null; //!< 控制连接处理器。
    private AsyncSocket data_socket=null; //!< 当前的数据连接。
    private File rootDirectory=null; //!< 根目录。
    private File fileToSend=null; //!< 要发送的文件。
    
    /**
    * 设置根目录。
    */
    public void setRootDirectory(File rootDirectory)
    {
        this.rootDirectory=rootDirectory;
    } //public void  setRootDirectory(File rootDirectory)
    
    public void setControlConnectHandler(ControlConnectHandler controlConnectHandler) // 设置控制连接处理器。
    {
        this.controlConnectHandler=controlConnectHandler;
    } //public void setControlConnectHandler(ControlConnectHandler controlConnectHandler)
    
    /**
    * 设置数据连接套接字。
    */
    public void setDataSocket(AsyncSocket socket) 
    {
        data_socket=socket; // 记录。
        
        if ((fileToSend!=null) && (data_socket!=null)) // 有等待发送的内容。
        {
            startSendFileContentForLarge(); // 开始发送文件内容。
        } // if (dataSocketPendingByteArray!=null)
    } //public void setDataSocket(AsyncSocket socket)
    
    private void startSendFileContentForLarge()
    {
      if (fileToSend.exists()) // 文件存在
      {
        Util.pump(fileToSend, data_socket, new CompletedCallback()
        {
          @Override
          public void onCompleted(Exception ex)
          {
            if (ex != null) throw new RuntimeException(ex);
            System.out.println("[Server] data Successfully wrote message");
                    
            notifyFileSendCompleted(); // 告知已经发送文件内容数据。
            fileToSend=null; // 将要发送的文件对象清空。
          }
        });
      } //if (fileToSend.exist()) // 文件存在
      else
      {
        notifyFileNotExist(); // 报告文件不存在。
      }
    } //private void startSendFileContentForLarge()
    
    /**
    * 开始发送文件内容。
    */
    private void startSendFileContent() 
    {
        byte[] photoBytes=null; //数据内容。

        try //尝试构造请求对象，并且捕获可能的异常。
        {
            photoBytes= FileUtils.readFileToByteArray(fileToSend); //将照片文件内容全部读取。
        } //try //尝试构造请求对象，并且捕获可能的异常。
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (photoBytes!=null) // 读取的文件存在
		{
            Util.writeAll(data_socket, photoBytes, new CompletedCallback() 
            {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] data Successfully wrote message");
                    
                    notifyFileSendCompleted(); // 告知已经发送文件内容数据。
                    fileToSend=null; // 将要发送的文件对象清空。
                }
            });
		} //if (photoBytes!=null) // 读取的文件存在
		else // 读取的文件不存在
		{
      notifyFileNotExist(); // 告知文件不存在
		} //else // 读取的文件不存在
    } //private void startSendFileContent()

    /**
    * 发送文件内容。
    */
    public void sendFileContent(String data51, String currentWorkingDirectory) 
    {
        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
//        File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
        FilePathInterpreter filePathInterpreter=new FilePathInterpreter(); // Create the file path interpreter.
        File photoDirecotry= filePathInterpreter.getFile(rootDirectory, currentWorkingDirectory, data51); //照片目录。

        fileToSend=photoDirecotry; // 记录，要发送的文件对象。
        
        if (data_socket!=null) // 数据连接存在。
        {
            startSendFileContentForLarge(); // 开始发送文件内容。
        } //if (data_socket!=null) // 数据连接存在。
    } //private void sendFileContent(String data51, String currentWorkingDirectory)
    
    /**
    * 告知已经发送文件内容数据。
    */
    private void notifyFileSendCompleted() 
    {
        controlConnectHandler.notifyFileSendCompleted(); // 告知文件内容发送完毕。
    } //private void notifyFileSendCompleted()
    
    private void notifyFileNotExist() // 告知文件不存在
    {
        controlConnectHandler.notifyFileNotExist(); // 告知文件不存在。
    } //private void notifyFileNotExist()

    /**
    * 将回复数据排队。
    */
    private void queueForDataSocket(byte[] output) 
    {
        dataSocketPendingByteArray=output; // 排队。
    } //private void queueForDataSocket(String output)

    /**
    * 将回复数据排队。
    */
    private void queueForDataSocket(String output) 
    {
        dataSocketPendingByteArray=output.getBytes(); // 排队。
    } //private void queueForDataSocket(String output)
}
