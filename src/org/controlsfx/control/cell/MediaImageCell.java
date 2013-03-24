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
	
	public MediaImageCell() {
		getStyleClass().add("media-grid-cell");
		itemProperty().addListener(new ChangeListener<Media>() {

			@Override
			public void changed(ObservableValue<? extends Media> arg0,
					Media arg1, Media arg2) {
				getChildren().clear();
				if(mediaPlayer != null) {
					mediaPlayer.stop();
				}
				mediaPlayer = MediaPlayerBuilder.create().media(arg2).build();
				MediaView mediaView = MediaViewBuilder.create().mediaPlayer(mediaPlayer).build();
				mediaView.fitHeightProperty().bind(heightProperty());
				mediaView.fitWidthProperty().bind(widthProperty());
				setGraphic(mediaView);
			}
		});
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
}