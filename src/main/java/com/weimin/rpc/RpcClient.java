package com.weimin.rpc;

import com.weimin.message.RpcRequestMessage;
import com.weimin.protocol.MessageCodecSharable;
import com.weimin.protocol.ProtocolFrameDecoder;
import com.weimin.rpc.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        // rpc 响应消息处理器，待实现
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();


            // 正常调用
            ChannelFuture future = channel.writeAndFlush(new RpcRequestMessage(1,
                    "com.weimin.server.service.HelloService",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"tom"}));
            future.addListener(promise ->{
                if (!promise.isSuccess()) {
                    Throwable cause = promise.cause();

                    cause.printStackTrace();
                    log.error(cause.getMessage());
                }
            });

            // 异常调用， HelloService1 没这个service
            ChannelFuture future1 = channel.writeAndFlush(new RpcRequestMessage(1,
                    "com.weimin.server.service.HelloService1",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"tom"}));
            future1.addListener(promise ->{
                if (!promise.isSuccess()) {
                    Throwable cause = promise.cause();

                    cause.printStackTrace();
                    log.error(cause.getMessage());
                }
            });

            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}