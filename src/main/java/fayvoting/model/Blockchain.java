package fayvoting.model;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class Blockchain {
    public static final Gson GSON = new Gson();

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
    private UUID serverId = UUID.randomUUID();

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

    public static Blockchain init() throws InterruptedException {
        Blockchain blockchain = new Blockchain();
        blockchain.start();
        blockchain.mode = Mode.READY_TO_SERVICE;
        // blockchain.connect("localhost", 20002);
        // blockchain.connect("localhost", 20003);
        // blockchain.connect("localhost", 20004);
        return blockchain;
    }
}

class Packet implements Serializable {
    private final String channel;
    private final UUID serverId;
    private final String data;

    public Packet(String channel, UUID serverId, String data) {
        this.channel = channel;
        this.serverId = serverId;
        this.data = data;
    }

    public String getChannel() {
        return channel;
    }

    public UUID getServerId() {
        return serverId;
    }

    public String getData() {
        return data;
    }
}

class BlockchainClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Packet packet = Blockchain.GSON.fromJson(msg, Packet.class);
        System.out.println("Response from server id: " + packet.getServerId().toString());

        if (packet.getChannel().equals("FULL_SYNC")) {
            // Send the entire blockchain
        }
        else if (packet.getChannel().startsWith("NEW_BLOCK")) {
            // Receive and validate a new block
        }
        else {
            // ctx.writeAndFlush("UNKNOWN_COMMAND");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Connection established: " + ctx.channel().remoteAddress());
        // ctx.writeAndFlush("GET_LAST_BLOCK");  // Request the latest block on connection
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

class BlockchainServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Packet packet = Blockchain.GSON.fromJson(msg, Packet.class);
        System.out.println("Received from server id: " + packet.getServerId());

        if (packet.getChannel().equals("FULL_SYNC")) {
            // Send the entire blockchain
        }
        else if (packet.getChannel().equals("NEW_BLOCK")) {
            // Receive and validate a new block
        }
        else {
            // ctx.writeAndFlush("UNKNOWN_COMMAND");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Connection established: " + ctx.channel().remoteAddress());
        // ctx.writeAndFlush("GET_LAST_BLOCK");  // Request the latest block on connection
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
