package com.jkantrell.keyLocker.io;

import com.jkantrell.yamlizer.yaml.AbstractYamlConfig;
import com.jkantrell.yamlizer.yaml.ConfigField;

public class KeyLockerConfiguration extends AbstractYamlConfig {
    public KeyLockerConfiguration(String filePath) {
        super(filePath);
    }

    //CONFIG
    @ConfigField
    public boolean unlockIfOpen = false;

    @ConfigField
    public boolean accessibleIfOpen = false;

    @ConfigField
    public boolean handsFreeKeys = false;
}
