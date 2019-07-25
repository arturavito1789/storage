import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.scene.text.Text;

public class Server {

    private int port;
    private boolean isRun;
    Thread thread = null;
    private Text captionServer;
    int countUsersConnect = 0;

    public Server(int port, Text captionServer) {
        this.port = port;
        this.captionServer = captionServer;
    }

    public boolean isRun() {
        return isRun;
    }

     public void run()  {
         thread = new Thread(){
             public void run(){
                 EventLoopGroup bossGroup = new NioEventLoopGroup();
                 EventLoopGroup workerGroup = new NioEventLoopGroup();
                 try {
                     ServerBootstrap b = new ServerBootstrap();
                     b.group(bossGroup, workerGroup)
                             .channel(NioServerSocketChannel.class)
                             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                                 @Override
                                 public void initChannel(SocketChannel ch) throws Exception {
                                     ch.pipeline().addLast(new ObjectEncoder(), new Decoder(ClassResolvers.cacheDisabled(null)), new Handler(Server.this));
                                 }
                             })
                             .childOption(ChannelOption.SO_KEEPALIVE, true);
                     //важно new HandlerServer(Server.this) - при каждом подключении обработчик создается заново
                     //если бы так handlerServer = new HandlerServer(Server.this) и передача ссылки на handlerServer
                     // это был бы общий обработчик на все подключения
                     //не смотря на отсутствие аннотации @Sharable
                     ChannelFuture f = b.bind(port).sync();
                     isRun = true;
                     captionServer.setText("Сервер запушен подключено клиентов: 0");
                     f.channel().closeFuture().sync();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                     isRun = false;
                     captionServer.setText("Не удалось запустить сервер");
                 }
                 finally {
                     isRun = false;
                     workerGroup.shutdownGracefully();
                     bossGroup.shutdownGracefully();
                     captionServer.setText("Сервер остановлен");
                 }
             }


         };

         thread.start();
     }

    public void stop(){
        thread.interrupt();
    }

    public void increment(){
        countUsersConnect ++;
        captionServer.setText("Сервер запушен подключено клиентов: " + countUsersConnect);
    }

    public void dicrement(){
        countUsersConnect --;
        captionServer.setText("Сервер запушен подключено клиентов: " + countUsersConnect);
    }




}
