package com.didithemouse.minididiplus.dragdrop;

import android.view.View;


/**
 * Interface defining an object where drag operations originate.
 *
 */
public interface DragSource {
    void setDragController(DragController dragger);
    void onDropCompleted(View target, boolean success);
}
