package fayvoting.model;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.ArrayDeque;
import java.util.Deque;

public class Blockchain {
    enum Mode {
        DISABLE,
        FULL_SYNC,
        NEW_BLOCK,
        READY_TO_SERVICE
    }
    private final int port = 20001;
    private Deque<Block> blocks = new ArrayDeque<>();
    private Mode mode = Mode.DISABLE;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private EventLoopGroup group = new NioEventLoopGroup();

    public Blockchain() {
    }

    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new BlockchainServerHandler());
                    }
                });

        ChannelFuture f = b.bind(port).sync();
        System.out.println("Server started on port: " + port);
        f.channel().closeFuture().addListener(future -> {
            group.shutdownGracefully();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            if (future.isSuccess()) {
                System.out.println("Channel closed successfully");
            } else {
                System.err.println("Channel close failed");
            }
        });
    }

    public void connect(String host, int port) {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new BlockchainClientHandler());
                    }
                });

        ChannelFuture f = b.connect(host, port);
        f.channel().writeAndFlush("GET_CHAIN");
        f.channel().closeFuture().addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("Channel closed successfully");
            } else {
                System.err.println("Channel close failed");
            }
        });

    }

    public static Blockchain init() {
        Blockchain blockchain = new Blockchain();
        try {
            blockchain.start();
            // blockchain.connect("localhost", 20002);
            // blockchain.connect("localhost", 20003);
            // blockchain.connect("localhost", 20004);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return blockchain;
    }
}

class BlockchainClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("Response from server: " + msg);

        if (msg.equals("FULL_SYNC")) {
            // Send the entire blockchain
        }
        else if (msg.startsWith("NEW_BLOCK")) {
            // Receive and validate a new block
        }
        else {
            ctx.writeAndFlush("UNKNOWN_COMMAND");
        }
    }
}

class BlockchainServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("Received: " + msg);

        if (msg.equals("FULL_SYNC")) {
            // Send the entire blockchain
        }
        else if (msg.startsWith("NEW_BLOCK")) {
            // Receive and validate a new block
        }
        else {
            ctx.writeAndFlush("UNKNOWN_COMMAND");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // System.out.println("Connection established: " + ctx.channel().remoteAddress());
        // ctx.writeAndFlush("GET_LAST_BLOCK");  // Request the latest block on connection
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
