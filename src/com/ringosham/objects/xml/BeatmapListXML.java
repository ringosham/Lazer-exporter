package com.ringosham.objects.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "beatmaplist")
@XmlAccessorType(XmlAccessType.FIELD)
public class BeatmapListXML {
    @XmlElement(name = "beatmap")
    private List<BeatmapXML> beatmaps;

    public List<BeatmapXML> getBeatmaps() {
        return beatmaps;
    }

    public void setBeatmaps(List<BeatmapXML> beatmaps) {
        this.beatmaps = beatmaps;
    }
}
