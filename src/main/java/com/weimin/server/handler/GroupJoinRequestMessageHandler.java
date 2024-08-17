package com.weimin.server.handler;

import com.weimin.message.GroupJoinRequestMessage;
import com.weimin.message.GroupJoinResponseMessage;
import com.weimin.server.session.GroupSession;
import com.weimin.server.session.GroupSessionFactory;
import com.weimin.server.session.Session;
import com.weimin.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

@ChannelHandler.Sharable
public class GroupJoinRequestMessageHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        String username = msg.getUsername();

        GroupSession groupSession = GroupSessionFactory.getGroupSession();

        if (groupSession.groupExist(groupName)) {
            groupSession.joinMember(groupName, username);


            Set<String> members = groupSession.getMembers(groupName);
            for (String member : members) {
                if (!member.equals(username)) {
                    Session session = SessionFactory.getSession();
                    Channel channel = session.getChannel(member);
                    channel.writeAndFlush(new GroupJoinResponseMessage(true, "用户【" + username + "】加入群聊【" + groupName + "】"));
                }
            }
            ctx.writeAndFlush(new GroupJoinResponseMessage(true, "成功加入群聊【" + groupName + "】"));
        } else {
            ctx.writeAndFlush(new GroupJoinResponseMessage(false, "群聊【" + groupName + "】不存在"));
        }
    }
}
