package com.weimin.protocol;


import com.weimin.Logger;
import com.weimin.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

// byteBuf和泛型类型相互转换
// 本例子 byteBuf和Message类型相互转换
//@ChannelHandler.Sharable
public class MessageCodec extends ByteToMessageCodec<Message> {

     private static final Logger logger = new Logger(MessageCodec.class);

     // ByteBuf out，这个bytebuf会由netty创建好，我们只需要把Message msg 按照自定义的协议规则写入out中即可
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 4字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 1字节的版本
        out.writeByte(1);
        // 1字节序列化算法
        out.writeByte(0);
        // 1字节指令类型
        out.writeByte(msg.getMessageType());
        // 4字节的序号
        out.writeInt(msg.getSequenceId());
        // 1字节的占位符，为了使得固定部分是2的整数倍
        out.writeByte(0xff);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);

        byte[] bytes = bos.toByteArray();
        // 4字节正文的长度
        out.writeInt(bytes.length);
        // 正文的内容
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 魔数
        int magicNum = in.readInt();

        byte version = in.readByte();

        byte serializerType = in.readByte();

        byte messageType = in.readByte();

        int sequenceId = in.readInt();

        in.readByte();

        int length = in.readInt();

        byte[] bytes = new byte[length];

        in.readBytes(bytes, 0, length);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Message message = (Message) ois.readObject();

        logger.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);

        logger.debug("{}", message);

        out.add(message);
    }
}
