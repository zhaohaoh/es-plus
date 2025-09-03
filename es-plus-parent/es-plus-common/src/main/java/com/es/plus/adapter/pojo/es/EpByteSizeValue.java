//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.es.plus.adapter.pojo.es;
public class EpByteSizeValue  {
    private final long size;
    private final EpByteSizeUnit unit;

   

    public EpByteSizeValue(long bytes) {
        this(bytes, EpByteSizeUnit.BYTES);
    }

    public EpByteSizeValue(long size, EpByteSizeUnit unit) {
        if (size >= -1L && (size != -1L || unit == EpByteSizeUnit.BYTES)) {
            if (size > Long.MAX_VALUE / unit.toBytes(1L)) {
                throw new IllegalArgumentException("Values greater than 9223372036854775807 bytes are not supported: " + size + unit.getSuffix());
            } else {
                this.size = size;
                this.unit = unit;
            }
        } else {
            throw new IllegalArgumentException("Values less than -1 bytes are not supported: " + size + unit.getSuffix());
        }
    }

    long getSize() {
        return this.size;
    }

    EpByteSizeUnit getUnit() {
        return this.unit;
    }

    

    public long getBytes() {
        return this.unit.toBytes(this.size);
    }

    public long getKb() {
        return this.unit.toKB(this.size);
    }

    public long getMb() {
        return this.unit.toMB(this.size);
    }

    public long getGb() {
        return this.unit.toGB(this.size);
    }

    public long getTb() {
        return this.unit.toTB(this.size);
    }

    public long getPb() {
        return this.unit.toPB(this.size);
    }

    public double getKbFrac() {
        return (double)this.getBytes() / (double)1024.0F;
    }

    public double getMbFrac() {
        return (double)this.getBytes() / (double)1048576.0F;
    }

    public double getGbFrac() {
        return (double)this.getBytes() / (double)1.0737418E9F;
    }

    public double getTbFrac() {
        return (double)this.getBytes() / (double)1.0995116E12F;
    }

    public double getPbFrac() {
        return (double)this.getBytes() / (double)1.1258999E15F;
    }

    public String getStringRep() {
        return this.size <= 0L ? String.valueOf(this.size) : this.size + this.unit.getSuffix();
    }

   
}
