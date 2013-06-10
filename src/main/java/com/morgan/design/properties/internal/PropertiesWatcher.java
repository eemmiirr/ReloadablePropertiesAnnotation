package com.morgan.design.properties.internal;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PropertiesWatcher implements Runnable {

    protected static Logger log = LoggerFactory.getLogger(PropertiesWatcher.class);

    public interface EventPublisher {
        void onResourceChanged(Resource resource);
    }

    private final Resource[] locations;
    private final EventPublisher eventPublisher;

    private final FileSystemManager manager;
    private final List<ResourceWatcher> resourceWatchers;
    private final long delay;

    public PropertiesWatcher(final Resource[] locations, final EventPublisher eventPublisher, long delay) throws IOException {
        this.locations = locations;
        this.eventPublisher = eventPublisher;
        this.manager = VFS.getManager();
        this.resourceWatchers = new ArrayList<ResourceWatcher>(locations.length);
        this.delay = delay;
    }

        @Override
        public void run() {
        try {
            for (final Resource resource : locations) {

                final String path = resource.getFile().getAbsolutePath();
                log.info("Starting ResourceWatcher on file {}", path);

                final FileObject file = manager.resolveFile(path);
                resourceWatchers.add(new ResourceWatcher(file, resource, delay));
            }
        } catch (IOException e) {
            log.error("Unable to create resource watchers", e);
            stop();
        }
    }

    public void stop() {
        log.info("Closing File Watching Service");
        for (ResourceWatcher resourceWatcher : resourceWatchers) {
            resourceWatcher.defaultFileMonitor.stop();
        }

        log.info("Shutting down Thread Service");
    }

    private void publishResourceChangedEvent(final Resource resource) {
        this.eventPublisher.onResourceChanged(resource);
    }

    private class ResourceWatcher implements FileListener {

        private final FileObject fileObject;
        private final Resource resource;
        private final long delay;

        private DefaultFileMonitor defaultFileMonitor;

        public ResourceWatcher(final FileObject fileObject, final Resource resource, final long delay) {
            this.fileObject = fileObject;
            this.resource = resource;
            this.delay = delay;
            init();
        }

        public void init() {
            try {
                log.info("START");
                log.info("Watching for modification events for path {}", this.fileObject.getName().getFriendlyURI());

                defaultFileMonitor = new DefaultFileMonitor(this);
                defaultFileMonitor.setChecksPerRun(1);
                defaultFileMonitor.setDelay(delay);
                defaultFileMonitor.addFile(fileObject);
                defaultFileMonitor.start();
            } catch (Exception e) {
                log.info("Exception thrown when watching resources, path {}\nException:", this.fileObject.getName().getFriendlyURI(), e);
                stop();
            }
        }

        @Override
        public void fileCreated(FileChangeEvent event) throws Exception {
            // We don't care about this
            log.debug("File created {}", this.fileObject.getName().getFriendlyURI());
        }

        @Override
        public void fileDeleted(FileChangeEvent event) throws Exception {
            // We don't care about this
            log.debug("File deleted {}", this.fileObject.getName().getFriendlyURI());
        }

        @Override
        public void fileChanged(FileChangeEvent event) throws Exception {
            log.info("Watched Resource changed, modified file [{}]", this.fileObject.getName().getFriendlyURI());
            log.info("  Event Kind [{}]", "CHANGED");

            publishResourceChangedEvent(resource);
        }
    }

}
