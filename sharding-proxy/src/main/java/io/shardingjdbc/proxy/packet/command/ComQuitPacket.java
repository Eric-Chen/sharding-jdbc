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

package io.shardingjdbc.proxy.packet.command;

import io.shardingjdbc.proxy.constant.StatusFlag;
import io.shardingjdbc.proxy.packet.AbstractMySQLSentPacket;
import io.shardingjdbc.proxy.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.packet.ok.OKPacket;

import java.util.Collections;
import java.util.List;

/**
 * COM_QUIT command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-quit.html">COM_QUIT</a>
 *
 * @author zhangliang
 */
public final class ComQuitPacket extends AbstractCommandPacket {
    
    @Override
    public ComQuitPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        return this;
    }
    
    @Override
    public List<AbstractMySQLSentPacket> execute() {
        return Collections.<AbstractMySQLSentPacket>singletonList(new OKPacket(getSequenceId() + 1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
}
