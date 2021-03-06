/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.proxy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.shardingjdbc.proxy.constant.StatusFlag;
import io.shardingjdbc.proxy.packet.AbstractMySQLSentPacket;
import io.shardingjdbc.proxy.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.packet.command.AbstractCommandPacket;
import io.shardingjdbc.proxy.packet.command.CommandPacketFactory;
import io.shardingjdbc.proxy.packet.handshake.AuthPluginData;
import io.shardingjdbc.proxy.packet.handshake.ConnectionIdGenerator;
import io.shardingjdbc.proxy.packet.handshake.HandshakePacket;
import io.shardingjdbc.proxy.packet.handshake.HandshakeResponse41Packet;
import io.shardingjdbc.proxy.packet.ok.OKPacket;

/**
 * Server handler.
 * 
 * @author zhangliang 
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    
    private AuthPluginData authPluginData;
    
    private boolean authorized;
    
    @Override
    public void channelActive(final ChannelHandlerContext context) throws Exception {
        authPluginData = new AuthPluginData();
        context.writeAndFlush(new HandshakePacket(ConnectionIdGenerator.getInstance().nextId(), authPluginData));
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) throws Exception {
        MySQLPacketPayload mysqlPacketPayload = new MySQLPacketPayload((ByteBuf) message);
        if (!authorized) {
            auth(context, mysqlPacketPayload);
        } else {
            executeCommand(context, mysqlPacketPayload);
        }
    }
    
    private void auth(final ChannelHandlerContext context, final MySQLPacketPayload mysqlPacketPayload) {
        HandshakeResponse41Packet response41 = new HandshakeResponse41Packet().read(mysqlPacketPayload);
        // TODO use authPluginData to auth
        authorized = true;
        context.writeAndFlush(new OKPacket(response41.getSequenceId() + 1, 0L, 0L, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
    
    private void executeCommand(final ChannelHandlerContext context, final MySQLPacketPayload mysqlPacketPayload) {
        int sequenceId = mysqlPacketPayload.readInt1();
        AbstractCommandPacket commandPacket = CommandPacketFactory.getCommandPacket(mysqlPacketPayload.readInt1());
        commandPacket.setSequenceId(sequenceId);
        commandPacket.read(mysqlPacketPayload);
        for (AbstractMySQLSentPacket each : commandPacket.execute()) {
            context.write(each);
        }
        context.flush();
    }
}
