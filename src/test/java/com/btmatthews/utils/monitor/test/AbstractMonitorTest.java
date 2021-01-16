/*
 * Copyright 2011-2021 Brian Matthews
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

package com.btmatthews.utils.monitor.test;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Abstract base class for tests.
 */
public abstract class AbstractMonitorTest {

    /**
     * Execute code asynchronously with a delay of 5 seconds.
     *
     * @param callable The code to be executed after a 5 second delay.
     */
    protected void runWithDelay(final RunAction callable) {
        final Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            callable.run();
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                5000L);
    }

    /**
     * Functional interface to lambdas to be exexuted by {@link #runWithDelay(RunAction)}.
     */
    @FunctionalInterface
    protected interface RunAction {
        /**
         * Execute the lambda.
         *
         * @throws Exception Thrown by code in the lambda.
         */
        void run() throws Exception;
    }
}
