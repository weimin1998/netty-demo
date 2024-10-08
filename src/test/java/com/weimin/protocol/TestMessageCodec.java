package com.weimin.protocol;

import com.weimin.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Assert;
import org.junit.Test;

public class TestMessageCodec {

    @Test
    public void testEncode() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                messageCodec);

        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan", "123");

        // 测试出站，将要发送的message对象转为byteBuf
        channel.writeOutbound(loginRequestMessage);
    }

    @Test
    public void testDecode() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                messageCodec);

        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan", "123");

        // 测试入站，将bytebuf转为message类型
        // 这里，bytebuf通过MessageCodec().encode生成一个

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();

        new MessageCodec().encode(null, loginRequestMessage, buffer);

        channel.writeInbound(buffer);
    }

    // 半包问题
    @Test
    public void testBanbaoIssue() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                messageCodec);

        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan", "123");

        // 测试入站，将bytebuf转为message类型
        // 这里，bytebuf通过MessageCodec().encode生成一个

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();

        new MessageCodec().encode(null, loginRequestMessage, buffer);

        // 把byffer分成两份分别入站，模拟客户端发送两个，半包
        ByteBuf b1 = buffer.slice(0, 100);
        ByteBuf b2 = buffer.slice(100, buffer.readableBytes() - 100);

        Assert.assertThrows(DecoderException.class, () -> channel.writeInbound(b1));

    }

    // 解决半包
    @Test
    public void testBanbao() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                // 解决粘包半包
                // 只有接收到完整消息时，才会把数据传递给下一个handler
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                messageCodec);

        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan", "123");

        // 测试入站，将bytebuf转为message类型
        // 这里，bytebuf通过MessageCodec().encode生成一个

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();

        new MessageCodec().encode(null, loginRequestMessage, buffer);

        // 把byffer分成两份分别入站，模拟客户端发送两个，半包
        ByteBuf b1 = buffer.slice(0, 100);
        ByteBuf b2 = buffer.slice(100, buffer.readableBytes() - 100);

        // channel.writeInbound 会让b1 release()，这样buffer，b1，b2就都不能用了
        // 因此提前retain()，引用计数+1
        b1.retain();
        channel.writeInbound(b1);
        channel.writeInbound(b2);

        // 解释一下这几个handler
        // 1.首先我们把buffer 拆成两份buffer发送。所以 LoggingHandler 分别打印了两个 小buffer
        // 2.定长解码器，先收到第一半buffer，他知道前16个字节是固定长度，其中前12个字节不关心，后4字节是消息的长度，比如本例子中是216
        //   于是尝试获取216字节的数据，但是第一半buffer并没有216字节的数据，只有100字节的数据，于是把前一半buffer先存起来，先不传递给下一个handler
        // 3.收到 后一半buffer后，终于读到了完整的数据，于是把完整的byteBuf数据传递给下一个handler
        // 4.MessageCodec 收到数据后，把byteBuf 解码为message类型

        // 所以 LoggingHandler，MessageCodec 可以被多个channel共享使用，也就是只需要创建一个实例
        // 而 LengthFieldBasedFrameDecoder 定长解码器，由于要保存半包数据，所以不能被多个channel共享使用，每个channel单独创建实例



        /**
         * netty提供的handler，是否可以被多个channel共享，通过@Shareable注解标识
         * 前面的 LoggingHandler 就是@Shareable的，LengthFieldBasedFrameDecoder就没有标注
         * 那么我们自定义的编解码器 MessageCodec 是否可以被shareable呢？
         * 答案是可以的。因为它并不保存任何数据，只是单纯的编解码。
         *
         * 但是！ 我们尝试加上@Shareable注解，在执行测试方法，发现报错了：
         *
         * java.lang.IllegalStateException: ChannelHandler com.weimin.protocol.MessageCodec is not allowed to be shared
         *
         * 	at io.netty.channel.ChannelHandlerAdapter.ensureNotSharable(ChannelHandlerAdapter.java:37)
         * 	at io.netty.handler.codec.ByteToMessageCodec.<init>(ByteToMessageCodec.java:73)
         * 	at io.netty.handler.codec.ByteToMessageCodec.<init>(ByteToMessageCodec.java:55)
         * 	at com.weimin.protocol.MessageCodec.<init>(MessageCodec.java:20)
         * 	at com.weimin.protocol.TestMessageCodec.testEncode(TestMessageCodec.java:17)
         *
         * 	而且是构造器报的错，但是我们并没有写构造器，所以问题出在父类的构造器，也就是 ByteToMessageCodec ,
         * 	这个类的构造器上明明白白的写道：
         * 	A Codec for on-the-fly encoding/decoding of bytes to messages and vise-versa.
         * 	This can be thought of as a combination of ByteToMessageDecoder and MessageToByteEncoder.
         * 	Be aware that sub-classes of ByteToMessageCodec MUST NOT annotated with @Sharable.
         *
         * 	也就是该类的子类不能被共享，所以在该类的构造方法中加入了判断，如果子类上标注的@Shareable，则抛异常。
         *
         * 	但是为啥呀，我这个编解码器明明是可以被多个channel共享的呀？
         *
         * 	作者在设计 ByteToMessageCodec 这个类的时候，因为这个类是Byte转其他类型，而Byte可能不是完整的，可能会发生半包现象，需要保存byte，最终拼凑成完整的消息
         * 	所以做了这样的限制。
         *
         * 那怎么办呢？既然父类有这样的限制，那就换个父类。
         *
         * 参考：MessageCodecSharable
         *
         */
    }

}
