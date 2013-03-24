package org.controlsfx.control.cell;

import org.controlsfx.control.GridCell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayerBuilder;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaViewBuilder;

public class MediaImageCell extends GridCell<Media> {
	
	private MediaPlayer mediaPlayer;
	private final MediaView mediaView;
	
	public MediaImageCell() {
		getStyleClass().add("media-grid-cell");
		
        mediaView = MediaViewBuilder.create().mediaPlayer(mediaPlayer).build();
        mediaView.fitHeightProperty().bind(heightProperty());
        mediaView.fitWidthProperty().bind(widthProperty());
        mediaView.setMediaPlayer(mediaPlayer);
	}
	
	public void pause() {
		if(mediaPlayer != null) {
			mediaPlayer.pause();
		}
	}
	
	public void play() {
		if(mediaPlayer != null) {
			mediaPlayer.play();
		}
	}
	
	public void stop() {
		if(mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}
	
	@Override protected void updateItem(Media item, boolean empty) {
	    super.updateItem(item, empty);
	    
	    getChildren().clear();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
	    
	    if (empty) {
	        setGraphic(null);
	    } else {
	        mediaPlayer = MediaPlayerBuilder.create().media(item).build();
	        mediaView.setMediaPlayer(mediaPlayer);
	        setGraphic(mediaView);
	    }
	}
}