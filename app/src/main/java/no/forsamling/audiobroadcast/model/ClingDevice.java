package no.royalone.audiobroadcast.model;

import org.teleal.cling.model.meta.Device;

import java.io.Serializable;

public class ClingDevice implements Serializable{
    public Device<?, ?, ?> device;

    public ClingDevice(Device<?, ?, ?> device) {
        this.device = device;
    }

    public Device<?, ?, ?> getDevice() {
        return this.device;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.device.equals(((ClingDevice) o).device);
    }

    public int hashCode() {
        return this.device.hashCode();
    }

    public String toString() {
        return (this.device.getDetails() == null || this.device.getDetails().getFriendlyName() == null) ? this.device.getDisplayString() : this.device.getDetails().getFriendlyName();
    }
}
