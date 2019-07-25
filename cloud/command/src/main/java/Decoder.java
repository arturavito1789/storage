import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.util.AttributeKey;


//Из декодера отправлять на прямую сообщения клиенту или серверу нельзя .
public class Decoder extends ObjectDecoder {
    //Эта переменная свидетельствует о передачи файла. Когда идет передача файла = true иначе false
    AttributeKey <Boolean> fileTransfer = AttributeKey.valueOf("fileTransfer");

    public Decoder(ClassResolver classResolver) {
        super(classResolver);
     }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if(ctx.channel().attr(fileTransfer).get().booleanValue() == true)  {
           //идет передача файлов отсылаем чистые байты иначе отсылаем команду
            return in.readBytes(in.readableBytes());
        }
        return super.decode(ctx, in);
    }



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //устанавливаем для каждого канала с пользователем, в дальнейшем
        //устанавливается в следующем обработчике
        ctx.channel().attr(fileTransfer).set(false);
    }
}
