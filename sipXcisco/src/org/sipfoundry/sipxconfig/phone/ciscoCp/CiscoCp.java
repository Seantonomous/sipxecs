package org.sipfoundry.sipxconfig.phone.ciscoCp;

import java.io.File;
import org.sipfoundry.sipxconfig.device.ProfileGenerator;
import org.sipfoundry.sipxconfig.device.ProfileLocation;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.phone.PhoneModel;
import org.sipfoundry.sipxconfig.phone.ciscoCp.CiscoCpModel;

public abstract class CiscoCp
extends Phone {
    public static final String PORT = "port";
    public static final String SIP = "sip";

    protected CiscoCp() {
    }

    public int getMaxLineCount() {
        return this.getModel().getMaxLineCount();
    }

    public void restart() {
        this.sendCheckSyncToFirstLine();
    }

    protected void copyFiles(ProfileLocation profileLocation) {
      /*
        CiscoCpModel ciscoMppModel = (CiscoCpModel)this.getModel();
        String string = ciscoMppModel.getDefaultConfigName();
        if (null == string) {
            return;
        }
        String string2 = ciscoMppModel.getModelDir() + File.separator + "default.cfg";
        this.getProfileGenerator().copy(profileLocation, string2, string);
      */
    }

    public String getProfileFilename() {
        String string = this.getSerialNumber();
        return string+".cfg";
    }
}
