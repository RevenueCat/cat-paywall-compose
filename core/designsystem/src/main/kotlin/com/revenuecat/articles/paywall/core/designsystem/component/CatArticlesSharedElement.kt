/*
 * Copyright (c) 2025 RevenueCat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revenuecat.articles.paywall.core.designsystem.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

fun Modifier.catArticlesSharedElement(
  sharedTransitionScope: SharedTransitionScope,
  isLocalInspectionMode: Boolean,
  state: SharedContentState,
  animatedVisibilityScope: AnimatedVisibilityScope,
  boundsTransform: BoundsTransform = DefaultBoundsTransform,
  placeholderSize: PlaceholderSize = PlaceholderSize.ContentSize,
  renderInOverlayDuringTransition: Boolean = true,
  zIndexInOverlay: Float = 0f,
  clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier {
  return if (isLocalInspectionMode) {
    this
  } else {
    with(sharedTransitionScope) {
      this@catArticlesSharedElement.sharedBounds(
        sharedContentState = state,
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = boundsTransform,
        placeholderSize = placeholderSize,
        renderInOverlayDuringTransition = renderInOverlayDuringTransition,
        zIndexInOverlay = zIndexInOverlay,
        clipInOverlayDuringTransition = clipInOverlayDuringTransition,
      )
    }
  }
}

private val ParentClip: OverlayClip =
  object : OverlayClip {
    override fun getClipPath(
      state: SharedContentState,
      bounds: Rect,
      layoutDirection: LayoutDirection,
      density: Density,
    ): Path? {
      return state.parentSharedContentState?.clipPathInOverlay
    }
  }

private val DefaultSpring = spring(
  stiffness = Spring.StiffnessMediumLow,
  visibilityThreshold = Rect.VisibilityThreshold,
)

private val DefaultBoundsTransform =
  BoundsTransform { _, _ -> DefaultSpring }
