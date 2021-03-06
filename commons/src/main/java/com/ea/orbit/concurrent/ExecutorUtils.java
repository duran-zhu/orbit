/*
 Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2.  Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
     its contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ea.orbit.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorUtils
{
    private static final Logger logger = Logger.getLogger(ExecutorUtils.class.getName());

    public static ExecutorService newScalingThreadPool(
            final int maxThreads)
    {
        return new ForkJoinPool(maxThreads, ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                new UncaughtExceptionHandler()
                {
                    @Override
                    public void uncaughtException(Thread t, Throwable e)
                    {
                        logger.log(Level.SEVERE, "Uncaught Exception", e);
                    }
                }, false);
    }

    @Deprecated
    public static ExecutorService newScalingThreadPool(
            final int minThreads, final int maxThreads,
            final long keepAlive, final TimeUnit keepAliveUnit, final int maxQueueSize)
    {
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxQueueSize)
        {
            private static final long serialVersionUID = -6903933921423432194L;

            @Override
            public boolean offer(Runnable e)
            {
                if (size() <= 1)
                {
                    return super.offer(e);
                }
                else
                {
                    return false;
                }
            }
        };
        return new ThreadPoolExecutor(minThreads, maxThreads,
                keepAlive, keepAliveUnit, queue, (r, executor) -> {
            try
            {
                executor.getQueue().put(r);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return;
            }
        });
    }
}
