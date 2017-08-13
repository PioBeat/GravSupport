/**
* Copyright (c) Aspose 2002-2014. All Rights Reserved.
*
* LICENSE: This program is free software; you can redistribute it 
* and/or modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
* You should have received a copy of the GNU General Public License
* along with this program. If not, 
* see http://opensource.org/licenses/gpl-3.0.html
*
* @author Adeel Ilyas <adeel.ilyas@aspose.com>
*  
*/
package net.offbeatpioneer.intellij.plugins.grav.module.ui;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * @author Adeel Ilyas
 */
public class ModalTaskImpl extends Task.Modal {
    private boolean done = false;

    public ModalTaskImpl(@Nullable Project project,String message) {
        super(project,message, true);
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        done=true;
    }

    @Override
    public void onCancel() {
        super.onCancel();
        done=false;
   }

    public boolean isDone() {
        return done;
    }

}
