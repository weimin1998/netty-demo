package com.weimin.server.handler;

import com.weimin.message.GroupQuitRequestMessage;
import com.weimin.message.GroupQuitResponseMessage;
import com.weimin.server.session.Group;
import com.weimin.server.session.GroupSession;
import com.weimin.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupQuitRequestMessageHandler extends SimpleChannelInboundHandler<GroupQuitRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupQuitRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String groupName = msg.getGroupName();

        GroupSession groupSession = GroupSessionFactory.getGroupSession();

        Group group = groupSession.removeMember(groupName, username);
        if (group == null) {
            ctx.writeAndFlush(new GroupQuitResponseMessage(false, "群聊【" + groupName + "】不存在"));
        } else {
            List<Channel> membersChannel = groupSession.getMembersChannel(groupName);
            for (Channel channel : membersChannel) {
                channel.writeAndFlush(new GroupQuitResponseMessage(true, "用户【" + username + "】退出群聊【" + groupName + "】"));
            }
            ctx.writeAndFlush(new GroupQuitResponseMessage(true, "成功退出群聊【" + groupName + "】"));
        }
    }
}
