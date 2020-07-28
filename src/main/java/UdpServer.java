import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * udp发送广播
 */
public class UdpServer {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap boot = new Bootstrap();
            boot.group(group)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .channel(NioDatagramChannel.class)
                    .handler(new UdpServerHandler());
            boot.bind(57572).sync().channel().closeFuture().await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}

class UdpServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        ctx.executor().parent().execute(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("{" +
                            "\"className\": \"MsgHDServerInfo\"," +
                            "  \"dataLength\": 0," +
                            "  \"msgJson\": \"{\"port\": 57571,\"teacherName\": \"19900000985\",\"teacherId\": \"4795\",\"className\": \"公共班级222\",\"classId\": \"FFFFFFFF-FFFF-FFFF-EEFF-FFFFFFFFFFFF\",\"version\": \"1.0.0-alpha\",\"versionCode\": 20170801,\"webUrl\": \"http://117.78.8.206:8084\"}\"" +
                            "}", Charset.forName("UTF-8")), new InetSocketAddress("255.255.255.255", 57572)));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket packet = (DatagramPacket) msg;
        ByteBuf byteBuf = packet.copy().content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String content = new String(bytes);
        System.out.println("发送广播:" + packet.sender() + "," + content);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}