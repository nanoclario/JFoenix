/*
 * JFoenix
 * Copyright (c) 2015, JFoenix and/or its affiliates., All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package com.jfoenix.controls;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;


/**
 * @author sshahine
 * toggle pane is a hidden pane that is shown upon mouse click on its toggle node
 * NOTE : the toggle animation is specified by the pane clipping shape
 */
public class JFXTogglePane extends StackPane {

	private Timeline toggleAnimation = null;
	
	private ObjectProperty<Control> toggleNode = new SimpleObjectProperty<>();
	
	private ObjectProperty<Node> contentNode = new SimpleObjectProperty<>();

	/*
	 * the scaling factor controls area of the clipping node when
	 * toggeling the pane
	 *  
	 */
	private DoubleProperty scalingFactor = new SimpleDoubleProperty(2.4);
	
	public JFXTogglePane() {
		
		this.clipProperty().addListener((o,oldVal,newVal)->{
			if(newVal != null){
				if(getToggleNode()!=null){
					Region toggleNode = getToggleNode();
					newVal.layoutXProperty().bind(Bindings.createDoubleBinding(()-> toggleNode.getLayoutX() + toggleNode.getWidth()/2, toggleNode.widthProperty(), toggleNode.layoutXProperty()));
					newVal.layoutYProperty().bind(Bindings.createDoubleBinding(()-> toggleNode.getLayoutY() + toggleNode.getHeight()/2, toggleNode.heightProperty(), toggleNode.layoutYProperty()));
				}
			}
		});
		
		this.widthProperty().addListener((o,oldVal,newVal) -> updateToggleAnimation());
		this.heightProperty().addListener((o,oldVal,newVal) -> updateToggleAnimation());
		
		toggleNode.addListener((o,oldVal,newVal)-> {
			if(newVal != null){
				if(getClip()!=null){
					getClip().layoutXProperty().unbind();
					getClip().layoutYProperty().unbind();
					getClip().layoutXProperty().bind(Bindings.createDoubleBinding(()-> newVal.getLayoutX() + newVal.getWidth()/2, newVal.widthProperty(), newVal.layoutXProperty()));
					getClip().layoutYProperty().bind(Bindings.createDoubleBinding(()-> newVal.getLayoutY() + newVal.getHeight()/2, newVal.heightProperty(), newVal.layoutYProperty()));
				}
			}
			updateToggleAnimation();
			newVal.widthProperty().addListener((o1,oldVal1,newVal1)-> updateToggleAnimation());
			newVal.heightProperty().addListener((o1,oldVal1,newVal1)-> updateToggleAnimation());
			newVal.setOnMouseClicked((click)->togglePane());
		});
		
	}
	
	public void togglePane(){
		if(toggleAnimation == null) updateToggleAnimation();
		this.getClip().scaleXProperty().unbind();
		this.getClip().scaleYProperty().unbind();
		toggleAnimation.setRate(toggleAnimation.getRate() * -1);
		if(toggleAnimation.getCurrentTime().equals(Duration.millis(0)) && toggleAnimation.getRate() == -1) toggleAnimation.playFrom(Duration.millis(510));
		else toggleAnimation.play();	
	}
	
	
	private void updateToggleAnimation(){
		if(getContentNode() == null) return;
		double rateX = this.getWidth()/getClip().getLayoutBounds().getWidth();
		double rateY = this.getHeight()/getClip().getLayoutBounds().getHeight();
		double newRate = Math.max(rateX, rateY) * getScalingFactor();
		double animationRate = toggleAnimation == null ? -1 : toggleAnimation.getRate();
		
		toggleAnimation = new Timeline(
				new KeyFrame(Duration.millis(0), new KeyValue(getClip().scaleXProperty(), 1 , Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(0), new KeyValue(getClip().scaleYProperty(), 1 , Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(0), new KeyValue(getContentNode().opacityProperty(), 0 , Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(350), new KeyValue(getClip().scaleXProperty(), newRate , Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(350), new KeyValue(getClip().scaleYProperty(), newRate , Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(370), new KeyValue(getContentNode().opacityProperty(), 0 , Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(510), new KeyValue(getContentNode().opacityProperty(), 1 , Interpolator.EASE_BOTH)));
		toggleAnimation.setOnFinished((finish)->{
			if(toggleAnimation.getRate() == 1){
				this.getClip().scaleXProperty().bind(Bindings.createDoubleBinding(()->{
					double X = this.getWidth()/getClip().getLayoutBounds().getWidth();
					double Y = this.getHeight()/getClip().getLayoutBounds().getHeight();
					double scale = Math.max(X, Y) * getScalingFactor();
					return scale;
				}, this.widthProperty(), this.heightProperty()));
				
				this.getClip().scaleYProperty().bind(Bindings.createDoubleBinding(()->{
					double X = this.getWidth()/getClip().getLayoutBounds().getWidth();
					double Y = this.getHeight()/getClip().getLayoutBounds().getHeight();
					double scale = Math.max(X, Y) * getScalingFactor();
					return scale;
				}, this.widthProperty(), this.heightProperty()));
			}			
		});
		toggleAnimation.setRate(animationRate);
	}
	
	/***************************************************************************
	 *                                                                         *
	 * Public Properties                                                       *
	 *                                                                         *
	 **************************************************************************/
	
	public final ObjectProperty<Control> toggleNodeProperty() {
		return this.toggleNode;
	}

	public final Control getToggleNode() {
		return this.toggleNodeProperty().get();
	}

	public final void setToggleNode(final Control toggleNode) {
		this.toggleNodeProperty().set(toggleNode);
	}

	public final ObjectProperty<Node> contentNodeProperty() {
		return this.contentNode;
	}

	public final Node getContentNode() {
		return this.contentNodeProperty().get();
	}

	public final void setContentNode(final Node content) {
		this.contentNodeProperty().set(content);
		content.setOpacity(0);
	}

	public final DoubleProperty scalingFactorProperty() {
		return this.scalingFactor;
	}

	public final double getScalingFactor() {
		return this.scalingFactorProperty().get();
	}

	public final void setScalingFactor(final double scalingFactor) {
		this.scalingFactorProperty().set(scalingFactor);
	}
	
	
}