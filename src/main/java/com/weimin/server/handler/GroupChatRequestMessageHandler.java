package com.weimin.server.handler;

import com.weimin.message.GroupChatRequestMessage;
import com.weimin.message.GroupChatResponseMessage;
import com.weimin.server.session.GroupSession;
import com.weimin.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        String from = msg.getFrom();
        String groupName = msg.getGroupName();
        String content = msg.getContent();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();

        if (groupSession.groupExist(groupName)) {
            Set<String> members = groupSession.getMembers(groupName);
            if (members.contains(from)) {
                List<Channel> membersChannel = groupSession.getMembersChannel(groupName);
                for (Channel channel : membersChannel) {
                    if (channel != ctx.channel()) {
                        channel.writeAndFlush(new GroupChatResponseMessage("来自群聊【" + groupName + "】的【" + from + "】", content));
                    }
                }
            } else {
                ctx.writeAndFlush(new GroupChatResponseMessage(false, "你不在群聊中，不能发送群聊消息！"));
            }
        } else {
            ctx.writeAndFlush(new GroupChatResponseMessage(false, groupName + "不存在！"));
        }

    }
}
