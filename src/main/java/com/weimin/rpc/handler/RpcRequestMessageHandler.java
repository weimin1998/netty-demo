package com.weimin.rpc.handler;

import com.weimin.config.AppConfig;
import com.weimin.message.RpcRequestMessage;
import com.weimin.message.RpcResponseMessage;
import com.weimin.server.service.HelloService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(msg.getSequenceId());
        try {
            // 获取真正的实现对象
            HelloService service = (HelloService)
                    AppConfig.getService(Class.forName(msg.getInterfaceName()));

            // 获取要调用的方法
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());

            // 调用方法
            Object invoke = method.invoke(service, msg.getParameterValue());
            // 调用成功
            response.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            // 调用异常
            response.setExceptionValue(e);
        }
        // 返回结果
        ChannelFuture future = ctx.writeAndFlush(response);

        future.addListener(promise ->{
            if (!promise.isSuccess()) {
                Throwable cause = promise.cause();
                cause.printStackTrace();
                log.error(cause.getMessage());
            }
        });
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RpcRequestMessage msg =
                new RpcRequestMessage(1,
                        "com.weimin.server.service.HelloService",
                        "sayHello",
                        String.class,
                        new Class[]{String.class},
                        new Object[]{"tom"});


        HelloService service = (HelloService) AppConfig.getService(Class.forName(msg.getInterfaceName()));
        // 获取要调用的方法
        Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());

        // 调用方法
        Object invoke = method.invoke(service, msg.getParameterValue());

        System.out.println(invoke);
    }
}
