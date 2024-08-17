package com.weimin.server.handler;

import com.weimin.Logger;
import com.weimin.message.LoginRequestMessage;
import com.weimin.message.LoginResponseMessage;
import com.weimin.server.service.UserServiceFactory;
import com.weimin.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    private static final Logger logger = new Logger(LoginRequestMessageHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();

        logger.debug(username);
        logger.debug(password);

        LoginResponseMessage message;
        boolean login = UserServiceFactory.getUserService().login(username, password);
        if (login) {
            // 将用户名和channel关联起来，服务器就可以根据用户名找到channel
            SessionFactory.getSession().bind(ctx.channel(), username);
            message = new LoginResponseMessage(true, "登录成功");
        } else {
            message = new LoginResponseMessage(false, "用户名或密码错误");
        }

        ctx.writeAndFlush(message);
    }
}
