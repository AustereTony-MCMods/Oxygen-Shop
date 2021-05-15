package austeretony.oxygen_shop.common.shop;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Locale;

public class ShopEntry implements SynchronousEntry {

    private long id, price;
    private Type type;
    private ItemStackWrapper stackWrapper;
    private int quantity;
    private float discount; // 0 - 1

    public ShopEntry() {}

    public ShopEntry(long id, Type type, long price, float discount, ItemStackWrapper stackWrapper, int quantity) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.discount = discount;
        this.stackWrapper = stackWrapper;
        this.quantity = quantity;
    }

    @Override
    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public long getBasePrice() {
        return price;
    }

    public boolean isDiscounted() {
        return discount != 0F;
    }

    public float getDiscount() {
        return discount;
    }

    public long getPrice() {
        return (long) (getBasePrice() * (1F + (type == Type.BUY ? -getDiscount() : getDiscount())));
    }

    public ItemStackWrapper getStackWrapper() {
        return stackWrapper;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(id);
        buffer.writeByte(type.ordinal());

        buffer.writeLong(price);
        buffer.writeFloat(discount);

        stackWrapper.write(buffer);
        buffer.writeShort(quantity);
    }

    @Override
    public void read(ByteBuf buffer) {
        id = buffer.readLong();
        type = Type.values()[buffer.readByte()];

        price = buffer.readLong();
        discount = buffer.readFloat();

        stackWrapper = ItemStackWrapper.read(buffer);
        quantity = buffer.readShort();
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setLong("id", id);
        tagCompound.setByte("type_ordinal", (byte) type.ordinal());

        tagCompound.setLong("price", price);
        tagCompound.setFloat("discount", discount);

        tagCompound.setTag("item_stack", stackWrapper.writeToNBT());
        tagCompound.setShort("quantity", (short) quantity);
        return tagCompound;
    }

    public static ShopEntry readFromNBT(NBTTagCompound tagCompound) {
        ShopEntry shopEntry = new ShopEntry();
        shopEntry.id = tagCompound.getLong("id");
        shopEntry.type = Type.values()[tagCompound.getByte("type_ordinal")];

        shopEntry.price = tagCompound.getLong("price");
        shopEntry.discount = tagCompound.getFloat("discount");

        shopEntry.stackWrapper = ItemStackWrapper.readFromNBT(tagCompound.getCompoundTag("item_stack"));
        shopEntry.quantity = tagCompound.getShort("quantity");
        return shopEntry;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("type", type.toString().toLowerCase(Locale.ROOT));

        jsonObject.addProperty("price", price);
        if (discount > 0F) {
            jsonObject.addProperty("discount", discount);
        }

        jsonObject.add("item_stack", stackWrapper.toJson());
        jsonObject.addProperty("quantity", quantity);
        return jsonObject;
    }

    public static ShopEntry fromJson(JsonObject jsonObject) {
        ShopEntry shopEntry = new ShopEntry();
        shopEntry.id = jsonObject.get("id").getAsLong();
        shopEntry.type = Type.valueOf(jsonObject.get("type").getAsString().toUpperCase(Locale.ROOT));

        shopEntry.price = jsonObject.get("price").getAsLong();
        if (jsonObject.has("discount")) {
            shopEntry.discount = jsonObject.get("discount").getAsFloat();
        }

        shopEntry.stackWrapper = ItemStackWrapper.fromJson(jsonObject.getAsJsonObject("item_stack"));
        shopEntry.quantity = jsonObject.get("quantity").getAsInt();
        return shopEntry;
    }

    @Override
    public String toString() {
        return "ShopEntry[" +
                "id= " + id + ", " +
                "type= " + type + ", " +
                "price= " + price + ", " +
                "discount= " + discount + ", " +
                "stackWrapper= " + stackWrapper + ", " +
                "quantity= " + quantity +
                "]";
    }

    public enum Type {

        BUY,
        SELLING
    }
}
