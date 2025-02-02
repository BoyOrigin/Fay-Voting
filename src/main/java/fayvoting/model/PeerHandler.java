package fayvoting.model;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PeerHandler extends SimpleChannelInboundHandler<Blockchain> {
    private Blockchain blockchain;

    public PeerHandler(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Blockchain receivedChain) {
        blockchain.synchronizeBlockchain(receivedChain.getChain());
        // System.out.println("Blockchain synchronized with peer.");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(blockchain);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
