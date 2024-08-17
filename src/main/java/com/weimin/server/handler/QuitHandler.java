package com.weimin.server.handler;

import com.weimin.Logger;
import com.weimin.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = new Logger(QuitHandler.class);

    // 连接正常断开时，ServerSocketChannel触发inactive事件
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        String name = SessionFactory.getSession().getName(ctx.channel());
        SessionFactory.getSession().unbind(ctx.channel());
        logger.debug("用户【" + name + "】正常断开连接");

    }

    // 连接异常断开，服务器会收到异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String name = SessionFactory.getSession().getName(ctx.channel());
        SessionFactory.getSession().unbind(ctx.channel());
        logger.debug("用户【" + name + "】异常断开连接");
        logger.debug(cause.getMessage());
    }
}
