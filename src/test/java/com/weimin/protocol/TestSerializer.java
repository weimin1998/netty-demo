package com.weimin.protocol;

import com.weimin.config.AppConfig;
import com.weimin.message.LoginRequestMessage;
import com.weimin.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;

public class TestSerializer {

    @Test
    public void testSerialize() throws Exception {
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        LoggingHandler loggingHandler = new LoggingHandler();
        EmbeddedChannel channel = new EmbeddedChannel(
                loggingHandler,
                messageCodecSharable,
                loggingHandler);

        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan", "123");
        channel.writeOutbound(loginRequestMessage);
    }

    @Test
    public void testDeserialize() throws Exception {
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        LoggingHandler loggingHandler = new LoggingHandler();
        EmbeddedChannel channel = new EmbeddedChannel(
                loggingHandler,
                messageCodecSharable,
                loggingHandler);

        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan", "123");

        ByteBuf byteBuf = msgToByteBuf(loginRequestMessage);

        channel.writeInbound(byteBuf);
    }

    private static ByteBuf msgToByteBuf(Message msg) {
        int algorithm = AppConfig.getSerializerAlgorithm().ordinal();
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        // 4字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 1字节的版本
        out.writeByte(1);
        // 1字节序列化算法
        // ordinal，是枚举对象在枚举类中的顺序，java 0 , json 1
        out.writeByte(algorithm);
        // 1字节指令类型
        out.writeByte(msg.getMessageType());
        // 4字节的序号
        out.writeInt(msg.getSequenceId());
        // 1字节的占位符，为了使得固定部分是2的整数倍
        out.writeByte(0xff);
        // 序列化
        byte[] bytes = Serializer.Algorithm.values()[algorithm].serialize(msg);
        // 4字节正文的长度
        out.writeInt(bytes.length);
        // 正文的内容
        out.writeBytes(bytes);
        return out;
    }
}
