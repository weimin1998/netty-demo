package com.weimin.protocol;

import com.weimin.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;

public class TestMessageCodec {

    @Test
    public void test() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                // 解决粘包半包
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                messageCodec);

        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan", "123");
        //channel.writeOutbound(loginRequestMessage);


        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();

        new MessageCodec().encode(null, loginRequestMessage, buffer);

        ByteBuf b1 = buffer.slice(0, 100);
        ByteBuf b2 = buffer.slice(100, buffer.readableBytes()-100);


        // channel.writeInbound(buffer);
        b1.retain();
        channel.writeInbound(b1);
        channel.writeInbound(b2);

    }
}
