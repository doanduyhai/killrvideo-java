package killrvideo.entity;

import java.util.UUID;

import info.archinnov.achilles.annotations.Column;

public abstract class AbstractVideo {

    @Column
    protected String name;

    @Column("preview_image_location")
    protected String previewImageLocation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviewImageLocation() {
        return previewImageLocation;
    }

    public void setPreviewImageLocation(String previewImageLocation) {
        this.previewImageLocation = previewImageLocation;
    }
}
