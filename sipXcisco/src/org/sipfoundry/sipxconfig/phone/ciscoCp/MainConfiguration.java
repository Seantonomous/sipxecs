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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sipfoundry.commons.util.ShortHash;
import org.sipfoundry.sipxconfig.common.SpecialUser.SpecialUserType;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.setting.Setting;

/**
 * Responsible for generating ipmid.cfg
 */
public class MainConfiguration extends ProfileContext<CiscoCpPhone> {
    private static final String PROVISION_AOR = "%s~%s";

    public MainConfiguration(CiscoCpPhone device) {
        super(device, device.getTemplateDir() + "/config.vm");
    }

    public Collection<Setting> getLines() {
        CiscoCpPhone phone = getDevice();
        List<Line> lines = phone.getLines();

        // Phones with no configured lines will register under the sipXprovision special user.
        if (lines.isEmpty()) {
            Line line = phone.createSpecialPhoneProvisionUserLine();
            line.setSettingValue("reg/label", line.getUser().getDisplayName());
            line.setSettingValue("reg/address",
                    String.format(PROVISION_AOR, SpecialUserType.PHONE_PROVISION.getUserName(),
                            ShortHash.get(phone.getSerialNumber())));
            lines.add(line);
        }

        int lineCount = lines.size();

        ArrayList<Setting> linesSettings = new ArrayList<Setting>(lineCount);

        for (Line line : lines) {
            linesSettings.add(line.getSettings());
        }

        return linesSettings;
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = super.getContext();
        getDevice().getSettings();
        context.put("lines", getLines());
        return context;
    }
}
