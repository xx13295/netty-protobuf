package plus.ojbk.netty.client;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import plus.ojbk.netty.protobuf.MessageBase;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

  /*  private static NettyClient nettyClient;
    private static NettyClient getNettyClient() {
        if (nettyClient == null) {
            synchronized (HeartbeatHandler.class) {
                if (nettyClient == null) {
                    nettyClient = (NettyClient) SpringContextJob.getBean("nettyClient");
                }
            }

        }
        return nettyClient;
    }*/

    private NettyClient nettyClient;
    public HeartbeatHandler(NettyClient nettyClient){
        this.nettyClient = nettyClient;
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                //WRITER_IDLE一段时间内没有数据发送 ClientHandlerInitializer 设置了 writerIdleTimeSeconds 20秒
                log.info("开始向服务端送心跳包");
                MessageBase.Message heartbeat = new MessageBase.Message().toBuilder().setCmd(MessageBase.Message.CommandType.HEARTBEAT_REQUEST)
                        .setRequestId(UUID.randomUUID().toString())
                        .setContent("Client Heartbeat").build();
                //发送心跳消息，并在发送失败时关闭该连接
                ctx.writeAndFlush(heartbeat).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //如果运行过程中服务端挂了,执行重连机制
        EventLoop eventLoop = ctx.channel().eventLoop();
        /*eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
                System.err.println("开始断线重连");
                getNettyClient().start();
            }
        }, 10L, TimeUnit.SECONDS);*/
        eventLoop.schedule(() -> nettyClient.start(), 10L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获的异常：{}", cause.getMessage());
        ctx.channel().close();
    }
}
