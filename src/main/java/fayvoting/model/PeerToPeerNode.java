package fayvoting.model;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class PeerToPeerNode {
    private Blockchain blockchain;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public PeerToPeerNode(int port, Blockchain blockchain) {
        this.blockchain = blockchain;
        startServer(port);
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    private void startServer(int port) {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new ObjectEncoder(),
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                new PeerHandler(blockchain)
                        );
                    }
                });
        b.bind(port);
    }

    public void connectToPeer(String address, int port) {
        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new ObjectEncoder(),
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                new PeerHandler(blockchain)
                        );
                    }
                });
        try {
            b.connect(address, port).sync();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // System.out.println("Disconnected from " + address + ":" + port + ". Trying to reconnect in 5 seconds...");
        LockSupport.parkNanos("waiting 5 seconds", TimeUnit.SECONDS.toNanos(5L));
    }
}