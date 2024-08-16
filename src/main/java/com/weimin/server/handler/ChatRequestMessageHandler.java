package com.weimin.server.handler;

import com.weimin.message.ChatRequestMessage;
import com.weimin.message.ChatResponseMessage;
import com.weimin.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


// 继承 SimpleChannelInboundHandler ，表示这个handler只关心某一种类型的消息，通过泛型指定
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        String to = msg.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to);
        if (channel != null) {
            String from = msg.getFrom();
            String content = msg.getContent();

            channel.writeAndFlush(new ChatResponseMessage(from, content));
        } else {
            // 对方不在线
            ctx.channel().writeAndFlush(new ChatResponseMessage(false, "对方不在线！"));
        }

    }
}
