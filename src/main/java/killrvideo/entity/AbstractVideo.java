package killrvideo.entity;

import org.hibernate.validator.constraints.NotBlank;
import com.datastax.driver.mapping.annotations.Column;

public abstract class AbstractVideo {

    @NotBlank
    @Column
    protected String name;

    @Column(name = "preview_image_location")
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
