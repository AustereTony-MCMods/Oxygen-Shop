package austeretony.oxygen_shop.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.persistent.PersistentEntry;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class ShopOffer implements PersistentEntry, SynchronousEntry {

    private long id, price;

    private ItemStackWrapper stackWrapper;

    private int amount, discount;

    public ShopOffer() {}

    public ShopOffer(long offerId, ItemStackWrapper offeredStack, int amount, long price, int discount) {
        this.id = offerId;
        this.stackWrapper = offeredStack;
        this.amount = amount;
        this.price = price;
        this.discount = discount;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public ItemStackWrapper getStackWrapper() {
        return this.stackWrapper;
    }

    public int getAmount() {
        return this.amount;
    }

    public long getPrice() {
        return this.price;
    }

    public int getDiscount() {
        return this.discount;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("id", new JsonPrimitive(this.id));
        jsonObject.add("amount", new JsonPrimitive(this.amount));
        jsonObject.add("price", new JsonPrimitive(this.price));
        jsonObject.add("discount", new JsonPrimitive(this.discount));
        jsonObject.add("itemstack", this.stackWrapper.toJson());
        return jsonObject;
    }

    public static ShopOffer fromJson(JsonObject jsonObject) {
        ShopOffer offer = new ShopOffer(
                jsonObject.get("id").getAsLong(),
                ItemStackWrapper.fromJson(jsonObject.get("itemstack").getAsJsonObject()),
                jsonObject.get("amount").getAsInt(),
                jsonObject.get("price").getAsInt(),
                jsonObject.get("discount").getAsInt());
        return offer;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.id, bos);
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
        StreamUtils.write((byte) this.discount, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.id = StreamUtils.readLong(bis);
        this.stackWrapper = ItemStackWrapper.read(bis);
        this.amount = StreamUtils.readShort(bis);
        this.price = StreamUtils.readLong(bis);
        this.discount = StreamUtils.readByte(bis);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(this.id);
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
        buffer.writeLong(this.price);
        buffer.writeByte(this.discount);
    }

    @Override
    public void read(ByteBuf buffer) {
        this.id = buffer.readLong();
        this.stackWrapper = ItemStackWrapper.read(buffer);
        this.amount = buffer.readShort();
        this.price = buffer.readLong();
        this.discount = buffer.readByte();
    }
}
