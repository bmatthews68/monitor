/*
 * Copyright 2011-2012 Brian Matthews
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

package com.btmatthews.utils.monitor.mojo;

import com.btmatthews.utils.monitor.Monitor;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
@Mojo(name = "stop")
public class StopMojo extends AbstractServerMojo {

    /**
     * Stop a running an embedded server by sending a {@code stop} command to the monitor that is controlling that
     * server.
     *
     * @throws org.apache.maven.plugin.MojoFailureException
     *          If there was an error stopping the embedded server.
     */
    @Override
    public void execute() throws MojoFailureException {
        final Monitor monitor = createMonitor();
        monitor.sendCommand("stop", this);
    }
}
