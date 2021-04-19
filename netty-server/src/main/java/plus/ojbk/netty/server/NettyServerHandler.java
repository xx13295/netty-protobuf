package plus.ojbk.netty.server;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import plus.ojbk.netty.protobuf.MessageBase;

import java.util.UUID;


@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<MessageBase.Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageBase.Message msg) throws Exception {
        if (msg.getCmd().equals(MessageBase.Message.CommandType.HEARTBEAT_REQUEST)) {
            log.info("收到客户端发来的心跳消息：{}", msg.toString());
            //回应不回应，看具体业务需求
            MessageBase.Message message = new MessageBase.Message()
                    .toBuilder().setCmd(MessageBase.Message.CommandType.HEARTBEAT_RESPONSE)
                    .setContent("Server Heartbeat")
                    .setRequestId(UUID.randomUUID().toString()).build();
            ctx.writeAndFlush(message);
        } else if (msg.getCmd().equals(MessageBase.Message.CommandType.NORMAL)) {
            log.info("收到客户端的业务消息：{}",msg.toString());
        }
    }

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("注册事件");
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新客户端连接接入 remoteAddress = {}", ctx.channel().remoteAddress());
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("失去连接事件");
    }

    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("取消注册事件");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获的异常：{}", cause.getMessage());
        ctx.channel().close();
    }
}
