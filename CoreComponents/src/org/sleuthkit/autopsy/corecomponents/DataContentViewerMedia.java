/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.corecomponents;

import java.awt.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gstreamer.*;
import org.gstreamer.elements.PlayBin2;
import org.gstreamer.swing.VideoComponent;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.datamodel.File;
import org.sleuthkit.datamodel.TskData;

/**
 *
 * @author dfickling
 */
@ServiceProvider(service = DataContentViewer.class)
public class DataContentViewerMedia extends javax.swing.JPanel implements DataContentViewer {

    private static final String[] IMAGES = new String[]{ ".jpg", ".jpeg", ".png", ".gif", ".jpe", ".bmp"};
    private static final String[] VIDEOS = new String[]{ ".mov", ".m4v", ".flv", ".mp4", ".3gp", ".avi", ".mpg", ".mpeg"};
    private static final String[] AUDIOS = new String[]{ ".mp3", ".wav", ".wma"};
    private static final Logger logger = Logger.getLogger(DataContentViewerMedia.class.getName());
    private VideoComponent videoComponent; 
    private PlayBin2 playbin2;
    private File currentFile;
    private long durationMillis = 0;
    private boolean autoTracking = false; // true if the slider is moving automatically
    /**
     * Creates new form DataContentViewerVideo
     */
    public DataContentViewerMedia() {
        initComponents();
        customizeComponents();
    }
    
