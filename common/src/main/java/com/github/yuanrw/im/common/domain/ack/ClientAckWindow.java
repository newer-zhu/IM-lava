package com.github.yuanrw.im.common.domain.ack;

import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * for client, every connection should has an ServerAckWindow
 */
public class ClientAckWindow {
    private static Logger logger = LoggerFactory.getLogger(ClientAckWindow.class);

    private final int maxSize;

    //是否初次开启
    private AtomicBoolean first;
    //窗口里的最后一条消息ID
    private AtomicLong lastId;
    //不连续消息暂存Map
    private ConcurrentMap<Long, ProcessMsgNode> notContinuousMap;

    public ClientAckWindow(int maxSize) {
        this.first = new AtomicBoolean(true);
        this.maxSize = maxSize;
        this.lastId = new AtomicLong(-1);
        this.notContinuousMap = new ConcurrentHashMap<>();
    }

    /**
     * multi thread do it
     *
     * @param id              msg id
     * @param from            from module
     * @param dest            dest module
     * @param receivedMsg
     * @param processFunction
     */
    public CompletableFuture<Void> offer(Long id, Internal.InternalMsg.Module from, Internal.InternalMsg.Module dest,
                                         ChannelHandlerContext ctx, Message receivedMsg, Consumer<Message> processFunction) {
        if (isRepeat(id)) {//重复消息
            ctx.writeAndFlush(getInternalAck(id, from, dest));
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }

        ProcessMsgNode msgNode = new ProcessMsgNode(id, from, dest, ctx, receivedMsg, processFunction);
        if (!isContinuous(id)) {//消息不连续
            if (notContinuousMap.size() >= maxSize) {//不连续消息缓存达上线
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(new ImException("client window is full"));
                return future;
            }
            notContinuousMap.put(id, msgNode);
            return msgNode.getFuture();
        }
        //处理消息
        return processAsync(msgNode);
    }

    private CompletableFuture<Void> processAsync(ProcessMsgNode node) {
        return CompletableFuture
            .runAsync(node::process)
            .thenAccept(v -> {
                node.sendAck();
                node.complete();
            })
            .thenAccept(v -> {
                lastId.set(node.getId());
                notContinuousMap.remove(node.getId());
            })
            .thenComposeAsync(v -> {
                Long nextId = nextId(node.getId());
                if (notContinuousMap.containsKey(nextId)) {
                    //缓存中拿取下一条消息
                    ProcessMsgNode nextNode = notContinuousMap.get(nextId);
                    return processAsync(nextNode);
                } else {
                    //that's the newest msg
                    return node.getFuture();
                }
            })
            .exceptionally(e -> {
                logger.error("[process received msg] has error", e);
                return null;
            });
    }


    /**
     * 消息是否重复
     * @param msgId
     * @return
     */
    private boolean isRepeat(Long msgId) {
        return msgId <= lastId.get();
    }

    /**
     * 与上一条消息是否是连续的
     */
    private boolean isContinuous(Long msgId) {
        //如果是本次会话的第一条消息
        if (first.compareAndSet(true, false)) {
            return true;
        } else {
            //不是第一条消息，则按照公式算（如果同时有好几条第一条消息，除了真正的第一条，其他会返回false）
            return msgId - lastId.get() == 1;
        }
    }

    private Long nextId(Long id) {
        return id + 1;
    }

    private Internal.InternalMsg getInternalAck(Long msgId, Internal.InternalMsg.Module from, Internal.InternalMsg.Module dest) {
        return Internal.InternalMsg.newBuilder()
            .setVersion(MsgVersion.V1.getVersion())
            .setId(IdWorker.snowGenId())
            .setFrom(from)
            .setDest(dest)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.ACK)
            .setMsgBody(msgId + "")
            .build();
    }
}
