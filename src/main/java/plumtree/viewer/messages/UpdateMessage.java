package plumtree.viewer.messages;

import babel.generic.ProtoMessage;
import plumtree.utils.Coordinate;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import network.data.Host;

import java.io.IOException;

public class UpdateMessage extends ProtoMessage {

    public final static short MSG_ID = 201;


    private final Host node;
    private final Coordinate coordinate;

    public UpdateMessage(Host node, Coordinate coordinate) {
        super(MSG_ID);
        this.node = node;
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Host getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "UpdateMessage{" +
                "node=" + node +
                ", coordinate=" + coordinate +
                '}';
    }

    public static ISerializer<UpdateMessage> serializer = new ISerializer<UpdateMessage>() {
        @Override
        public void serialize(UpdateMessage updateMessage, ByteBuf out) throws IOException {
            Host.serializer.serialize(updateMessage.node, out);
            updateMessage.coordinate.serialize(out);
        }

        @Override
        public UpdateMessage deserialize(ByteBuf in) throws IOException {
            Host node = Host.serializer.deserialize(in);
            Coordinate coordinate = Coordinate.deserialize(in);
            return new UpdateMessage(node, coordinate);
        }
    };
}