    private void customizeComponents() {
        Gst.init();
        resetVideo();
        progressSlider.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    int time = progressSlider.getValue();
                    if(playbin2 != null && !autoTracking) {
                        State orig = playbin2.getState();
                        playbin2.pause();
                        playbin2.getState();
                        playbin2.seek(ClockTime.fromMillis(time));
                        playbin2.setState(orig);
                    }
                }
                
            });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pauseButton = new javax.swing.JButton();
        videoPanel = new javax.swing.JPanel();
        progressSlider = new javax.swing.JSlider();
        progressLabel = new javax.swing.JLabel();

        pauseButton.setText(org.openide.util.NbBundle.getMessage(DataContentViewerMedia.class, "DataContentViewerMedia.pauseButton.text")); // NOI18N
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout videoPanelLayout = new javax.swing.GroupLayout(videoPanel);
        videoPanel.setLayout(videoPanelLayout);
        videoPanelLayout.setHorizontalGroup(
            videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 283, Short.MAX_VALUE)
        );
        videoPanelLayout.setVerticalGroup(
            videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 242, Short.MAX_VALUE)
        );

        progressLabel.setText(org.openide.util.NbBundle.getMessage(DataContentViewerMedia.class, "DataContentViewerMedia.progressLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(videoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pauseButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressLabel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(videoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pauseButton)
                    .addComponent(progressLabel)
                    .addComponent(progressSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        if(playbin2.getState().equals(State.PLAYING)){
            playbin2.pause();
            pauseButton.setText("►");
        } else if(playbin2.getState().equals(State.PAUSED)) {
            playbin2.play();
            pauseButton.setText("||");
        } else {
            progressLabel.setText("Buffering...");
            java.io.File ioFile = extractFile(currentFile);
            if(ioFile == null || !ioFile.exists()) {
                progressLabel.setText("Error buffering file");
                return;
            }
            playbin2.setInputFile(ioFile);
            playbin2.play(); // must play, then pause and get state to get duration.
            playbin2.pause();
            playbin2.getState();
            String duration = playbin2.queryDuration().toString();
            durationMillis = playbin2.queryDuration().toMillis();
            progressSlider.setMaximum((int)durationMillis);
            progressSlider.setMinimum(0);
            final String finalDuration;
            if(duration.length() == 8) {
                finalDuration = duration.substring(3);
                progressLabel.setText("00:00/" + duration);
            } else {
                finalDuration = duration;
                progressLabel.setText("00:00:00/" + duration);
            }
            playbin2.play();
            pauseButton.setText("||");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    long positionMillis = 0;
                    while (positionMillis < durationMillis
                            && !playbin2.getState().equals(State.NULL)) {
                        String position = playbin2.queryPosition().toString();
                        positionMillis = playbin2.queryPosition().toMillis();
                        if (position.length() == 8) {
                            position = position.substring(3);
                        }
                        progressLabel.setText(position + "/" + finalDuration);
                        autoTracking = true;
                        progressSlider.setValue((int) positionMillis);
                        autoTracking = false;
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                        }
                    }
                    if (finalDuration.length() == 5) {
                        progressLabel.setText("00:00/" + finalDuration);
                    } else {
                        progressLabel.setText("00:00:00/" + finalDuration);
                    }
                    playbin2.stop();
                    playbin2.getState();
                    pauseButton.setText("►");
                    progressSlider.setValue(0);
                }
            }).start();
        }
    }//GEN-LAST:event_pauseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton pauseButton;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JSlider progressSlider;
    private javax.swing.JPanel videoPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setNode(Node selectedNode) {
        pauseButton.setText("►");
        if(selectedNode == null) {
            setDataView(null);
            return;
        }
        File file = selectedNode.getLookup().lookup(File.class);
        resetVideo();
        setDataView(file);
        boolean isVidOrAud = containsExt(file.getName(), VIDEOS) || containsExt(file.getName(), AUDIOS);
        pauseButton.setVisible(isVidOrAud);
        progressLabel.setVisible(isVidOrAud);
        progressSlider.setVisible(isVidOrAud);
    }

    private void setDataView(File file) {
        if(file == null)
            return;
        this.currentFile = file;
        
        if(containsExt(file.getName(), IMAGES)) {
            java.io.File ioFile = extractFile(file);
            playbin2.setInputFile(ioFile);
            playbin2.play();
        }
    }

    @Override
    public String getTitle() {
        return "Media View";
    }

    @Override
    public String getToolTip() {
        return "Displays supported multimedia files";
    }

    @Override
    public DataContentViewer getInstance() {
        return new DataContentViewerMedia();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void resetComponent() {
    }
    
    private void resetVideo() {
        if(playbin2 != null) {
            playbin2.stop();
            playbin2.getState();
        } else {
            playbin2 = new PlayBin2("VideoPlayer");
        }
        if(videoComponent != null && videoComponent.getElement() != null) {
            videoComponent.getElement().stop();
            videoComponent.getElement().getState();
        } else {
            videoComponent = new VideoComponent();
            playbin2.setVideoSink(videoComponent.getElement());
        }
        videoPanel.removeAll();
        videoPanel.setLayout(new BoxLayout(videoPanel, BoxLayout.Y_AXIS));
        videoPanel.add(videoComponent);
        videoPanel.revalidate();
        videoPanel.repaint();
    }

    @Override
    public boolean isSupported(Node node) {
        if (node == null) {
            resetVideo();
            return false;
        }

        File file = node.getLookup().lookup(File.class);
        if (file == null) {
            resetVideo();
            return false;
        }

        if (File.dirFlagToValue(file.getDir_flags()).equals(TskData.TSK_FS_NAME_FLAG_ENUM.TSK_FS_NAME_FLAG_UNALLOC.toString())) {
            resetVideo();
            return false;
        }

        String name = file.getName().toLowerCase();
        
        if(file.getSize() == 0) {
            resetVideo();
            return false;
        }
        
        if(containsExt(name, IMAGES) || containsExt(name, AUDIOS) || containsExt(name, VIDEOS)) {
            resetVideo();
            return true;
        }
        
        resetVideo();
        return false;
    }

    @Override
    public boolean isPreferred(Node node, boolean isSupported) {
        return isSupported;
    }
    
    private static boolean containsExt(String name, String[] exts) {
        int extStart = name.lastIndexOf(".");
        String ext = "";
        if (extStart != -1) {
            ext = name.substring(extStart, name.length()).toLowerCase();
        }
        return Arrays.asList(exts).contains(ext);
    }
    
    private java.io.File extractFile(File file) {
        // Get the temp folder path of the case
        String tempPath = Case.getCurrentCase().getTempDirectory();
        String name = file.getName();
        int extStart = name.lastIndexOf(".");
        String ext = "";
        if (extStart != -1) {
            ext = name.substring(extStart, name.length()).toLowerCase();
        }
        tempPath = tempPath + java.io.File.separator + file.getId() + ext;

        // create the temporary file
        java.io.File tempFile = new java.io.File(tempPath);
        if (tempFile.exists()) {
            return tempFile;
        }
        
        try {
            ContentUtils.writeToFile(file, tempFile);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error buffering file", ex);
        }
        return tempFile;
    }
}
