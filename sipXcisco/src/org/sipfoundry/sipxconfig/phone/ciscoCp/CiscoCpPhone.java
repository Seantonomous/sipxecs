package org.sipfoundry.sipxconfig.phone.ciscoCp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.SpecialUser;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.Device;
import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.phone.PhoneContext;
import org.sipfoundry.sipxconfig.phone.PhoneModel;
import org.sipfoundry.sipxconfig.device.Profile;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.device.ProfileFilter;
import org.sipfoundry.sipxconfig.device.ProfileLocation;
import org.sipfoundry.sipxconfig.phone.ciscoCp.CiscoCp;
import org.sipfoundry.sipxconfig.phone.ciscoCp.CiscoCpModel;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;
import org.sipfoundry.commons.util.ShortHash;
import org.sipfoundry.sipxconfig.speeddial.SpeedDial;
import org.sipfoundry.sipxconfig.speeddial.Button;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CiscoCpPhone
extends CiscoCp {
    private static final String USER_ID_SETTING = "Ext/User_ID";
    private static final String DISPLAY_NAME_SETTING = "Ext/Display_Name";
    private static final String PASSWORD_SETTING = "Ext/Password";
    private static final String REGISTRATION_SERVER_SETTING = "Proxy_and_Registration/Proxy";
    private static final String PROVISION_AOR = "%s~%s";
    private static final String MIME_TYPE_PLAIN = "text/plain";
    private static final String TEMPLATE_DIR = "ciscoCp";
    private static final Log LOG = LogFactory.getLog(CiscoCpPhone.class);

    private String m_firmwareFilename = "";
    private SpeedDial m_speedDial;

    protected void initialize() {
        DeviceDefaults deviceDefaults = this.getPhoneContext().getPhoneDefaults();
        this.m_speedDial = getPhoneContext().getSpeedDial(this);
        this.addDefaultBeanSettingHandler((Object)new CiscoCpPhoneDefaults(deviceDefaults));
        this.addDefaultBeanSettingHandler((Object)new CiscoCpProvisioningDefaults(deviceDefaults, (CiscoCpModel)this.getModel()));
    }

    public List<Button> getSpeedDials() {
      if (m_speedDial != null) {
        return m_speedDial.getButtons();
      }else {
        return null;
      }
    }

    public void initializeLine(Line line) {
        DeviceDefaults deviceDefaults = this.getPhoneContext().getPhoneDefaults();
        line.addDefaultBeanSettingHandler((Object)new CiscoCpLineDefaults(deviceDefaults, line, this));
    }

    public void setModel(PhoneModel phoneModel) {
        super.setModel(phoneModel);
        this.setDeviceVersion(((CiscoCpModel)phoneModel).getDefaultVersion());
    }

    public String getDefaultVersionId() {
        DeviceVersion deviceVersion = getDeviceVersion();
        return deviceVersion != null ? deviceVersion.getVersionId() : null;
    }

    @Override
    public Profile[] getProfileTypes() {
        Profile[] profileTypes = new Profile[] { new MainProfile(getMainFileName()) };
        return profileTypes;
    }

    @Override
    public String getProfileFilename() {
        return getSerialNumber();
    }

    @Override
    public void removeProfiles(ProfileLocation location) {
        Profile[] profiles = getProfileTypes();
        for (Profile profile : profiles) {
            location.removeProfile(profile.getName());
        }
    }

    @Override
    public void restart() {
        sendCheckSyncToMac();
    }

    public String getMainFileName() {
        return String.format("%s.cfg", getSerialNumber());
    }

    public String getTemplateDir() {
        return TEMPLATE_DIR;
    }

    @Override
    public void setDeviceVersion(DeviceVersion deviceVersion) {
        if (deviceVersion == null) {
          return;
        }
        super.setDeviceVersion(deviceVersion);
        LOG.info(String.format("Setting Device Version: %s", deviceVersion.getVersionId()));
        this.m_firmwareFilename = ((CiscoCpModel)this.getModel()).getFirmwareFilename(deviceVersion);

        this.addDefaultBeanSettingHandler((Object)new CiscoCpEditProvisioningDefaults(this.m_firmwareFilename));
    }

    public Collection<Setting> getProfileLines() {
        int n;
        int n2 = this.getModel().getMaxLineCount();
        ArrayList<Setting> arrayList = new ArrayList<Setting>(this.getMaxLineCount());
        List list = this.getLines();

        if (list.isEmpty()) {
            Line line = this.createSpecialPhoneProvisionUserLine();
            line.setSettingValue("reg/label", line.getUser().getDisplayName());
            line.setSettingValue("reg/address", String.format(PROVISION_AOR, SpecialUser.SpecialUserType.PHONE_PROVISION.getUserName(), ShortHash.get((String)this.getSerialNumber())));
            list.add(line);
        }

        Iterator iterator = list.iterator();
        for (n = 0; iterator.hasNext() && n < n2; ++n) {
            arrayList.add(((Line)iterator.next()).getSettings());
        }
        while (n < n2) {
            Line line = this.createLine();
            line.setPhone((Phone)this);
            arrayList.add(line.getSettings());
            ++n;
        }
        return arrayList;
    }

    protected void setLineInfo(Line line, LineInfo lineInfo) {
        line.setSettingValue(DISPLAY_NAME_SETTING, lineInfo.getDisplayName());
        line.setSettingValue(USER_ID_SETTING, lineInfo.getUserId());
        line.setSettingValue(PASSWORD_SETTING, lineInfo.getPassword());
        line.setSettingValue(REGISTRATION_SERVER_SETTING, lineInfo.getRegistrationServer());
    }

    protected LineInfo getLineInfo(Line line) {
        LineInfo lineInfo = new LineInfo();
        lineInfo.setDisplayName(line.getSettingValue(DISPLAY_NAME_SETTING));
        lineInfo.setUserId(line.getSettingValue(USER_ID_SETTING));
        lineInfo.setPassword(line.getSettingValue(PASSWORD_SETTING));
        lineInfo.setRegistrationServer(line.getSettingValue(REGISTRATION_SERVER_SETTING));
        return lineInfo;
    }

    public static class CiscoCpLineDefaults {
        private final Line m_line;
        private final DeviceDefaults m_defaults;
        private final CiscoCpPhone m_phone;

        CiscoCpLineDefaults(DeviceDefaults deviceDefaults, Line line, CiscoCpPhone phone) {
            this.m_defaults = deviceDefaults;
            this.m_phone = phone;

            if (line == null) {
                this.m_line = phone.createSpecialPhoneProvisionUserLine();
                line.setSettingValue("reg/label", line.getUser().getDisplayName());
                line.setSettingValue("reg/address", String.format(PROVISION_AOR, SpecialUser.SpecialUserType.PHONE_PROVISION.getUserName(), ShortHash.get((String)phone.getSerialNumber())));
            }else {
                this.m_line = line;
            }
        }

        @SettingEntry(path="Ext/User_ID")
        public String getUserName() {
            String string = null;
            User user = this.m_line.getUser();
            if (user != null) {
                string = user.getUserName();
            }
            return string;
        }

        @SettingEntry(path="Ext/Auth_ID")
        public String getAuthId() {
            String string = null;
            User user = this.m_line.getUser();
            if (user != null) {
                string = user.getUserName();
            }

            string += "/" + this.m_phone.getSerialNumber();
            return string;
        }

        @SettingEntry(path="Ext/Display_Name")
        public String getDisplayName() {
            String string = null;
            User user = this.m_line.getUser();
            if (user != null) {
                string = user.getDisplayName();
            }
            return string;
        }

        @SettingEntry(path="Ext/Password")
        public String getPassword() {
            String string = null;
            User user = this.m_line.getUser();
            if (user != null) {
                string = user.getSipPassword();
            }
            return string;
        }

        @SettingEntry(path="Proxy_and_Registration/Proxy")
        public String getRegistrationServer() {
            DeviceDefaults deviceDefaults = this.m_line.getPhoneContext().getPhoneDefaults();
            return deviceDefaults.getDomainName();
        }

        @SettingEntry(path="Call_Feature_Settings/MOH_Server")
        public String getMohUrl() {
            User user = this.m_line.getUser();
            String string = user != null ? user.getMusicOnHoldUri() : this.m_defaults.getMusicOnHoldUri();
            return SipUri.stripSipPrefix((String)string);
        }

        @SettingEntry(path="Call_Feature_Settings/Mailbox_ID")
        public String getMailboxId() {
            User user = this.m_line.getUser();
            if (user != null) {
                return user.getUserName();
            }
            return "";
        }
    }

    public static class CiscoCpPhoneDefaults {
        private final DeviceDefaults m_defaults;

        CiscoCpPhoneDefaults(DeviceDefaults deviceDefaults) {
            this.m_defaults = deviceDefaults;
        }

        @SettingEntry(path="Phone/Voice_Mail_Number")
        public String getVoicemailNumber() {
            return this.m_defaults.getVoiceMail();
        }
    }

    public static class CiscoCpEditProvisioningDefaults {
      private final String m_firmwareFileName;

      CiscoCpEditProvisioningDefaults(String firmwareFileName) {
          this.m_firmwareFileName = firmwareFileName;
      }

      @SettingEntry(path="Provisioning/Upgrade_Rule")
      public String setUpgradeRule() {
          return m_firmwareFileName;
      }
    }

    public static class CiscoCpProvisioningDefaults {
      private final DeviceDefaults m_defaults;
      private final CiscoCpModel m_model;

      CiscoCpProvisioningDefaults(DeviceDefaults deviceDefaults, CiscoCpModel model) {
          this.m_defaults = deviceDefaults;
          this.m_model = model;
      }

      @SettingEntry(path="Provisioning/Profile_Rule")
      public String setProfileRule() {
          return "$PSN.xml";
      }
      
      @SettingEntry(path="Provisioning/Profile_Rule_B")
      public String setProfileRuleB() {
          return "$MA.cfg";
      }
      
      @SettingEntry(path="Provisioning/Provisioning_Server_Address")
      public String setProvisioningServerAddress() {
          return "$P";
      }
    }

    public static class MainProfile extends Profile {
        public MainProfile(String name) {
            super(name, MIME_TYPE_PLAIN);
        }

        @Override
        protected ProfileFilter createFilter(Device device) {
            return null;
        }

        @Override
        protected ProfileContext createContext(Device device) {
            CiscoCpPhone phone = (CiscoCpPhone) device;
            return new MainConfiguration(phone);
        }
    }
}
