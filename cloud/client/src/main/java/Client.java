import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Client {

    Handler handler = null;
    Thread thread = null;
    public CountDownLatch operationServer =  null;
    private EventLoopGroup group;

    private boolean isRun;


    public Client(FXMLMainController fxmlClientController) {
        this.handler = new Handler(fxmlClientController::setTextCaptionConnect,
                fxmlClientController::showStageWork,
                fxmlClientController::addItemlistServer,
                fxmlClientController::deleteItemlistServer,
                fxmlClientController::addItemlistClient,
                fxmlClientController::clearlistClient,
                fxmlClientController::clearlistServer);
        this.operationServer =  new CountDownLatch(1);
    }

    public EventLoopGroup getGroup() {
        return group;
    }

    public boolean isRun() {
        return isRun;
    }

    public void run()  {

        thread = new Thread(){
            public void run(){
                group = new NioEventLoopGroup();
                try{
                    Bootstrap clientBootstrap = new Bootstrap();
                    clientBootstrap.group(group);
                    clientBootstrap.channel(NioSocketChannel.class);
                    clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 8189));
                    clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ObjectEncoder(),new Decoder(ClassResolvers.cacheDisabled(null)), handler);
                        }
                    });
                    ChannelFuture channelFuture = clientBootstrap.connect().sync();
                    isRun = true;
                    operationServer.countDown();
                    channelFuture.channel().closeFuture().sync();
                } catch (Exception e) {
                    isRun = false;
                    operationServer.countDown();
                    e.printStackTrace();
                } finally {
                    try {
                        isRun = false;
                        group.shutdownGracefully().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }


}
