import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.AttributeKey;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


public class HandlerMessageBase extends ChannelInboundHandlerAdapter {
    public ChannelHandlerContext ctxActiv;
    //Эта переменная свидетельствует о передачи файла. Когда идет передача файла = true иначе false
    public AttributeKey<Boolean> fileTransfer = AttributeKey.valueOf("fileTransfer");

    protected String mainPath;
    protected CommandFileInfo commandFileTransfer = null;//эта переменная свидетельствует что последняя команда была CommandFileInfo
    // которая свидетельствует что следующшим будет идти передача файла. Передача файла может быть с сервера на клиент или наоборот
    protected long countByteFile = 0;
    public List<String> listNameFileTransfer = new ArrayList<String>();

    public void channelActive(ChannelHandlerContext channelHandlerContext){
        this.ctxActiv = channelHandlerContext;
    }


    public boolean sendCommand(Command command){
        try {
            ctxActiv.writeAndFlush(command).sync().await();
        } catch (Exception e) {
           return false;
        }
        return true;
    }

    public boolean sendFile(String nameF){
        File file = new File(mainPath + nameF);
        long length = file.length();
        CommandFileInfo commandFileInfo = new CommandFileInfo(nameF, length);
        sendCommand(commandFileInfo);
        try {
            Thread.sleep(50);
            //делаем задержку перед посылкой файла это нужно так как commandFileInfo попадет в decoder из
            //него будет передана в Handler, Handler - установит атрибут fileTransfer и на основе этого атрибута
            //decoder будет в decode возврашать байты чтобы можно было записать в виде файла иначе в decode
            //будет раскодироваться объект команда. Задержка нужна чтобы Handler - успел установить атрибут fileTransfer
            //до того как начнется передача файла.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //можно все передать через FileRegion то для разнообразия делим на два варианта
        try (FileInputStream f = new FileInputStream(file)){
            if (length > 100000){
                //считаем большой файл
                FileRegion region = new DefaultFileRegion(f.getChannel(), 0, file.length());//получили файл
                ctxActiv.writeAndFlush(region).sync().await();
            }else{
                byte[] data = new byte[(int)length];
                f.read(data);
                ctxActiv.writeAndFlush(Unpooled.copiedBuffer(data)).sync().await();
            }
        }  catch (Exception e) {
           e.printStackTrace();
           return false;
        }
        return true;
    }
}
