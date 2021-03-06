package newWatchingService;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @author Cloudraid Dev Team (cloudraid.stnetix.com)
 */
public class ChangeSyncFolderEvent {


    private WatchEvent.Kind<?> type;
    private Path source;


    public ChangeSyncFolderEvent(WatchEvent.Kind<?> type, Path source){
        this.type = type;
        this.source = source;
    }

    public WatchEvent.Kind<?> getType() {
        return type;
    }

    public void setType(WatchEvent.Kind<?> type) {
        this.type = type;
    }

    public Path getSource() {
        return source;
    }

    public void setSource(Path source) {
        this.source = source;
    }
}
