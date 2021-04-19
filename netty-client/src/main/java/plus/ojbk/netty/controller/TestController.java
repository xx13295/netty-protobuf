package plus.ojbk.netty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.ojbk.netty.client.NettyClient;
import plus.ojbk.netty.protobuf.MessageBase;

import java.util.UUID;

@RestController
public class TestController {

    private NettyClient nettyClient;
    @Autowired
    public void setNettyClient(NettyClient nettyClient){
        this.nettyClient = nettyClient;
    }

    @GetMapping("/send")
    public String send() {
        MessageBase.Message message = new MessageBase.Message()
                .toBuilder().setCmd(MessageBase.Message.CommandType.NORMAL)
                .setContent("hello world!")
                .setRequestId(UUID.randomUUID().toString()).build();
        nettyClient.sendMsg(message);
        return "success";
    }
}
