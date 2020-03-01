/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.phone.ciscoCp;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.upload.Upload;

public class CiscoCpUpload extends Upload {
    private static final Log LOG = LogFactory.getLog(CiscoCpUpload.class);
    private static final String CISCO_DIR = "/cisco/";
    private static final String VERSION = "firmware/version";
    private static final String TYPE = "firmware/type";
    private static final Pattern SIP_APP = Pattern.compile(".*.loads");
    private String m_profileDir;

    public String getProfileDir() {
        return m_profileDir;
    }

    public void setProfileDir(String profileDir) {
        m_profileDir = profileDir;
    }

    @Override
    public void deploy() {
        String destination = new StringBuilder(getDestinationDirectory()).append(CISCO_DIR)
                .append(getSettingValue(VERSION)).append("/").append(getSettingValue(TYPE)).toString();
        File destinationFolder = new File(destination);
        super.setDestinationDirectory(destination);
        super.deploy();
        
        File[] folder = destinationFolder.listFiles();
        File newFile = new File(destination, "sip" + getSettingValue(TYPE) + "." + getSettingValue(VERSION) + ".loads");
        for (int i = 0; i < folder.length; i++) {
            if (SIP_APP.matcher(folder[i].getName()).matches()) {
              LOG.info("Found: " + folder[i].getName());
              // We need to rename the *.loads files to something more predicatble
              boolean ok = folder[i].renameTo(newFile);
              
              if (!ok) {
                LOG.info("Failed to rename cisco SIP load: " + folder[i].getName() + " to " + newFile.getName());
              }
            }
        }
    }

    @Override
    public void undeploy() {
        super.setDestinationDirectory(getDestinationDirectory() + CISCO_DIR + getSettingValue(VERSION) + "/" + getSettingValue(TYPE));
        super.undeploy();
        /*
        File spipLoc = new File(getDestinationDirectory() + "/SoundPointIPLocalization");
        try {
            if (spipLoc.exists()) {
                FileUtils.deleteDirectory(spipLoc);
            }
            File config = new File(getDestinationDirectory() + "/Config");
            if (config.exists()) {
                FileUtils.deleteDirectory(config);
            }
        } catch (IOException e) {
            LOG.error("IOException while deleting folder.", e);
        }
        */
    }

    @Override
    public FileRemover createFileRemover() {
        return new FileRemover();
    }

    public class FileRemover extends Upload.FileRemover {
        @Override
        public void removeFile(File dir, String name) {
            File victim = new File(dir, name);
            if (!victim.exists()) {
                String[] splits = name.split("/");
                if (splits.length >= 2) {
                    victim = new File(dir, splits[1]);
                }
            }
            victim.delete();
        }
    }

}
