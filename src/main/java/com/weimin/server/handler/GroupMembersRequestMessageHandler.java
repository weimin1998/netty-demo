package com.weimin.server.handler;

import com.weimin.message.GroupMembersRequestMessage;
import com.weimin.message.GroupMembersResponseMessage;
import com.weimin.server.session.GroupSession;
import com.weimin.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;
@ChannelHandler.Sharable
public class GroupMembersRequestMessageHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        if (groupSession.groupExist(groupName)) {
            Set<String> members = groupSession.getMembers(groupName);
            ctx.channel().writeAndFlush(new GroupMembersResponseMessage(members));
        } else {
            ctx.channel().writeAndFlush(new GroupMembersResponseMessage(false, "群聊【" + groupName + "】不存在"));
        }
    }
}
