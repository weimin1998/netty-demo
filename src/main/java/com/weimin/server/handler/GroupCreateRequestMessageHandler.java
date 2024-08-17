package com.weimin.server.handler;

import com.weimin.message.GroupCreateRequestMessage;
import com.weimin.message.GroupCreateResponseMessage;
import com.weimin.server.session.Group;
import com.weimin.server.session.GroupSession;
import com.weimin.server.session.GroupSessionFactory;
import com.weimin.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();

        Set<String> members = msg.getMembers();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();

        Group group = groupSession.createGroup(groupName, members);

        if (group == null) {
            ctx.channel().writeAndFlush(new GroupCreateResponseMessage(true, "群聊【" + groupName + "】创建成功"));
            String groupOwner = SessionFactory.getSession().getName(ctx.channel());
            // 获取在线的成员
            List<Channel> membersChannel = groupSession.getMembersChannel(groupName);
            for (Channel channel : membersChannel) {
                if (channel != ctx.channel()) {
                    channel.writeAndFlush(new GroupCreateResponseMessage(true, groupOwner + "邀请你进入群：" + groupName));
                }
            }
        } else {
            ctx.channel().writeAndFlush(new GroupCreateResponseMessage(false, groupName + "创建失败，群聊已经存在"));
        }
    }
}
