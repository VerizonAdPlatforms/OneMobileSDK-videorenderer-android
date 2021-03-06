/*
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the MIT License. See LICENSE.md file in project root for terms.
 */

package com.aol.mobile.sdk.renderer.sensor;

import com.aol.mobile.sdk.renderer.viewmodel.VideoVM;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static java.lang.Math.toRadians;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CameraOrientationModuleTest {
    private static final float P = 0.0001f;

    private VideoVM.Callbacks callbacks;
    private CameraOrientationModule com;

    private float asRad(float degrees) {
        return (float) toRadians(degrees);
    }

    @Before
    public void setUp() {
        callbacks = mock(VideoVM.Callbacks.class);
        com = new CameraOrientationModule();
        com.setCallbacks(callbacks);
    }

    @Test
    public void testInitialState() {
        com.updateCameraPosition(0.0, 0.0, true);
        com.updateCameraPosition(0.0, 0.0, false);
        com.updateDeviceOrientation(1.5707963705062866, -0.0, -3.1415927410125732);

        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(0, P));
    }

    @Test
    public void testNoInteractionOnCameraUpdate() {
        com.updateCameraPosition(0f, 0f, true);
        com.updateCameraPosition(0f, 2f, false);
        com.updateCameraPosition(-1f, 2f, false);

        Mockito.verifyZeroInteractions(callbacks);
    }

    @Test
    public void testTrivialBehavior() {
        com.updateCameraPosition(0, 0, true);
        com.updateDeviceOrientation(asRad(0), 0, asRad(90));
        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(0, P));

        com.updateDeviceOrientation(asRad(10), 0, asRad(100));
        verify(callbacks).onCameraDirectionChanged(eq(asRad(-10), P), eq(asRad(-10), P));

        com.updateCameraPosition(0, 0, false);
        com.updateDeviceOrientation(asRad(20), 0, asRad(110));
        verify(callbacks, times(2)).onCameraDirectionChanged(eq(asRad(-10), P), eq(asRad(-10), P));

        com.updateDeviceOrientation(asRad(10), 0, asRad(100));
        verify(callbacks, times(2)).onCameraDirectionChanged(eq(0, P), eq(0, P));

        com.updateDeviceOrientation(asRad(0), 0, asRad(90));
        verify(callbacks).onCameraDirectionChanged(eq(asRad(10), P), eq(asRad(10), P));
    }

    @Test
    public void testCriticalAzimuthPoints() {
        com.updateCameraPosition(0f, 0f, true);
        com.updateDeviceOrientation(asRad(175), 0, 0);

        com.updateDeviceOrientation(asRad(-175), 0, 0);
        verify(callbacks).onCameraDirectionChanged(eq(asRad(-10), P), eq(0, P));

        com.updateDeviceOrientation(asRad(0), 0, 0);
        verify(callbacks).onCameraDirectionChanged(eq(asRad(175), P), eq(0, P));

        com.updateDeviceOrientation(asRad(180), 0, 0);
        verify(callbacks).onCameraDirectionChanged(eq(asRad(-5), P), eq(0, P));

        com.updateDeviceOrientation(asRad(-180), 0, 0);
        verify(callbacks, times(2)).onCameraDirectionChanged(eq(asRad(-5), P), eq(0, P));

        com.updateCameraPosition(0f, 0f, true);
        com.updateDeviceOrientation(asRad(90), 0, 0);
        verify(callbacks, times(2)).onCameraDirectionChanged(eq(0, P), eq(0, P));
    }

    @Test
    public void testCriticalRollPoints() {
        com.updateCameraPosition(0f, 0f, true);
        com.updateDeviceOrientation(asRad(180), 0, asRad(90));
        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(asRad(0), P));

        com.updateDeviceOrientation(asRad(180), 0, asRad(30));
        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(asRad(60), P));

        com.updateDeviceOrientation(asRad(180), 0, asRad(-100));
        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(asRad(-170), P));

        com.updateDeviceOrientation(asRad(180), 0, asRad(-110));
        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(asRad(-160), P));

        com.updateDeviceOrientation(asRad(180), 0, asRad(0));
        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(asRad(90), P));

        com.updateCameraPosition(0f, asRad(90), false);
        com.updateDeviceOrientation(asRad(180), 0, asRad(66));
        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(asRad(24), P));
    }

    @Test
    public void testOutOfScopeCameraDirectionChange() {
        com.updateCameraPosition(0f, 0f, true);
        com.updateDeviceOrientation(0, 0, asRad(30));
        verify(callbacks).onCameraDirectionChanged(eq(0, P), eq(0, P));

        com.updateDeviceOrientation(asRad(90), 0, asRad(0));
        verify(callbacks).onCameraDirectionChanged(eq(asRad(-90), P), eq(asRad(30), P));

        com.updateCameraPosition(asRad(-60), asRad(30), false);
        com.updateDeviceOrientation(asRad(90), 0, asRad(0));
        verify(callbacks).onCameraDirectionChanged(eq(asRad(-60), P), eq(asRad(30), P));

        com.updateCameraPosition(asRad(-180), asRad(-60), false);
        com.updateDeviceOrientation(asRad(90), 0, asRad(0));
        verify(callbacks).onCameraDirectionChanged(eq(asRad(180), P), eq(asRad(-60), P));
    }
}