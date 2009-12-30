/*
 * Copyright 2009 Brice Lambson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ase.interpreter.rhino;

import java.io.File;

import com.google.ase.Constants;
import com.google.ase.RpcFacade;
import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.jsonrpc.JsonRpcServer;

public class RhinoInterpreterProcess extends InterpreterProcess {
  private final static String RHINO_BIN =
      "dalvikvm -Xss128k "
          + "-classpath /sdcard/ase/extras/rhino/rhino1_7R2-dex.jar org.mozilla.javascript.tools.shell.Main -O -1";

  private final int mAndroidProxyPort;
  
  private final JsonRpcServer mRpcServer;

  public RhinoInterpreterProcess(String launchScript, RpcFacade... facades) {
    super(launchScript);
    
    mRpcServer = JsonRpcServer.create(facades);
    mAndroidProxyPort = mRpcServer.startLocal().getPort();

    buildEnvironment();
  }

  private void buildEnvironment() {
    File dalvikCache = new File(Constants.ASE_DALVIK_CACHE_ROOT);

    if (!dalvikCache.exists()) {
      dalvikCache.mkdirs();
    }

    mEnvironment.put("ANDROID_DATA", Constants.SDCARD_ASE_ROOT);
    mEnvironment.put("AP_PORT", Integer.toString(mAndroidProxyPort));
  }

  @Override
  protected void writeInterpreterCommand() {
    print(RHINO_BIN);

    if (mLaunchScript != null) {
      print(" " + mLaunchScript);
    }

    print("\n");
  }

  @Override
  protected void shutdown() {
    mRpcServer.shutdown();
  }
}