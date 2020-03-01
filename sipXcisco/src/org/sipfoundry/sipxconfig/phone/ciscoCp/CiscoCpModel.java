package org.sipfoundry.sipxconfig.phone.ciscoCp;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.phone.PhoneModel;
import org.sipfoundry.sipxconfig.device.DeviceVersion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CiscoCpModel
extends PhoneModel {
    private String m_psn;
    public static final DeviceVersion VERSION_11_1_1 = new DeviceVersion("CiscoCpPhone", "11.1.1");
    public static final DeviceVersion VERSION_11_1_2 = new DeviceVersion("CiscoCpPhone", "11.1.2");
    public static final DeviceVersion VERSION_11_2_1 = new DeviceVersion("CiscoCpPhone", "11.2.1");
    public static final DeviceVersion VERSION_11_2_3 = new DeviceVersion("CiscoCpPhone", "11.2.3");
    public static final DeviceVersion VERSION_11_3_1 = new DeviceVersion("CiscoCpPhone", "11.3.1");
    public static final DeviceVersion VERSION_11_3_X = new DeviceVersion("CiscoCpPhone", "11.3.X");
    public static final DeviceVersion[] SUPPORTED_VERSIONS = new DeviceVersion[]{VERSION_11_1_1, VERSION_11_1_2,
      VERSION_11_2_1, VERSION_11_2_3, VERSION_11_3_1, VERSION_11_3_X};
    private static final Log LOG = LogFactory.getLog(CiscoCpModel.class);

    private DeviceVersion m_deviceVersion;

    public CiscoCpModel() {

    }

    public CiscoCpModel(String string) {
        super(string);
    }

    public String getDefaultConfigName() {
        if (StringUtils.isBlank((String)this.m_psn)) {
            return null;
        }
        return String.format("%s-3PCC.xml", this.m_psn);
    }

    public void setPsn(String string) {
        this.m_psn = string;
    }

    public static DeviceVersion getPhoneDeviceVersion(String string) {
        for (DeviceVersion deviceVersion : SUPPORTED_VERSIONS) {
            if (deviceVersion.getName().contains(string)) {
              return deviceVersion;
            }
        }
        return VERSION_11_2_3;
    }

    public void setDefaultVersion(DeviceVersion deviceVersion) {
        this.m_deviceVersion = deviceVersion;
    }

    public DeviceVersion getDefaultVersion() {
        return this.m_deviceVersion;
    }

    public String getFirmwareFilename(DeviceVersion deviceVersion) {
        String name = "";

        if(this.m_psn.equals("8845") || this.m_psn.equals("8865")) {
          name = "/" + deviceVersion.getVersionId() + "/cisco/" + deviceVersion.getVersionId() + "/8845_8865/sip8845_65." + deviceVersion.getVersionId() + ".loads";
        } else {
          name = "/" + deviceVersion.getVersionId() + "/cisco/" + deviceVersion.getVersionId() + "/88XX/sip88XX." + deviceVersion.getVersionId() + ".loads";
        }

        return name;
    }
}
